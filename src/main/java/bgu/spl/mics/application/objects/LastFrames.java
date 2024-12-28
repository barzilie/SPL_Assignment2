package bgu.spl.mics.application.objects;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;

public class LastFrames {

    private ConcurrentHashMap<String, StampedDetectedObjects> cameraLastFrames;
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<TrackedObject>> lidarLastFrames;

    private LastFrames(){
        this.cameraLastFrames = new ConcurrentHashMap<>(); 
        this.lidarLastFrames = new ConcurrentHashMap<>();

    }
    // Singleton instance holder
    private static class LastFramesHolder {
        private static LastFrames instance = new LastFrames();
    }

    public static LastFrames getInstance(){
        return LastFramesHolder.instance;
    }

    public ConcurrentHashMap<String, StampedDetectedObjects> getCameraLastFrames() {
        return cameraLastFrames;
    }

    public void addCameraLastFrames(String camera, StampedDetectedObjects cameraLastFrames) {
        this.cameraLastFrames.put(camera, cameraLastFrames);
    }

    public ConcurrentHashMap<String, ConcurrentLinkedQueue<TrackedObject>> getLidarLastFrames() {
        return lidarLastFrames;
    }

    public void addLidarLastFrames(String lidar, ConcurrentLinkedQueue<TrackedObject> lidarLastFrames) {
        this.lidarLastFrames.put(lidar, lidarLastFrames);
    }
    
}
