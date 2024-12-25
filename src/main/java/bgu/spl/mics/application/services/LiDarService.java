package bgu.spl.mics.application.services;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private int currentTick = 0;
    private LiDarWorkerTracker lidarWT; 
    private LiDarDataBase lidarDataBase;
    private ConcurrentLinkedQueue<Future<Boolean>> lidarFutures;
    private ConcurrentLinkedQueue<TrackedObjectsEvent> eventsToSend;


    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker, String lidarDBPath) {
        super("LiDarService: "+ LiDarWorkerTracker.getId());
        this.lidarWT = LiDarWorkerTracker;
        this.lidarDataBase = LiDarDataBase.getInstance(lidarDBPath);
        this.lidarFutures = new ConcurrentLinkedQueue<>();
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, this::handleTick);
        subscribeBroadcast(TerminatedBroadcast.class, this::handleTerminated);
        subscribeBroadcast(CrashedBroadcast.class, this::handleCrashed);
        subscribeEvent(DetectObjectsEvent.class, this::handleDetectObject);   
    }

    //callback function for TickBroadcasts
    protected void handleTick(TickBroadcast tick){
        currentTick++; 
        if(!eventsToSend.isEmpty() && currentTick == eventsToSend.peek().getTimeToSend()){
            TrackedObjectsEvent event = eventsToSend.poll();
            lidarFutures.add(sendEvent(event));
            this.lidarWT.setLastTrackedObjects(event.getTrackedObjects());
        }
    }    

    //callback function for TerminatedBroadcast
    private void handleTerminated(TerminatedBroadcast terminated){ 
        //add to statistics and do the termination stuff
        terminate();
        lidarWT.setStatus(STATUS.DOWN);
    }

    //callback function for CrashedBroadcast
    private void handleCrashed(CrashedBroadcast crashed){
        //add to statistics and do the termination stuff and more crashed things page 23
        terminate();
        lidarWT.setStatus(STATUS.DOWN);
    }

    private void handleDetectObject(DetectObjectsEvent detectObject){
        ConcurrentLinkedQueue<TrackedObject> trackedObjects = new ConcurrentLinkedQueue<>();
        StampedDetectedObjects stampedObjects = detectObject.getDetectedObjects();
        int timeOfDetection = stampedObjects.getTime();
        for(DetectedObject object: stampedObjects.getDetectedObjectsList()){
            String objectId = object.getId();
            String ObjectDescription = object.getDescription();
            StampedCloudPoints stampedCP = this.lidarDataBase.retrieveCloudPoint(timeOfDetection, objectId);
            Vector<CloudPoint> coordinates = new Vector<CloudPoint>();
            for(List<Double> listCP: stampedCP.getCloudPoints()){
                coordinates.add(new CloudPoint(listCP.get(0),listCP.get(1)));
            }
            trackedObjects.add(new TrackedObject(objectId, timeOfDetection, ObjectDescription, coordinates));
            eventsToSend.add(new TrackedObjectsEvent(trackedObjects, timeOfDetection + lidarWT.getFrequency()));
            complete(detectObject, coordinates);
        }
    }
}
