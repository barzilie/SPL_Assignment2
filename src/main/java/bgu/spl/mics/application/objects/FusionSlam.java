package bgu.spl.mics.application.objects;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private Vector<LandMark> landmarks;
    private Vector<Pose> poses;
    private FusionSlam(){
        this.landmarks = new Vector<>();
        this.poses = new Vector<>();
    }
    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam instance = new FusionSlam();
    }

    public static FusionSlam getInstance(){
        return FusionSlamHolder.instance;
    }

    public ConcurrentLinkedQueue<CloudPoint> convertToGlobal(TrackedObject trackedObject){
        ConcurrentLinkedQueue<CloudPoint> globalCoordinates = new ConcurrentLinkedQueue<CloudPoint>();
        Pose p = poses.get(trackedObject.getTime()-1);
        double x_robot = p.getX();
        double y_robot = p.getY();
        double radDegree = (p.getYaw()*Math.PI)/180;
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
    

}
