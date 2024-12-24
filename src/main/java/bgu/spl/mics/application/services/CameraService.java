package bgu.spl.mics.application.services;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private final Camera camera;
    private int currentTick;
    private ConcurrentLinkedQueue<StampedDetectedObjects> detectedObjectsToSend;
    private int ticksPassedSinceLastSend;
    //private List<HashMap<String, Object>> cameraData;
    //private boolean dataLoaded = false; 


    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(String name, Camera camera) {
        super(name);
        this.camera = camera;
        this.currentTick = 0;
        this.ticksPassedSinceLastSend = 0;
        this.detectedObjectsToSend = new  ConcurrentLinkedQueue<StampedDetectedObjects>();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        // subscribe to TickBroadcast, TerminatedBroadcast, CrashedBroadcast.
        // TODO Implement this
    }

    private void sendDetectObjectsEvent(){}

}
