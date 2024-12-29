package bgu.spl.mics.application.objects;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private int time;
    private ConcurrentLinkedQueue<DetectedObject> detectedObjects;

    public StampedDetectedObjects(){
        this.time = 0;
        this.detectedObjects = new ConcurrentLinkedQueue<>();
    }

    public StampedDetectedObjects(int time, ConcurrentLinkedQueue<DetectedObject> detectedObjects){
        this.time = time;
        this.detectedObjects = detectedObjects;
    }

    public int getTime(){
        return this.time;
    }
    
    public ConcurrentLinkedQueue<DetectedObject> getDetectedObjectsList(){
        return this.detectedObjects;
    }


}


