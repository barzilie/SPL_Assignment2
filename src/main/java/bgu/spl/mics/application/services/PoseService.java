package bgu.spl.mics.application.services;

import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private int currentTick = 0;
    private GPSIMU gpsimu;
    private ConcurrentLinkedQueue<Future<Boolean>> poseFutures;


    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
        this.poseFutures = new ConcurrentLinkedQueue<>();
        initialize();
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick)->{ 
            currentTick++;
            Pose p = this.gpsimu.retrievePose(currentTick);
            if(p == null){
                terminate();
            }
            else{
                Future<Boolean> f = sendEvent(new PoseEvent(p));
                if(gpsimu.failedTosendEvent(f)){
                    terminate();
                }
                else{
                    poseFutures.add(f);
                }
            }
        });

        //added because wasnt terminating without them
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated)->{ 
            if(terminated.getSenderName().equals("Fusion Slam")){
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed)->{
            StatisticalFolder.getInstance().setPoses(this.gpsimu.errorPoseList(StatisticalFolder.getInstance().getSystemRuntime()));
            terminate();
        });
    }

}
