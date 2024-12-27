package bgu.spl.mics.application.services;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.Pose;


/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {

    private int currentTick = 0;
    private FusionSlam fusionSlam;
    private StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();
    private int numOfSensors = 0;

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("Fusion Slam");
        this.fusionSlam = FusionSlam.getInstance();
        initialize();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, this::handleTick);
        subscribeBroadcast(TerminatedBroadcast.class, this::handleTerminated);
        subscribeBroadcast(CrashedBroadcast.class, this::handleCrashed);
        subscribeEvent(TrackedObjectsEvent.class, this::handleTrackedObject);       
        subscribeEvent(PoseEvent.class, this::handlePose);

    }


    //callback function for TickBroadcasts
    protected void handleTick(TickBroadcast tick){ 
        currentTick++;


    }    

    //callback function for TerminatedBroadcast
    private void handleTerminated(TerminatedBroadcast terminated){ 
        if(terminated.getSenderName().equals("TimeService")){
            terminate();
        }
        else{
            numOfSensors--;
            if(numOfSensors == 0){
                statisticalFolder.setTerminateClock();
                terminate();
            }
        }
        //add to statistics and do the termination stuff
    }

    //callback function for CrashedBroadcast
    private void handleCrashed(CrashedBroadcast crashed){
        //add to statistics and do the termination stuff and more crashed things page 23
        statisticalFolder.setTerminateClock();
        terminate();
    }

    private void handleTrackedObject(TrackedObjectsEvent trackedObjects){
        for(TrackedObject object: trackedObjects.getTrackedObjects()){
            ConcurrentLinkedQueue<CloudPoint> ObjectGlobalCoordinates = fusionSlam.convertToGlobal(object);
            LandMark toRefine = fusionSlam.retrieveLandmark(object.getId());
            if(toRefine == null){
                LandMark toAdd = new LandMark(object.getId(),object.getDescription(), ObjectGlobalCoordinates);
                this.fusionSlam.addLandMark(toAdd);
                statisticalFolder.increaseNumLandMarks();
                statisticalFolder.addLandMarks(toAdd);
            }
            else{
                 toRefine.setCoordinates(fusionSlam.refineCoordinates(toRefine.getCoordinates(), ObjectGlobalCoordinates));
            }
        }
        complete(trackedObjects, true);

    }

    private void handlePose(PoseEvent pose){
        fusionSlam.addPose(pose.getPose());
        complete(pose, true);
    }

    public void incrementNumOfSensors() {
        numOfSensors = numOfSensors+1;
    }
}
