package bgu.spl.mics.application.objects;

import java.util.Vector;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    private int id;
    private int time;
    private String description;
    private Vector<CloudPoint> coordinates;
}
