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
    private String camera_key;
    private STATUS status = STATUS.UP;
    private ConcurrentLinkedQueue<StampedDetectedObjects> detectedObjectsList;

    public Camera(int id, int frequency, String cameraKey){
        this.id = id;
        this.frequency = frequency;
        this.camera_key = cameraKey;
        this.status = STATUS.UP;
        this.detectedObjectsList = new ConcurrentLinkedQueue<StampedDetectedObjects>();

    }



    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getCameraKey() {
        return camera_key;
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
        return "camera id: "+id+ "camera freq: " + frequency + "camera key: " + camera_key;
    }

    public void setDetectedObjectsList(ConcurrentLinkedQueue<StampedDetectedObjects> detectedObjectsList) {
        this.detectedObjectsList = detectedObjectsList;

        for(StampedDetectedObjects SDO: detectedObjectsList){
            ConcurrentLinkedQueue<DetectedObject> DOList = SDO.getDetectedObjectsList();
            for(DetectedObject DO: DOList){
                System.out.println(camera_key+"found:"+DO.getDescription());
            }


        }
    }
            
}
