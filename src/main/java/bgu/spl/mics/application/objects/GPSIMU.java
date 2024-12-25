package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    int currentTick;
    STATUS status;
    List<Pose> poseList;
}
