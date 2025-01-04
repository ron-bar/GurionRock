package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.MicroService;

import java.util.concurrent.CountDownLatch;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private final GPSIMU gpsimu;
    private final CountDownLatch latch;


    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu, CountDownLatch latch) {
        super("PoseService");
        this.gpsimu = gpsimu;
        this.latch = latch;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        MessageBusImpl.getInstance().register(this);
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            sendBroadcast(new TerminatedBroadcast(getName()));
            terminate();
        });
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if (terminatedBroadcast.getSenderName().equals("TimeService"))
                terminate();
        });
        subscribeBroadcast(TickBroadcast.class, tick -> {
            Pose currentPose = gpsimu.detect(tick.getCurrentTick());
            if (currentPose != null) {
                sendEvent(new PoseEvent(currentPose));
                sendEvent(new StatUpdateEvent(getName(), currentPose));
            }
            if (gpsimu.getStatus() == STATUS.DOWN) {
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });

        latch.countDown();
    }
}
