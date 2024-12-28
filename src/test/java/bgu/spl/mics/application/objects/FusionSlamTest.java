package bgu.spl.mics.application.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import bgu.spl.mics.application.messages.TrackedObjectsEvent;

@TestInstance(Lifecycle.PER_CLASS)
class FusionSlamTest {

    private FusionSlam fusionSlam;
    public TrackedObjectsEvent trackedObjectEvent;

    @BeforeAll
    public void setUp() {
        fusionSlam = FusionSlam.getInstance();
        fusionSlam.addPose(new Pose(3, 3, 80, 1));
        fusionSlam.addPose(new Pose(4, 1, 100, 2));
    }

    @Test
    public void testHandleTrackedObject_newLandmark() {
        //initialization
        Vector<CloudPoint> coordinatesB = new Vector<>();
        coordinatesB.add(new CloudPoint(0, 1));
        coordinatesB.add(new CloudPoint(10, 4));  
        ConcurrentLinkedQueue<TrackedObject> trackedObjects = new ConcurrentLinkedQueue<>();
        TrackedObject toB = new TrackedObject("Bed_2", 1, "sleep", coordinatesB);
        trackedObjects.add(toB);
        Vector<CloudPoint> coordinatesC = new Vector<>();
        coordinatesB.add(new CloudPoint(1, 0));
        coordinatesB.add(new CloudPoint(4, 10));  
        TrackedObject toC = new TrackedObject("table_2", 1, "sleep", coordinatesB);
        trackedObjects.add(toC);
        this.trackedObjectEvent = new TrackedObjectsEvent(trackedObjects, 1);
        fusionSlam.handleTrackedObject(trackedObjectEvent);

        //expected First Object result calculation:
        ConcurrentLinkedQueue<CloudPoint> globalCoordinatesB = new ConcurrentLinkedQueue<CloudPoint>();
        Pose p = new Pose(3, 3, 80, 1);
        double x_robot = p.getX();
        double y_robot = p.getY();
        double radDegree = (p.getYaw()*Math.PI)/180;
        double cos = Math.cos(radDegree);
        double sin = Math.sin(radDegree);
        for(CloudPoint cp: toB.getCoordinates()){
            double x_global = cos*cp.getX() - sin*cp.getY() + x_robot;
            double y_global = sin*cp.getX() + cos*cp.getY() + y_robot;
            globalCoordinatesB.add(new CloudPoint(x_global, y_global));
        }


        //first Object Testing
        LandMark retrievedLandmarkB = fusionSlam.retrieveLandmark("Bed_2");
        assertNotNull(retrievedLandmarkB);
        assertEquals(toB.getId(), retrievedLandmarkB.getId());
        assertEquals(toB.getDescription(), retrievedLandmarkB.getDescription());
        Iterator<CloudPoint> resultCoordinatesIterator = globalCoordinatesB.iterator();
        Iterator<CloudPoint> landMarkIteratorB = retrievedLandmarkB.getCoordinates().iterator();
        while(resultCoordinatesIterator.hasNext()&& landMarkIteratorB.hasNext()){
            CloudPoint resultCP = resultCoordinatesIterator.next();
            CloudPoint lamdMarkCP = landMarkIteratorB.next();
            assertEquals(resultCP.getX(), lamdMarkCP.getX());
            assertEquals(resultCP.getY(), lamdMarkCP.getY());
        }

        //expected Second Object result calculation:
        ConcurrentLinkedQueue<CloudPoint> globalCoordinatesC = new ConcurrentLinkedQueue<CloudPoint>();
        Pose pC = new Pose(3, 3, 80, 1);
        double x_robotC = pC.getX();
        double y_robotC = pC.getY();
        double radDegreeC = (pC.getYaw()*Math.PI)/180;
        double cosC = Math.cos(radDegreeC);
        double sinC = Math.sin(radDegreeC);
        for(CloudPoint cp: toC.getCoordinates()){
            double x_global = cosC*cp.getX() - sinC*cp.getY() + x_robotC;
            double y_global = sinC*cp.getX() + cosC*cp.getY() + y_robotC;
            globalCoordinatesC.add(new CloudPoint(x_global, y_global));
        }

        //second Object Testing
        LandMark retrievedLandmarkC = fusionSlam.retrieveLandmark("table_2");
        assertNotNull(retrievedLandmarkC);
        assertEquals(toC.getId(), retrievedLandmarkC.getId());
        assertEquals(toC.getDescription(), retrievedLandmarkC.getDescription());
        Iterator<CloudPoint> resultCoordinatesIteratorC = globalCoordinatesC.iterator();
        Iterator<CloudPoint> landMarkIteratorC = retrievedLandmarkC.getCoordinates().iterator();
        while(resultCoordinatesIteratorC.hasNext()&& landMarkIteratorC.hasNext()){
            CloudPoint resultCP = resultCoordinatesIteratorC.next();
            CloudPoint lamdMarkCP = landMarkIteratorC.next();
            assertEquals(resultCP.getX(), lamdMarkCP.getX());
            assertEquals(resultCP.getY(), lamdMarkCP.getY());        }


    }

