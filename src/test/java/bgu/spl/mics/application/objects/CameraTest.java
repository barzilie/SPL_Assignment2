package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Iterator;
import java.util.Vector;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(Lifecycle.PER_CLASS)
class CameraTest {
    private Camera camera;

    @BeforeAll
    void setUp() {
        camera = new Camera(1, 2, "test_camera");

        // Setting up input data
        DetectedObject obj1 = new DetectedObject("Wall_6", "Wallie");
        DetectedObject obj2 = new DetectedObject("TV", "Television");
        DetectedObject errorObj = new DetectedObject("ERROR", "I have a problem");

        ConcurrentLinkedQueue<DetectedObject> first = new ConcurrentLinkedQueue<>();
        first.add(obj1); first.add(obj2);
        ConcurrentLinkedQueue<DetectedObject> second = new ConcurrentLinkedQueue<>();
        second.add(errorObj);

        StampedDetectedObjects sdo1 = new StampedDetectedObjects(1, first);
        StampedDetectedObjects sdo2 = new StampedDetectedObjects(2, second);

        Vector<StampedDetectedObjects> detectedObjectsList = new Vector<>();
        detectedObjectsList.add(sdo1);
        detectedObjectsList.add(sdo2);

        camera.setDetectedObjectsList(detectedObjectsList);
    }

    @Test
    void testPrepareDataValidInput() {
        int currentTick = 1;

        // Run the method on valid data
        StampedDetectedObjects result = camera.prepareData(currentTick);

        ConcurrentLinkedQueue<DetectedObject> expectedObjects = new ConcurrentLinkedQueue<>();
        expectedObjects.add(new DetectedObject("Wall_6", "Wallie"));
        expectedObjects.add(new DetectedObject("TV", "Television"));
        ConcurrentLinkedQueue<DetectedObject> actualObjects = result.getDetectedObjectsList();

        // Verify the output
        assertNotNull(result, "prepareData should not return null for valid input");
        assertEquals(1, result.getTime(), "The time of the returned data should match the currentTick");
        assertEquals(expectedObjects.size(), actualObjects.size(), "The size of detected objects lists should match");
        
        Iterator<DetectedObject> expectedIterator = expectedObjects.iterator();
        Iterator<DetectedObject> actualIterator = actualObjects.iterator();
        while(expectedIterator.hasNext() && actualIterator.hasNext()){
            DetectedObject d1 = expectedIterator.next();
            DetectedObject d2 = actualIterator.next();
            assertEquals(d1.getId(), d2.getId());
            assertEquals(d1.getDescription(), d2.getDescription());           
        }

        // Ensure the detected objects list is updated
        assertNull(camera.getDetectedObjectsAtTime(1), "Detected objects at currentTick should be removed");
    }

    @Test
    void testPrepareDataWithError() {
        int currentTick = 2;

        // Run the method on data with an error object
        StampedDetectedObjects result = camera.prepareData(currentTick);

        // Verify the output
        assertNull(result, "prepareData should return null when error objects are present");
        assertEquals(STATUS.ERROR, camera.getStatus(), "Camera status should be set to ERROR when error objects are detected");
    }

    @Test
    void testPrepareDataNoData() {
        int currentTick = 3;

        // Run the method on a tick with no data
        StampedDetectedObjects result = camera.prepareData(currentTick);

        // Verify the output
        assertNotNull(result, "prepareData should not return null for missing data");
        assertEquals(0, result.getTime(), "The returned data should have a default time of 0 for missing data");
        assertTrue(result.getDetectedObjectsList().isEmpty(), "The returned data should have an empty detected objects list");
    }
}
