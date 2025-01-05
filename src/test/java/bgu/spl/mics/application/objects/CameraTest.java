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

    // Test the method on valid data

    /* Pre-condition:
    1. The camera is initialized with a list of StampedDetectedObjects, where each entry matches a tick:
    detectedObjectsList != null
    detectedObjectsList.get(i).getTime()<detectedObjectsList.get(i+1).getTime()
    2. The list does not contain any "error" objects:
    forEach i: detectedObjectsList.get(i).getId() != error
    */ 

    /* Post-condition:
    1. The return value is not null.
    output != null
    2. The returned StampedDetectedObjects object should match the provided currentTick and contain the correct detected objects.
    output.getTime() = currentTick
    We mark index i such that:
    output = detectedObjectsList.get(i), when @pre(detectedObjectsList.get(i).getTime()) = currentTick
    3. The list of detected objects for the current tick should be removed from the camera's detectedObjectsList.
    detectedObjectsList.contains(@pre(detectedObjectsList.get(i)) = false
     */ 

    // Invariant: The camera's detected objects list should remain consistent for all ticks except the one being processed.
    // detectedObjectsList = @pre(detectedObjectsList.remove(i))



    @Test
    void testPrepareDataValidInput() {

        int currentTick = 1;

        //original DetectedObjectsList to compare
        Vector<StampedDetectedObjects> originDetectedObjectsList = new Vector<>(camera.getDetectedObjectsList());
        originDetectedObjectsList.remove(camera.getDetectedObjectsAtTime(1));

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

        // Ensure invariant
        assertNull(camera.getDetectedObjectsAtTime(1), "Detected objects at currentTick should be removed");
        assertEquals(camera.getDetectedObjectsList().size(), originDetectedObjectsList.size());
    }


    // Test the method on data with an error object

    /* Pre-condition:
    1. The camera is initialized with a list of StampedDetectedObjects, where each entry matches ticks increasingly:
    detectedObjectsList.get(i).getTime()<detectedObjectsList.get(i+1).getTime()
    2. The list contains "error" id object at the tested tick:
    We mark index j such that: detectedObjectsList.get(j).getTime() == currentTick
    detectedObjectsList.get(j).getId() == "Error" 
     */ 

    /* Post-condition:
    1. The method return value is null.
    output == null
    2. The camera's status in ERROR
    camera.getStatus == ERROR
     */ 

    // Invariant: The camera's detected objects list should remain unchanged :
    //@pre(detectedObjectsList) == detectedObjectsList


    @Test
    void testPrepareDataWithError() {

        Vector<StampedDetectedObjects> originDetectedObjectsList = camera.getDetectedObjectsList();

        int currentTick = 2;

        StampedDetectedObjects result = camera.prepareData(currentTick);

        // Verify the output
        assertNull(result, "prepareData should return null when error objects are present");
        assertEquals(STATUS.ERROR, camera.getStatus(), "Camera status should be set to ERROR when error objects are detected");

        // Ensure invariant
        assertEquals(camera.getDetectedObjectsList().size(), originDetectedObjectsList.size());
    }


    // Test the method on a tick with no data

    /* Pre-condition:
    1. The camera is initialized with a list of StampedDetectedObjects with no matching data for the given tick.
    We mark index i such that:
    detectedObjectsList.get(i).getTime() < detectedObjectsList.get(i).getTime()-1

    /* Post-condition:
    1. The method will not return null:
    output != null
    2. The method returns a new default StampedDetectedObjects, with an empty detectedObjects list, and time = 0:
    output.getDetectedObjectsList().isEmpty == true 
    */ 
    // Invariant: The camera's detected objects list should remain unchanged 
    //@pre(detectedObjectsList) == detectedObjectsList


    @Test
    void testPrepareDataNoData() {

        Vector<StampedDetectedObjects> originDetectedObjectsList = camera.getDetectedObjectsList();

        int currentTick = 3;

        StampedDetectedObjects result = camera.prepareData(currentTick);

        // Verify the output
        assertNotNull(result, "prepareData should not return null for missing data");
        assertEquals(0, result.getTime(), "The returned data should have a default time of 0 for missing data");
        assertTrue(result.getDetectedObjectsList().isEmpty(), "The returned data should have an empty detected objects list");

        
        // Ensure invariant
        assertEquals(camera.getDetectedObjectsList().size(), originDetectedObjectsList.size());
    }
}
