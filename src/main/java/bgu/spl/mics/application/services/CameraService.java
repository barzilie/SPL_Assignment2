package bgu.spl.mics.application.services;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.STATUS;
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
    private int currentTick = 0;
    private ConcurrentLinkedQueue<Future<Vector<CloudPoint>>> cameraFutures;
    //maybe add a field of last index that was sent in the stampedlist (and change tickhandle method)

    //private List<HashMap<String, Object>> cameraData;
    //private boolean dataLoaded = false; 


    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("CameraService: "+camera.getId());
        this.camera = camera;
        this.cameraFutures = new ConcurrentLinkedQueue<>();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {

        //subscribes and enters callbacks
        subscribeBroadcast(TickBroadcast.class, this::handleTick);
        subscribeBroadcast(TerminatedBroadcast.class, this::handleTerminated);
        subscribeBroadcast(CrashedBroadcast.class, this::handleCrashed);

    }

    //callback function for TickBroadcasts
    protected void handleTick(TickBroadcast tick){ 
        currentTick++;
        int timeToCheck = currentTick - this.camera.getFrequency();
        for(StampedDetectedObjects s: camera.getDetectedObjectsList()){
            if(s.getTime() == timeToCheck){
                Future<Vector<CloudPoint>> f = sendEvent(new DetectObjectsEvent(s, camera));
                cameraFutures.add(f);
                break;
            }
        }
    }    

    //callback function for TerminatedBroadcast
    private void handleTerminated(TerminatedBroadcast terminated){ 
        //add to statistics and do the termination stuff
        terminate();
        camera.setStatus(STATUS.DOWN);
    }

    //callback function for CrashedBroadcast
    private void handleCrashed(CrashedBroadcast crashed){
        //add to statistics and do the termination stuff and more crashed things page 23
        terminate();
        camera.setStatus(STATUS.DOWN);
    }



}
