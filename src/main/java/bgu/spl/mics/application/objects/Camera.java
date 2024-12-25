package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    int id;
    int frequency;
    STATUS status;
    List<DetectedObject> detectedObjectsList;

    public Camera(int id, int frequency)
    {
        this.id=id;
        this.frequency=frequency;
        detectedObjectsList = new ArrayList<>();
    }

    public int getId(){
        return id;
    }

    public int getFrequency(){
        return frequency;
    }

    public StampedDetectedObjects detect(int currentTime) {
        //todo
        return null;
    }
}
