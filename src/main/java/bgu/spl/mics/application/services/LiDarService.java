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
import bgu.spl.mics.application.objects.LastFrames;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
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
        this.lidarWT.setLidarDataBase(LiDarDataBase.getInstance(lidarDBPath));
        this.lidarWT.initializeFinishTime();
        this.lidarWT.checkErrorId();
        this.lidarFutures = new ConcurrentLinkedQueue<>();
        this.eventsToSend = new ConcurrentLinkedQueue<TrackedObjectsEvent>();
        initialize();
        System.out.println("Lidar: " + LiDarWorkerTracker.getId());
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick)->{
            currentTick++; 
            if(lidarWT.isFinish(currentTick)){
                terminate();
            }
            else if(lidarWT.isError(currentTick)){
                sendBroadcast(new CrashedBroadcast(this.currentTick));
                Thread.currentThread().interrupt();
            }
            else if(!eventsToSend.isEmpty() && currentTick == eventsToSend.peek().getTimeToSend()){
                TrackedObjectsEvent event = eventsToSend.poll();
                lidarFutures.add(sendEvent(event));
            }
        }  );

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated)->{ 
            //add to statistics and do the termination stuff
            if(terminated.getSenderName().equals("TimeService")){
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed)->{
            lidarWT.handleCrash();
            terminate();
        });
        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent detectObject)->{
            StampedDetectedObjects stampedObjects = detectObject.getDetectedObjects();
            int detectionTime = stampedObjects.getTime();
            ConcurrentLinkedQueue<TrackedObject> trackedObjects = lidarWT.handleDetectObject(detectObject, detectionTime);
    
            eventsToSend.add(new TrackedObjectsEvent(trackedObjects, detectionTime + lidarWT.getFrequency()));
            complete(detectObject, true);
        });   
    }

}
