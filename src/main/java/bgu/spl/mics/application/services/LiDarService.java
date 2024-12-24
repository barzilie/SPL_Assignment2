package bgu.spl.mics.application.services;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
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
    private int currentTick;
    private LiDarWorkerTracker lidarWT; 

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("Change_This_Name");
        // TODO Implement this
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
        //create TrackedObject for every detectedObject, create list of cloudpoints to it, and assign it to the TrackedObject/ then, create TrackedObjectsEvent and send it. 
        ConcurrentLinkedQueue<TrackedObject> trackedObjects = new ConcurrentLinkedQueue<>();
        StampedDetectedObjects stampedObjects = detectObject.getDetectedObjects();
        int timeOfDetection = stampedObjects.getTime();
        for(DetectedObject object: stampedObjects.getDetectedObjectsList()){
            String objectId = object.getId();
            String ObjectDescription = object.getDescription();
            //add field of LidarDB from worker or service, reach it and find coordinates
            //then, create each new Tracked object to the trackedObjects list
            //finally pass the list to 1. the worker 2. the TrackedObjectEvent and send it
        }



    }
}
