package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private final int id;
    private final int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;

    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        status = STATUS.UP;
        lastTrackedObjects = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    private TrackedObject trackObject(DetectedObject detectedObject, int time) {
        StampedCloudPoints points = LiDarDataBase.getInstance().getStampedCloudPoints(detectedObject.getId(), time);
        if (points == null)
            return null;
        return new TrackedObject(detectedObject.getId(), time, detectedObject.getDescription(), points.getCloudPoints());
    }

    public List<TrackedObject> trackObjects(StampedDetectedObjects stampedObjects) {
        int time = stampedObjects.getTime();
        if (LiDarDataBase.getInstance().hasError(time)) {
            this.status = STATUS.ERROR;
            return null;
        }

        List<TrackedObject> trackedObjects = new ArrayList<>();

        for (DetectedObject detectedObject : stampedObjects.getDetectedObjects()) {
            TrackedObject obj = trackObject(detectedObject, time);
            if (obj != null)
                trackedObjects.add(obj);
        }
        if (LiDarDataBase.getInstance().isFinished())
            this.status = STATUS.DOWN;
        this.lastTrackedObjects = trackedObjects;
        return trackedObjects;
    }

    public String getName() {
        return "LiDarWorkerTracker" + id;
    }
}
