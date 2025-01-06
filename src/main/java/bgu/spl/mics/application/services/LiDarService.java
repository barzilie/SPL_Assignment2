package bgu.spl.mics.application.services;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and
 * process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private int currentTick = 0;
    private boolean cameraFinish = false;
    private LiDarWorkerTracker lidarWT;
    private ConcurrentLinkedQueue<Future<Boolean>> lidarFutures;
    private ConcurrentLinkedQueue<TrackedObjectsEvent> eventsToSend;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service
     *                           will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker, String lidarDBPath) {
        super("LiDarService: " + LiDarWorkerTracker.getId());
        this.lidarWT = LiDarWorkerTracker;
        this.lidarWT.setLidarDataBase(LiDarDataBase.getInstance(lidarDBPath));
        this.lidarWT.checkErrorId();
        this.lidarFutures = new ConcurrentLinkedQueue<>();
        this.eventsToSend = new ConcurrentLinkedQueue<TrackedObjectsEvent>();
        initialize();
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
            Iterator<TrackedObjectsEvent> itToSend = eventsToSend.iterator();
            while(itToSend.hasNext()){
                TrackedObjectsEvent next = itToSend.next();
                if(currentTick >= next.getTimeToSend()){
                    TrackedObjectsEvent event = eventsToSend.poll();
                    Future<Boolean> f = sendEvent(event);
                    if(f!=null){
                        lidarFutures.add(f);
                    }
                }
            }

            if(eventsToSend.isEmpty() && cameraFinish){
                lidarWT.setStatus(STATUS.DOWN);
                terminate();
            }
        }  );

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated)->{ 
            if(terminated.getSenderName().equals("TimeService")){
                lidarWT.setStatus(STATUS.DOWN);
                terminate();
            }
            if(terminated.getSenderName().contains("CameraService")){
                lidarWT.decreaseNumOfCameras();
                if(lidarWT.getNumOfCameras() == 0){
                    cameraFinish = true;
                }  
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed)->{
            lidarWT.handleCrash();
            lidarWT.setStatus(STATUS.DOWN);
            terminate();
        });
        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent detectObject)->{
            StampedDetectedObjects stampedObjects = detectObject.getDetectedObjects();
            int detectionTime = stampedObjects.getTime();
            //MAYBE SHOULD BE MOVED to the handleDetectedEvent according to ahmed's answer !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            if(lidarWT.isError(detectionTime)){
                sendBroadcast(new CrashedBroadcast(this.currentTick));
                lidarWT.handleCrash();
                System.out.println(getName()+" INTERRUPT");
                Thread.currentThread().interrupt();
            }
            else{
                ConcurrentLinkedQueue<TrackedObject> trackedObjects = lidarWT.handleDetectObject(detectObject, detectionTime);

                //maybe check if relevant to send and send the event right away
                if(detectionTime + lidarWT.getFrequency()<=currentTick){
                    sendEvent(new TrackedObjectsEvent(trackedObjects, detectionTime + lidarWT.getFrequency()));
                }
                else{
                    eventsToSend.add(new TrackedObjectsEvent(trackedObjects, detectionTime + lidarWT.getFrequency()));
                }
                complete(detectObject, true);
            }
        });   
    }

}
