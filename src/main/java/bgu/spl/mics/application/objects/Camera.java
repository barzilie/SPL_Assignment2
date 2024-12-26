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

    public Camera(int id, int frequency, String cameraKey, ConcurrentLinkedQueue<StampedDetectedObjects> detectedObjectsList){
        this.id = id;
        this.frequency = frequency;
        this.cameraKey = cameraKey;
        this.status = STATUS.UP;
        this.detectedObjectsList = detectedObjectsList;
        for(StampedDetectedObjects SDO: detectedObjectsList){
            ConcurrentLinkedQueue<DetectedObject> DOList = SDO.getDetectedObjectsList();
            for(DetectedObject DO: DOList){
                System.out.println(cameraKey+"found:"+DO.getDescription());
            }


        }
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getCameraKey() {
        return cameraKey;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public STATUS getStatus() {
        return status;
    }

    public void addDetectedObjects(StampedDetectedObjects stampedDetectedObjects){
        detectedObjectsList.add(stampedDetectedObjects);
    }
    public ConcurrentLinkedQueue<StampedDetectedObjects> getDetectedObjectsList(){
        return this.detectedObjectsList;
    }

    public String toString(){
        return "camera id: "+id+ "camera freq: " + frequency + "camera key: " + cameraKey;
    }
            
}
