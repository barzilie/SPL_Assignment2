package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    private int time;
    private String id;
    private ConcurrentLinkedQueue<List<Double>> cloudPoints;

    public String getId(){
        return this.id;
    }
    
    public int getTime(){
        return this.time;
    }

    public ConcurrentLinkedQueue<List<Double>> getCloudPoints(){
        return this.cloudPoints;
    }
}
