package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.application.messages.DetectObjectsEvent;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private int id;
    private int frequency;
    private STATUS status;
    private int errorTime = -1;
    private int numOfCameras;
    private LiDarDataBase lidarDataBase;
    private ConcurrentLinkedQueue<TrackedObject> lastTrackedObjects;

    public LiDarWorkerTracker(int id, int frequency){
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new ConcurrentLinkedQueue<>();
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public STATUS getStatus() {
        return status;
    }

    public void addTrackedObjects(TrackedObject trackedObject){
        lastTrackedObjects.add(trackedObject);
    }
    public ConcurrentLinkedQueue<TrackedObject> getTrackedObjectsList(){
        return lastTrackedObjects;
    }

    public void setLastTrackedObjects(ConcurrentLinkedQueue<TrackedObject> trackedObjects){
        this.lastTrackedObjects = trackedObjects;
    }

    public void setNumOfCameras(int numOfCameras) {
        this.numOfCameras = numOfCameras;
    }

    public void decreaseNumOfCameras() {
        this.numOfCameras = numOfCameras-1;
    }

    public int getNumOfCameras() {
        return numOfCameras;
    }

    public String toString(){
        return "lidar id: "+id+ "lidar freq: " + frequency;
    }

    public void handleCrash(){
        LastFrames.getInstance().addLidarLastFrames("lidar: "+ getId(), getTrackedObjectsList());

    }

    public void setLidarDataBase(LiDarDataBase lidarDataBase) {
        this.lidarDataBase = lidarDataBase;
    }

    public void checkErrorId(){
        ConcurrentLinkedQueue<StampedCloudPoints> DB = lidarDataBase.getCloudPoints();
        for(StampedCloudPoints scp: DB){
           if(scp.getId().equals("ERROR")){
            this.errorTime = scp.getTime();
            break;
           }
        }
    }

    public boolean isError(int currentTick){
        if(errorTime == currentTick){
            System.out.println("LIDAR ERROR FOUND IN: " + currentTick);
            StatisticalFolder.getInstance().setError("lidar " + getId() + " disconnected");
            StatisticalFolder.getInstance().setFaultySensor("lidar " + getId());
            setStatus(STATUS.ERROR);
            return true;
        }
        return false;
    }
    
    public ConcurrentLinkedQueue<TrackedObject> handleDetectObject(DetectObjectsEvent detectObject, int detectionTime){
        ConcurrentLinkedQueue<TrackedObject> trackedObjects = new ConcurrentLinkedQueue<>();
        StampedDetectedObjects stampedObjects = detectObject.getDetectedObjects();
        //NOTICE: i changed the time here a little bit so it will capture also earlier object like BIN - time 9 measured in time 8
        for(DetectedObject object: stampedObjects.getDetectedObjectsList()){
            String objectId = object.getId();
            String ObjectDescription = object.getDescription();
            //THE actual check for cloudpoints is in retrieve function
            StampedCloudPoints stampedCP = this.lidarDataBase.retrieveCloudPoint(detectionTime, objectId);
            Vector<CloudPoint> coordinates = new Vector<CloudPoint>();
            for(List<Double> listCP: stampedCP.getCloudPoints()){
                coordinates.add(new CloudPoint(listCP.get(0),listCP.get(1)));
            }
            trackedObjects.add(new TrackedObject(objectId, detectionTime, ObjectDescription, coordinates));
        }
        StatisticalFolder.getInstance().addNumTrackedObjects(trackedObjects.size());
        setLastTrackedObjects(trackedObjects);
        return trackedObjects;
    }




}