    @Test
    public void testHandleTrackedObject_UpdateExistingLandmark() {

        //initialization
        ConcurrentLinkedQueue<CloudPoint> coordinates = new ConcurrentLinkedQueue<>();
        coordinates.add(new CloudPoint(1, 2));
        coordinates.add(new CloudPoint(2, 5));
        coordinates.add(new CloudPoint(2, 3));
        LandMark lm = new LandMark("refrigerator_1", "makes cold", coordinates);
        fusionSlam.addLandMark(lm);
        Vector<CloudPoint> coordinatesA = new Vector<>();
        coordinatesA.add(new CloudPoint(3, 4));
        coordinatesA.add(new CloudPoint(5, 6));
        coordinatesA.add(new CloudPoint(6, 6));  
        ConcurrentLinkedQueue<TrackedObject> trackedObjects = new ConcurrentLinkedQueue<>();
        TrackedObject toA = new TrackedObject("refrigerator_1", 2, "makes cold", coordinatesA);
        trackedObjects.add(toA);
        this.trackedObjectEvent = new TrackedObjectsEvent(trackedObjects, 1);

        //expected Object result calculation:
        ConcurrentLinkedQueue<CloudPoint> globalCoordinatesA = new ConcurrentLinkedQueue<CloudPoint>();
        Pose pA = new Pose(4, 1, 100, 2);
        double x_robotA = pA.getX();
        double y_robotA = pA.getY();
        double radDegreeA = (pA.getYaw()*Math.PI)/180;
        double cosA = Math.cos(radDegreeA);
        double sinA = Math.sin(radDegreeA);
        for(CloudPoint cp: toA.getCoordinates()){
            double x_global = cosA*cp.getX() - sinA*cp.getY() + x_robotA;
            double y_global = sinA*cp.getX() + cosA*cp.getY() + y_robotA;
            globalCoordinatesA.add(new CloudPoint(x_global, y_global));
        }

        ConcurrentLinkedQueue<CloudPoint> resultCoordinatesA = new ConcurrentLinkedQueue<CloudPoint>();
        LandMark retrievedLandmarkA = fusionSlam.retrieveLandmark("refrigerator_1");
        Iterator<CloudPoint> globalCoordinatesIteratorA = globalCoordinatesA.iterator();
        Iterator<CloudPoint> landMarkIteratorA = retrievedLandmarkA.getCoordinates().iterator();
        while(globalCoordinatesIteratorA.hasNext()&& landMarkIteratorA.hasNext()){
            CloudPoint cpLM = landMarkIteratorA.next();
            CloudPoint cpGC = globalCoordinatesIteratorA.next();
            resultCoordinatesA.add(new CloudPoint((cpLM.getX()+cpGC.getX())/2, (cpLM.getY()+cpGC.getY())/2));

        }


        //function run
        fusionSlam.handleTrackedObject(trackedObjectEvent);
        retrievedLandmarkA = fusionSlam.retrieveLandmark("refrigerator_1");
        
        //Object result testing
        assertNotNull(retrievedLandmarkA);
        assertEquals(toA.getId(), retrievedLandmarkA.getId());
        assertEquals(toA.getDescription(), retrievedLandmarkA.getDescription());

        Iterator<CloudPoint> resultCoordinatesAIterator = resultCoordinatesA.iterator();
        Iterator<CloudPoint> landMarkIteratorAafter = retrievedLandmarkA.getCoordinates().iterator();
        while(resultCoordinatesAIterator.hasNext()&& landMarkIteratorAafter.hasNext()){
            CloudPoint resultCP = resultCoordinatesAIterator.next();
            CloudPoint lamdMarkCP = landMarkIteratorAafter.next();
            assertEquals(resultCP.getX(), lamdMarkCP.getX());
            assertEquals(resultCP.getY(), lamdMarkCP.getY());            }
    }
}
