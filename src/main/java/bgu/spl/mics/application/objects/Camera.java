package bgu.spl.mics.application.objects;

import java.util.Vector;
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
    private Vector<StampedDetectedObjects> detectedObjectsList;
    private int finishTime = 0;

    public Camera(int id, int frequency, String cameraKey){
        this.id = id;
        this.frequency = frequency;
        this.camera_key = cameraKey;
        this.status = STATUS.UP;
        this.detectedObjectsList = new Vector<StampedDetectedObjects>();
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
    public Vector<StampedDetectedObjects> getDetectedObjectsList(){
        return this.detectedObjectsList;
    }

    public String toString(){
        return "camera id: "+id+ "camera freq: " + frequency + "camera key: " + camera_key;
    }

    public void setDetectedObjectsList(Vector<StampedDetectedObjects> detectedObjectsList) {
        this.detectedObjectsList = detectedObjectsList;
        for(StampedDetectedObjects SDO: detectedObjectsList){
            if(SDO.getTime()>finishTime) finishTime = SDO.getTime();
            ConcurrentLinkedQueue<DetectedObject> DOList = SDO.getDetectedObjectsList();
            for(DetectedObject DO: DOList){
                System.out.println(camera_key+" found: "+DO.getDescription());
            }


        }
    }

    public StampedDetectedObjects getDetectedObjectsAtTime(int time){
        for(StampedDetectedObjects sdo: detectedObjectsList){
            if(sdo.getTime() == time)
                return sdo;
        }
        return null;
    }

    public boolean checkErrorId (ConcurrentLinkedQueue<DetectedObject> detectedObjects){
        for(DetectedObject d: detectedObjects){
            if(d.getId().equals("ERROR")) return true;
        }
        return false; 
    }

    public Boolean safeTermination(int currentTick){
        if(currentTick>finishTime){
            setStatus(STATUS.DOWN);
            System.out.println("CAMERA TERMINATED IN: " + currentTick);
            return true;
        }
        return false;
    }

    public StampedDetectedObjects prepareData(int currentTick){
        for(StampedDetectedObjects s: detectedObjectsList){
            if(s.getTime() == currentTick){
                if(checkErrorId(s.getDetectedObjectsList())){
                    StatisticalFolder.getInstance().setError("camera " + getId() + " disconnected");
                    System.out.println("INTERRUPTED " + Thread.currentThread().getName() + "TIME: " + s.getTime() );
                    setStatus(STATUS.ERROR);
                    return null;
                }
                else{
                    StatisticalFolder.getInstance().addNumDetectedObjects(s.getDetectedObjectsList().size());
                    StampedDetectedObjects output = s;
                    detectedObjectsList.remove(s);
                    return output;
                }
            }
        }
        return new StampedDetectedObjects();
    }
            
}
