package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * <p>
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private final FusionSlam fusionSlam;
    private final Map<Integer, List<TrackedObject>> pendingObjects;
    private final CountDownLatch latch;

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam, CountDownLatch latch) {
        super("FusionSlam");
        this.fusionSlam = fusionSlam;
        this.pendingObjects = new HashMap<>();
        this.latch = latch;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        MessageBusImpl.getInstance().register(this);
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            sendBroadcast(new TerminatedBroadcast(getName()));
            sendEvent(new StatFinalEvent(fusionSlam.getLandmarks(), crashedBroadcast));
            terminate();
        });
        subscribeBroadcast(TickBroadcast.class, TickBroadcast -> {
            if (ServiceCounter.getServiceCount() == 3)
                terminateGracefully();
        });
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if (terminatedBroadcast.getSenderName().equals("TimeService") || ServiceCounter.getServiceCount() == 3)
                terminateGracefully();
        });
        subscribeEvent(PoseEvent.class, event -> {
            Pose pose = event.getPose();
            fusionSlam.addPose(pose);
            int time = pose.getTime();

            List<TrackedObject> objects = pendingObjects.remove(time);
            if (objects != null)
                for (TrackedObject obj : objects)
                    fusionSlam.processTrackedObject(obj);
        });
        subscribeEvent(TrackedObjectsEvent.class, event -> {
            for (TrackedObject obj : event.getTrackedObjects())
                if (fusionSlam.isProcessable(obj))
                    fusionSlam.processTrackedObject(obj);
                else
                    pendingObjects.computeIfAbsent(obj.getTime(), k -> new ArrayList<>()).add(obj);
        });

        latch.countDown();
    }

    private void terminateGracefully() {
        sendBroadcast(new TerminatedBroadcast(this.getName()));
        sendEvent(new StatFinalEvent(fusionSlam.getLandmarks()));
        terminate();
    }

}
