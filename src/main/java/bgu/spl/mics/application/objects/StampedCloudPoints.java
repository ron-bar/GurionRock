package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    private final String id;
    private final int time;
    private final List<List<Double>> cloudPoints;

    public StampedCloudPoints(String id, int time, List<List<Double>> cloudPoints) {
        this.id = id;
        this.time = time;
        this.cloudPoints = cloudPoints;
    }

    public String getId(){
        return id;
    }

    public int getTime(){
        return time;
    }

    public List<CloudPoint> getCloudPoints(){
        List<CloudPoint> points = new ArrayList<>();
        for(List<Double> p : cloudPoints)
            points.add(new CloudPoint(p.get(0),p.get(1)));
        return points;
    }
}
