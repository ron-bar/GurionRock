package bgu.spl.mics.application.objects;

import bgu.spl.mics.ConfigReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private final List<StampedCloudPoints> cloudPoints;
    AtomicBoolean isFinished;

    private static class LiDarDataBaseHolder {
        private static final LiDarDataBase instance = new LiDarDataBase();
    }

    private LiDarDataBase() {
        cloudPoints = Collections.unmodifiableList(ConfigReader.getInstance().getStampedCloudPoints());
        isFinished = new AtomicBoolean(false);
    }

    public StampedCloudPoints getStampedCloudPoints(String id, int time) {
        for (StampedCloudPoints point : cloudPoints)
            if (point.getTime() == time && point.getId().equals(id)) {
                if (point.equals(cloudPoints.get(cloudPoints.size() - 1)))
                    isFinished.set(true);
                return point;
            }
        return null;
    }

    public boolean hasError(int time) {
        for (StampedCloudPoints point : cloudPoints)
            if (point.getTime() == time && point.getId().equalsIgnoreCase("error"))
                return true;
        return false;
    }

    public boolean isFinished() {
        return isFinished.get();
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance() {
        return LiDarDataBaseHolder.instance;
    }
}
