package bgu.spl.mics.application;

import bgu.spl.mics.ConfigReader;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        ConfigReader.getInstance().init(args[0]);

        List<MicroService> msList = new ArrayList<>();
        int latchCount = ConfigReader.getInstance().getCameras().size() + ConfigReader.getInstance().getLiDarWorkers().size() + 3;
        CountDownLatch latch = new CountDownLatch(latchCount);

        List<Camera> cameraList = ConfigReader.getInstance().getCameras();
        List<LiDarWorkerTracker> lidarList = ConfigReader.getInstance().getLiDarWorkers();
        List<Pose> poseList = ConfigReader.getInstance().getPoses();

        cameraList.forEach(camera -> msList.add(new CameraService(camera, latch)));
        lidarList.forEach(lidar -> msList.add(new LiDarService(lidar, latch)));


        msList.add(new PoseService(new GPSIMU(poseList), latch));
        msList.add(new FusionSlamService(FusionSlam.getInstance(), latch));
        msList.add(new StatisticsService(latch));

        msList.forEach(service -> new Thread(service, service.getName()).start());

        try {
            latch.await();
            new Thread(new TimeService(ConfigReader.getInstance().getTickTime(), ConfigReader.getInstance().getDuration()), "TimeService").start();
        } catch (InterruptedException ignored) {
        }
    }
}
