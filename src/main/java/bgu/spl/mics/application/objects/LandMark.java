package bgu.spl.mics.application.objects;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    private String id;
    private String description;
    private ConcurrentLinkedQueue<CloudPoint> coordinates;

    public LandMark(String id, String description, ConcurrentLinkedQueue<CloudPoint> coordinates){
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    public String getId(){
        return this.id;
    }

    public void setCoordinates(ConcurrentLinkedQueue<CloudPoint> refinedCoordinates){
        this.coordinates = refinedCoordinates;

    }

    public ConcurrentLinkedQueue<CloudPoint> getCoordinates(){
        return this.coordinates;

    }

    //for testing 
    public String getDescription() {
        return this.description;
    }
    
}
