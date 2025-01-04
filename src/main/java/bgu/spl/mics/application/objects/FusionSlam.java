package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private final List<LandMark> landmarks;
    private final HashMap<Integer, Pose> poses; // used a hashmap instead of a list for efficiency

    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam instance = new FusionSlam();
    }

    private FusionSlam() {
        landmarks = new ArrayList<>();
        poses = new HashMap<>();
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }

    public void addPose(Pose pose) {
        this.poses.put(pose.getTime(), pose);
    }

    private Pose getPose(int time) {
        return poses.get(time);
    }

    public boolean isProcessable(TrackedObject obj) {
        return getPose(obj.getTime()) != null;
    }

    public void processTrackedObject(TrackedObject obj) {
        Pose pose = getPose(obj.getTime());
        if (pose == null)
            return;

        List<CloudPoint> newPoints = new ArrayList<>();

        for (CloudPoint point : obj.getCoordinates())
            newPoints.add(calcGlobalPoint(pose, point));

        LandMark landmark = getLandmark(obj.getId());

        if (landmark == null) {
            landmarks.add(new LandMark(obj.getId(), obj.getDescription(), newPoints));
            return;
        }

        List<CloudPoint> avgPoints = new ArrayList<>();
        List<CloudPoint> oldPoints = landmark.getCoordinates();

        for (int i = 0; i < Math.max(oldPoints.size(), newPoints.size()); i++) {
            if (i < oldPoints.size() && i < newPoints.size()) {
                CloudPoint oldPoint = oldPoints.get(i), newPoint = newPoints.get(i);
                double avgX = (oldPoint.getX() + newPoint.getX()) / 2.0;
                double avgY = (oldPoint.getY() + newPoint.getY()) / 2.0;
                avgPoints.add(new CloudPoint(avgX, avgY));
            } else if (i < oldPoints.size())
                avgPoints.add(oldPoints.get(i));
            else
                avgPoints.add(newPoints.get(i));
        }

        landmark.setCoordinates(avgPoints);
    }

    private LandMark getLandmark(String id) {
        for (LandMark l : landmarks)
            if (l.getId().equals(id))
                return l;
        return null;
    }

    private CloudPoint calcGlobalPoint(Pose pose, CloudPoint point) {
        double yaw = pose.getYaw() * (Math.PI / 180);
        double xRobot = pose.getX(), yRobot = pose.getY();
        double xLocal = point.getX(), yLocal = point.getY();
        double xGlobal = Math.cos(yaw) * xLocal - Math.sin(yaw) * yLocal + xRobot;
        double yGlobal = Math.sin(yaw) * xLocal + Math.cos(yaw) * yLocal + yRobot;
        return new CloudPoint(xGlobal, yGlobal);
    }

    public List<LandMark> getLandmarks() {
        return landmarks;
    }

}
