package bgu.spl.mics.application.objects;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private Vector<LandMark> landmarks;
    private Vector<Pose> poses;
    private StatisticalFolder statisticalFolder;
    private ConcurrentLinkedQueue<TrackedObjectsEvent> toHandleNextTick;
    
    private FusionSlam(){
        this.landmarks = new Vector<>();
        this.poses = new Vector<>();
        this.statisticalFolder = StatisticalFolder.getInstance();
        this.toHandleNextTick = new ConcurrentLinkedQueue<>();
    }
    // Singleton instance holder
    private static class FusionSlamHolder {
        private static FusionSlam instance = new FusionSlam();
    }

    public static FusionSlam getInstance(){
        return FusionSlamHolder.instance;
    }

    public ConcurrentLinkedQueue<CloudPoint> convertToGlobal(TrackedObject trackedObject){
        ConcurrentLinkedQueue<CloudPoint> globalCoordinates = new ConcurrentLinkedQueue<CloudPoint>();
        Pose p = poses.get(trackedObject.getTime()-1);
        double x_robot = p.getX();
        double y_robot = p.getY();
        double radDegree = (p.getYaw()*(Math.PI/180));
        double cos = Math.cos(radDegree);
        double sin = Math.sin(radDegree);
        for(CloudPoint cp: trackedObject.getCoordinates()){
            double x_global = cos*cp.getX() - sin*cp.getY() + x_robot;
            double y_global = sin*cp.getX() + cos*cp.getY() + y_robot;
            globalCoordinates.add(new CloudPoint(x_global, y_global));
        }
        return globalCoordinates;
    }

    public ConcurrentLinkedQueue<CloudPoint> refineCoordinates(ConcurrentLinkedQueue<CloudPoint> oldCoordinates, ConcurrentLinkedQueue<CloudPoint> newCoordinates){
        ConcurrentLinkedQueue<CloudPoint> refinedCoordinates = new ConcurrentLinkedQueue<>();
        Iterator<CloudPoint> itOld = oldCoordinates.iterator();
        Iterator<CloudPoint> itNew = newCoordinates.iterator();
        while(itOld.hasNext()&itNew.hasNext()){
            CloudPoint oldCP = itOld.next();
            CloudPoint newCP = itNew.next();
            refinedCoordinates.add(new CloudPoint((oldCP.getX()+newCP.getX())/2, (oldCP.getY()+newCP.getY())/2));
        }
        while(itNew.hasNext()){
            CloudPoint newCP = itNew.next();
            refinedCoordinates.add(new CloudPoint(newCP.getX(),newCP.getY()));
        }
        return refinedCoordinates;
    }

    public LandMark retrieveLandmark(String objectId){
        for(LandMark lm: landmarks){
            if(lm.getId().equals(objectId)) return lm;
        }
        return null;
    }

    public void addLandMark(LandMark lm){
        this.landmarks.add(lm);
    }

    public void addPose(Pose p){
        poses.add(p);
    }

    //public for testing 
    public void handleTrackedObject(TrackedObjectsEvent trackedObjects){
        if(!getPoseSize(trackedObjects)){
            this.toHandleNextTick.add(trackedObjects);
        }
        else{
            for(TrackedObject object: trackedObjects.getTrackedObjects()){
                ConcurrentLinkedQueue<CloudPoint> ObjectGlobalCoordinates = convertToGlobal(object);
                LandMark toRefine = retrieveLandmark(object.getId());
                if(toRefine == null){
                    LandMark toAdd = new LandMark(object.getId(),object.getDescription(), ObjectGlobalCoordinates);
                    addLandMark(toAdd);
                    statisticalFolder.increaseNumLandMarks();
                    statisticalFolder.addLandMarks(toAdd);
                }
                else{
                     toRefine.setCoordinates(refineCoordinates(toRefine.getCoordinates(), ObjectGlobalCoordinates));
                }
            }

        }    
    }

    public void updateStatisticsBeforeCrash(CrashedBroadcast crashed){
        setTerminateClock();
        statisticalFolder.setLastFrames(LastFrames.getInstance());
        statisticalFolder.setSystemRuntime(crashed.getCrashTime());
    }

    public void setTerminateClock(){
        statisticalFolder.setTerminateClock();
    }

    public boolean getPoseSize(TrackedObjectsEvent toe){
        return this.poses.size()>=toe.getTrackedObjects().peek().getTime()-1;
    }

    public void handlePastTrackedObjects() {
        while(!this.toHandleNextTick.isEmpty() && getPoseSize(this.toHandleNextTick.peek())){
            handleTrackedObject(toHandleNextTick.poll());
        }
    }

    public Vector<LandMark> getLandmarks() {
        return landmarks;
    }

}
