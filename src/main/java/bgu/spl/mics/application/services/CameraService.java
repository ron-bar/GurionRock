package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;


import java.util.List;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * <p>
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private final Camera camera;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("CameraService" + camera.getId());
        this.camera = camera;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        MessageBusImpl.getInstance().register(this);
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> terminate());
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if (terminatedBroadcast.getSenderName().equals("TimeService"))
                terminate();
        });

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            StampedDetectedObjects detection = camera.detect(tickBroadcast.getCurrentTick());
            if (detection != null)
                /*Future<Boolean> result =*/ sendEvent(new DetectObjectsEvent(detection));
            if(camera.getStatus() != STATUS.UP){
                Broadcast b = camera.getStatus() == STATUS.DOWN ? new TerminatedBroadcast(getName()) : new CrashedBroadcast();
                sendBroadcast(b);
                terminate();
            }
        });
    }


}
