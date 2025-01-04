package bgu.spl.mics.application.objects;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private final int id;
    private final int frequency;
    private STATUS status;
    private final List<StampedDetectedObjects> detectedObjectsList;
    private final int finalTick;

    public Camera(int id, int frequency, List<StampedDetectedObjects> detectedObjectsList) {
        this.id = id;
        this.frequency = frequency;
        this.detectedObjectsList = detectedObjectsList;
        this.finalTick = detectedObjectsList.get(detectedObjectsList.size() - 1).getTime();
        status = STATUS.UP;
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public StampedDetectedObjects detect(int currentTick) {
        if (status != STATUS.UP)
            return null;
        if (currentTick >= finalTick)
            this.status = STATUS.DOWN;
        int index = Collections.binarySearch(detectedObjectsList, new StampedDetectedObjects(currentTick - frequency, null), Comparator.comparingInt(StampedDetectedObjects::getTime));
        if (index < 0)
            return null;
        StampedDetectedObjects obj = detectedObjectsList.get(index);
        if (obj.hasError())
            this.status = STATUS.ERROR;
        return obj;
    }

    public STATUS getStatus() {
        return status;
    }

    public String getName() {
        return "Camera" + id;
    }
}
