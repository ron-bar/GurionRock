package bgu.spl.mics.application.objects;

/**
 * Represents the robot's pose (position and orientation) in the environment.
 * Includes x, y coordinates and the yaw angle relative to a global coordinate system.
 */
public class Pose {
    private final int time;
    private final float x;
    private final float y;
    private final float yaw;

    public Pose(int time, float x, float y, float yaw) {
        this.time = time;
        this.x = x;
        this.y = y;
        this.yaw = yaw;
    }

    public int getTime(){
        return time;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public float getYaw(){
        return yaw;
    }
}
