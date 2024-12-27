package bgu.spl.mics.application.objects;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private String error;
    //priavte last frames need to add TODO
    private  Vector<Pose> poses;

    private int systemRuntime;
    private AtomicInteger numDetectedObjects;
    private AtomicInteger numTrackedObjects;
    private int numLandMarks;
    private Vector<LandMark> landMarks;
    private transient boolean terminateClock = false;


    private static class StatisticalFolderHolder{
        private static StatisticalFolder instance = new StatisticalFolder();
    }

    private StatisticalFolder(){
        this.error = null;
        //add lastframes TODO
        this.poses = null; 
        this.systemRuntime = 0;
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandMarks = 0;
        this.landMarks = new Vector<>();
    }

    public static StatisticalFolder getInstance(){
        return StatisticalFolderHolder.instance;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setPoses(Vector<Pose> poses) {
        this.poses = poses;
    }

    public int getSystemRuntime() {
        return systemRuntime;
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public int getNumLandMarks() {
        return numLandMarks;
    }

    
    public void increaseSystemRuntime(){
        systemRuntime++;
    }
    public void addNumDetectedObjects(int toAdd){
        int oldNumDetectedObjects;
        int newNumDetectedObjects;
        do{
            oldNumDetectedObjects = numDetectedObjects.get();
            newNumDetectedObjects = oldNumDetectedObjects + toAdd;
        } while(!numDetectedObjects.compareAndSet(oldNumDetectedObjects, newNumDetectedObjects));
    }

    public void addNumTrackedObjects(int toAdd){
        int oldNumTrackedObjects;
        int newNumTrackedObjects;
        do{
            oldNumTrackedObjects = numTrackedObjects.get();
            newNumTrackedObjects = oldNumTrackedObjects + toAdd;
        } while(!numTrackedObjects.compareAndSet(oldNumTrackedObjects, newNumTrackedObjects));    
    }

    public void increaseNumLandMarks(){
        numLandMarks++;
    }

    public void addLandMarks(LandMark toAdd){
        landMarks.add(toAdd);
    }

    public boolean terminateClock(){
        return this.terminateClock;
    }
    
    public void setTerminateClock() {
        this.terminateClock = true;
    }
}
