package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
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
    Camera camera;

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
        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> terminate());//callback??
        //subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> ???);
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            int currentTime = tickBroadcast.getCurrentTick();
            if ((currentTime % camera.getFrequency()) == 0) {
                List<StampedDetectedObjects> detections = camera.detect(currentTime); // Perform detection

                if (!detections.isEmpty()) {
                    //DetectObjectsEvent event = new DetectObjectsEvent(detections, currentTime);
                    //sendEvent(event);
                }
            }
        });
    }
}
