package bgu.spl.mics.application.objects;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private int id;
    private int frequency;
    private STATUS status;
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

    public String toString(){
        return "lidar id: "+id+ "lidar freq: " + frequency;
    }

}
