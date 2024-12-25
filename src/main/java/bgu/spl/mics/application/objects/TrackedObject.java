package bgu.spl.mics.application.objects;

import java.lang.reflect.Array;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    String id;
    int time;
    String description;
    CloudPoint[] coordinates;
}
