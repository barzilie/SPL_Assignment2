package bgu.spl.mics.application.objects;

import java.util.Vector;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    private String id;
    private int time;
    private String description;
    private Vector<CloudPoint> coordinates;

    public TrackedObject(String id, int time, String description, Vector<CloudPoint> coordinates){
        this.id = id;
        this.time = time;
        this.description = description;
        this.coordinates = coordinates;
    }

    public Vector<CloudPoint> getCoordinates(){
        return this.coordinates;
    }

    public int getTime(){
        return this.time;
    }

    public String getId(){
        return this.id;
    }
    public String getDescription(){
        return this.description;
    }
}
