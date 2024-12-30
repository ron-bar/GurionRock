package bgu.spl.mics.application;

import bgu.spl.mics.ConfigReader;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.TimeService;

import java.util.ArrayList;
import java.util.List;

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
//        for (Camera c : ConfigReader.getInstance().getCameras())
//            for(StampedDetectedObjects o : c.test())
//            System.out.println(o.getTime());



        //for (Camera c : ConfigReader.getInstance().getCameras())
       //    msList.add(new CameraService(c));
       //msList.add(new TimeService(ConfigReader.getInstance().getTickTime(), ConfigReader.getInstance().getDuration()));
        // TODO: Parse configuration file.
        // TODO: Initialize system components and services.
        // TODO: Start the simulation.
    }
}
