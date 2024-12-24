package bgu.spl.mics.application.objects;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 * 
 */


public class Camera {
    private int id;
    private int frequency;
    private String cameraKey;
    private STATUS status;
    private ConcurrentLinkedQueue<StampedDetectedObjects> detectedObjectsList;

    public Camera(int id, int frequency, String cameraKey){
        this.id = id;
        this.detectedObjectsList = new ConcurrentLinkedQueue<>();
        //...
    }
    
    public getObjectsAtTime(){
        
    }
}
