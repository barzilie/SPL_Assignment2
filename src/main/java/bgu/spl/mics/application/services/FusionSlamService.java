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
import bgu.spl.mics.application.objects.LastFrames;
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

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick)->{ 
            currentTick++;
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated)->{
            if(terminated.getSenderName().equals("TimeService")){
                terminate();
            }
            else{
                numOfSensors--;
                if(numOfSensors == 0){
                    this.fusionSlam.setTerminateClock();
                    terminate();
                }
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed)->{
            this.fusionSlam.updateStatisticsBeforeCrash(crashed);
            terminate();
        });

        subscribeEvent(TrackedObjectsEvent.class, (TrackedObjectsEvent trackedObjects)->{
            this.fusionSlam.handleTrackedObject(trackedObjects);
            complete(trackedObjects, true);
        }); 

        subscribeEvent(PoseEvent.class, (PoseEvent pose)->{
            fusionSlam.addPose(pose.getPose());
            complete(pose, true);
        });

    }

    public void incrementNumOfSensors() {
        numOfSensors = numOfSensors+1;
    }
}
