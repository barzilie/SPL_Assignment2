package bgu.spl.mics.application.objects;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {

    //change the fields a bit according to ver1.3.1
    private String Error;
    private String faultySensor;
    private LastFrames lastFrames;
    private  Vector<Pose> poses;

    private AtomicInteger systemRuntime;
    private AtomicInteger numDetectedObjects;
    private AtomicInteger numTrackedObjects;
    private int numLandMarks;
    private Vector<LandMark> landMarks;
    private transient boolean terminateClock = false;


    private static class StatisticalFolderHolder{
        private static StatisticalFolder instance = new StatisticalFolder();
    }

    private StatisticalFolder(){
        this.Error = null;
        this.lastFrames = null;
        this.poses = null;

        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandMarks = 0;
        this.landMarks = new Vector<>();
    }

    public static StatisticalFolder getInstance(){
        return StatisticalFolderHolder.instance;
    }

    public String getError() {
        return this.Error;
    }

    public void setError(String error) {
        this.Error = error;
    }

    public void setFaultySensor(String faultySensor) {
        this.faultySensor = faultySensor;
    }

    public void setPoses(Vector<Pose> poses) {
        this.poses = poses;
    }

    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    public void setSystemRuntime(int crashTime) {
        int oldTime;
        do{
            oldTime = systemRuntime.get();
        } while(!systemRuntime.compareAndSet(oldTime, crashTime));
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
        int oldTime;
        int newTime;
        do{
            oldTime = systemRuntime.get();
            newTime = oldTime + 1;
        } while(!systemRuntime.compareAndSet(oldTime, newTime));
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

    public void setLastFrames(LastFrames lastFrames) {
        this.lastFrames = lastFrames;
    }
}
