package bgu.spl.mics;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;

class CameraTest {

    private ArrayList<StampedDetectedObjects> normalInput;
    private ArrayList<StampedDetectedObjects> errorInput;

    @BeforeEach
    void setUp() {
        normalInput = new ArrayList<>();
        ArrayList<DetectedObject> objList1 = new ArrayList<>();
        ArrayList<DetectedObject> objList2 = new ArrayList<>();
        objList1.add(new DetectedObject("Desk_1", "Desk"));
        objList2.add(new DetectedObject("Chair_1", "Chair"));
        normalInput.add(new StampedDetectedObjects(5, objList1));
        normalInput.add(new StampedDetectedObjects(10, objList2));

        errorInput = new ArrayList<>();
        ArrayList<DetectedObject> errorObjects = new ArrayList<>();
        errorObjects.add(new DetectedObject("Desk_1", "Desk"));
        errorObjects.add(new DetectedObject("ERROR", "Camera was spray painted"));
        errorInput.add(new StampedDetectedObjects(3, new ArrayList<>()));
        errorInput.add(new StampedDetectedObjects(5, errorObjects));
    }

    @Test
    void testNormalInput() {
        final int frequency = 5;
        Camera camera = new Camera(1, frequency, normalInput);

        int objTime1 = normalInput.get(0).getTime();
        StampedDetectedObjects result = camera.detect(objTime1 + frequency);

        assertNotNull(result, "Detection should not be null");
        assertEquals(objTime1, result.getTime(), "Object's time should be currentTime - freq");
        assertEquals(STATUS.UP, camera.getStatus(), "Camera status should remain up, not all objects detected");

        int objTime2 = normalInput.get(1).getTime();
        result = camera.detect(normalInput.get(1).getTime() + frequency);

        assertNotNull(result, "Detection should not be null");
        assertEquals(objTime2, result.getTime(), "Object's time should be currentTime - freq");
        assertEquals(STATUS.DOWN, camera.getStatus(), "Camera status should be down after detecting all objects");
    }

    @Test
    void testErrorInput() {
        final int frequency = 2;
        Camera camera = new Camera(1, frequency, errorInput);

        int objTime1 = errorInput.get(0).getTime();
        StampedDetectedObjects result = camera.detect(objTime1 + frequency);

        assertNotNull(result, "Detection should not be null");
        assertEquals(STATUS.UP, camera.getStatus(), "Camera status should remain up, not all objects detected");

        int objTime2 = errorInput.get(1).getTime();
        result = camera.detect(objTime2 + frequency);

        assertNotNull(result, "Detection should not be null");
        assertEquals(STATUS.ERROR, camera.getStatus(), "Camera status should change to ERROR after detecting an error object");
    }

}