package bgu.spl.mics;

import bgu.spl.mics.application.objects.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ConfigReader {
    private JsonObject jsonObject;
    private String poseDataPath;
    private String cameraDataPath;
    private String lidarDataPath;
    private int tickTime;
    private int duration;
    private String baseDir;

    private ConfigReader() {
    }

    private static class ConfigReaderHolder {
        private static final ConfigReader instance = new ConfigReader();
    }

    public static ConfigReader getInstance() {
        return ConfigReaderHolder.instance;
    }

    public void init(String configFilePath) {
        try (FileReader reader = new FileReader(configFilePath)) {
            baseDir = new File(configFilePath).getParent();
            this.jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            this.poseDataPath = resolvePath(jsonObject.get("poseJsonFile").getAsString());
            this.cameraDataPath = resolvePath(jsonObject.getAsJsonObject("Cameras").get("camera_datas_path").getAsString());
            this.lidarDataPath = resolvePath(jsonObject.getAsJsonObject("LidarWorkers").get("lidars_data_path").getAsString());
            this.tickTime = jsonObject.get("TickTime").getAsInt();
            this.duration = jsonObject.get("Duration").getAsInt();
        } catch (IOException e) {
            throw new RuntimeException("Error reading configuration file", e);
        }
    }

    public List<Camera> getCameras() {
        JsonObject camerasConfig = jsonObject.getAsJsonObject("Cameras");
        JsonArray camerasArray = camerasConfig.getAsJsonArray("CamerasConfigurations");
        List<Camera> cameras = new ArrayList<>();

        for (int i = 0; i < camerasArray.size(); i++) {
            JsonObject cameraJson = camerasArray.get(i).getAsJsonObject();
            int id = cameraJson.get("id").getAsInt();
            int frequency = cameraJson.get("frequency").getAsInt();
            String key = cameraJson.get("camera_key").getAsString();

            List<StampedDetectedObjects> detectedObjects = loadDetectedObjects(cameraDataPath, key);
            cameras.add(new Camera(id, frequency, detectedObjects));
        }

        return cameras;
    }

    private List<StampedDetectedObjects> loadDetectedObjects(String dataPath, String key) {
        try (FileReader reader = new FileReader(dataPath)) {
            JsonObject cameraDataJson = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray detectedObjectsArray = cameraDataJson.getAsJsonArray(key);

            Type listType = new TypeToken<List<StampedDetectedObjects>>() {}.getType();
            return new Gson().fromJson(detectedObjectsArray, listType);
        } catch (IOException e) {
            throw new RuntimeException("File error");
        }
    }

    public List<Pose> getPoses() {
        try (FileReader reader = new FileReader(poseDataPath)) {
            Type listType = new TypeToken<List<Pose>>() {}.getType();
            return new Gson().fromJson(JsonParser.parseReader(reader), listType);
        } catch (IOException e) {
            throw new RuntimeException("File error");
        }
    }

    public List<StampedCloudPoints> getStampedCloudPoints() {
        try (FileReader reader = new FileReader(lidarDataPath)) {
            Type listType = new TypeToken<List<StampedCloudPoints>>() {}.getType();
            return new Gson().fromJson(reader, listType);
        } catch (IOException e) {
            throw new RuntimeException("File error");
        }
    }

    private String resolvePath(String relativePath) {
        return new File(baseDir, relativePath).toString();
    }

    public int getTickTime() {
        return tickTime;
    }

    public int getDuration() {
        return duration;
    }
}
