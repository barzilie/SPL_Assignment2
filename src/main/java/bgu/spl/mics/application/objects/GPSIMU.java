package bgu.spl.mics.application.objects;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    private ConcurrentLinkedQueue<Pose> poseList;

    public Pose retrievePose(int time){
        for(Pose p: poseList){
            if(p.getTime() == time) return p;
        }
        return null;
    }

}
