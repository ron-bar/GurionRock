package bgu.spl.mics.application.objects;

import com.google.gson.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;
    private Map<String, LandMark> landMarks;

    private final transient HashMap<String, StampedDetectedObjects> lastCameraFrames;
    private final transient HashMap<String, List<DetectedObject>> lastLidarFrames;
    private final transient List<Pose> poseHistory;

    public StatisticalFolder() {
        landMarks = new HashMap<>();
        lastCameraFrames = new HashMap<>();
        lastLidarFrames = new HashMap<>();
        poseHistory = new ArrayList<>();
    }

    public void incrementTime() {
        systemRuntime++;
    }

    public void setLandMarks(List<LandMark> landmarkList) {
        landMarks.clear();
        for (LandMark landmark : landmarkList)
            landMarks.put(landmark.getId(), landmark);
        numLandmarks = landmarkList.size();
    }

    public void updateStats(String sensorName, Object obj) {
        if(obj instanceof Pose)
            poseHistory.add((Pose)obj);
        else if (obj instanceof StampedDetectedObjects) {
            StampedDetectedObjects cameraFrame = (StampedDetectedObjects) obj;
            lastCameraFrames.put(sensorName, cameraFrame);
            numDetectedObjects += cameraFrame.getDetectedObjects().size();
        } else {
            @SuppressWarnings("unchecked")
            List<DetectedObject> lidarFrame = (List<DetectedObject>) obj;
            lastLidarFrames.put(sensorName, lidarFrame);
            numTrackedObjects += lidarFrame.size();
        }
    }

    public void generateOutput() {
        generateOutput(null, null);
    }

    public void generateOutput(String faultySensor, String errorDescription) {
        String fileName = "my.json";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement outputJson;

        if (faultySensor != null) {
            JsonObject errorJson = new JsonObject();

            errorJson.addProperty("error", errorDescription);
            errorJson.addProperty("faultySensor", faultySensor);
            errorJson.add("lastCamerasFrame", gson.toJsonTree(lastCameraFrames));
            errorJson.add("lastLiDarWorkerTrackersFrame", gson.toJsonTree(lastLidarFrames));
            errorJson.add("poses", gson.toJsonTree(poseHistory));
            errorJson.add("statistics", gson.toJsonTree(this).getAsJsonObject());

            outputJson = errorJson;
        } else
            outputJson = gson.toJsonTree(this);

        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(outputJson, writer);
        } catch (IOException e) {
            System.err.println("File writing error");
        }

        System.out.println("Output written to " + fileName);
    }


}


