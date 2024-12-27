package bgu.spl.mics.application.services;

import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.STATUS;
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
        subscribeBroadcast(TickBroadcast.class, this::handleTick);
    }

    //callback function for TickBroadcasts
    protected void handleTick(TickBroadcast tick){ 
        currentTick++;
        Future<Boolean> f = sendEvent(new PoseEvent(this.gpsimu.retrievePose(currentTick)));
        if(f == null){
            if(StatisticalFolder.getInstance().getError() == null){
                StatisticalFolder.getInstance().setPoses(this.gpsimu.errorPoseList(StatisticalFolder.getInstance().getSystemRuntime()));
            }
            terminate();
        }
        poseFutures.add(f);

    } 
}
