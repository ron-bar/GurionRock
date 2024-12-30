package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private final int time;
    private final List<DetectedObject> detectedObjects;

    public StampedDetectedObjects(int time, List<DetectedObject> detectedObjects) {
        this.time = time;
        this.detectedObjects = detectedObjects;
    }

    public int getTime() {
        return time;
    }

    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }

    public boolean isEmpty() {
        return detectedObjects.isEmpty();
    }

    public boolean hasError() {
        for (DetectedObject obj : detectedObjects)
            if (obj.getId().equals("ERROR"))
                return true;
        return false;
    }
}
