package bgu.spl.mics.application.objects;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    private final List<Pose> poseList;
    private final int finalTick;

    public GPSIMU(List<Pose> poseList) {
        this.poseList = poseList;
        this.finalTick = poseList.get(poseList.size() - 1).getTime();
        status = STATUS.UP;
    }

    public Pose detect(int tick) {
        if (status != STATUS.UP)
            return null;
        this.currentTick = tick;
        int index = Collections.binarySearch(poseList, new Pose(tick, 0, 0, 0), Comparator.comparingInt(Pose::getTime));
        if (tick >= finalTick)
            this.status = STATUS.DOWN;
        return index >= 0 ? poseList.get(index) : null;
    }

    public STATUS getStatus() {
        return status;
    }
}
