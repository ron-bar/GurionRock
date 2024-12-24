package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.MicroService;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private final GPSIMU gpsimu;
    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu=gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, tick -> {
            int currentTick = tick.getCurrentTick();
            Pose currentPose = gpsimu.getPoseAtTick(currentTick);
            if (currentPose != null) {
                sendEvent(new PoseEvent(currentPose));
            }
        });
    }
}
