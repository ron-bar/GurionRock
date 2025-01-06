package bgu.spl.mics;

import static org.junit.jupiter.api.Assertions.*;

import bgu.spl.mics.application.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class FusionSlamTest {
    private FusionSlam fusionSlam;
    private final double delta = 0.01;

    @BeforeEach
    void setUp() {
        fusionSlam = FusionSlam.getInstance();
        fusionSlam.getLandmarks().clear();
    }

    @Test
    void testNewLandmark() {
        assertEquals(0, fusionSlam.getLandmarks().size(), "Landmarks should be empty at this point");

        // adding a pose
        Pose pose = new Pose(1, 5, 10, 30);
        fusionSlam.addPose(pose);

        // creating a test object
        ArrayList<CloudPoint> localPoints = new ArrayList<>();
        localPoints.add(new CloudPoint(2.0, 3.0));
        TrackedObject trackedObject = new TrackedObject("Desk1", 1, "Desk", localPoints);

        fusionSlam.processTrackedObject(trackedObject);

        // fetching the resulted object
        List<LandMark> landmarks = fusionSlam.getLandmarks();
        assertEquals(1, landmarks.size(), "One landmark should be created");
        LandMark landmark = landmarks.get(0);


        // checking correctness with an accuracy of delta
        CloudPoint globalPoint = landmark.getCoordinates().get(0);

        double yawRad = Math.toRadians(30);
        double expectedX = (Math.cos(yawRad) * 2.0) - (Math.sin(yawRad) * 3.0) + 5.0;
        double expectedY = (Math.sin(yawRad) * 2.0) + (Math.cos(yawRad) * 3.0) + 10.0;

        assertEquals(expectedX, globalPoint.getX(), delta, "Global X is incorrect");
        assertEquals(expectedY, globalPoint.getY(), delta, "Global Y is incorrect");
    }

    @Test
    void testUpdateLandMark() {
        Pose pose = new Pose(1,0,0,45);
        fusionSlam.addPose(pose);

        List<CloudPoint> localCoordinates1 = new ArrayList<>();
        localCoordinates1.add(new CloudPoint(1.0, 1.0));
        TrackedObject trackedObject1 = new TrackedObject("1", 1, "Test Object", localCoordinates1);

        List<CloudPoint> localCoordinates2 = new ArrayList<>();
        localCoordinates2.add(new CloudPoint(3.0, 3.0));
        TrackedObject trackedObject2 = new TrackedObject("1", 1, "Test Object", localCoordinates2);

        fusionSlam.processTrackedObject(trackedObject1);
        fusionSlam.processTrackedObject(trackedObject2);

        List<LandMark> landmarks = fusionSlam.getLandmarks();
        assertEquals(1, landmarks.size(), "One landmark only should be created");
        LandMark landmark = landmarks.get(0);

        CloudPoint averagedPoint = landmark.getCoordinates().get(0);

        double yawRadians = Math.toRadians(45);
        double globalX1 = (Math.cos(yawRadians)) - (Math.sin(yawRadians));
        double globalY1 = (Math.sin(yawRadians)) + (Math.cos(yawRadians));
        double globalX2 = (Math.cos(yawRadians) * 3.0) - (Math.sin(yawRadians) * 3.0);
        double globalY2 = (Math.sin(yawRadians) * 3.0) + (Math.cos(yawRadians) * 3.0);

        double expectedX = (globalX1 + globalX2) / 2.0;
        double expectedY = (globalY1 + globalY2) / 2.0;

        assertEquals(expectedX, averagedPoint.getX(), delta, "Global X is incorrect");
        assertEquals(expectedY, averagedPoint.getY(), delta, "Global Y is incorrect");
    }

}
