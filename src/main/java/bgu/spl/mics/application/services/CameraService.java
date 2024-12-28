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
import bgu.spl.mics.application.objects.LastFrames;
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
    private ConcurrentLinkedQueue<Future<Boolean>> cameraFutures;
    private int finishTime = 0;
    private Vector<DetectObjectsEvent> eventsToSend;
    
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
        this.eventsToSend = new Vector<>();
        initialize();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {

        //subscribes and enters callbacks
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick)->{ 
            currentTick++;
            if(camera.safeTermination(currentTick)){
                terminate();
            }
            else{
                StampedDetectedObjects sdo = camera.prepareData(currentTick);
                if(sdo == null){
                    sendBroadcast(new CrashedBroadcast(this.currentTick));
                    Thread.currentThread().interrupt();
                }
                else{
                    this.eventsToSend.add(new DetectObjectsEvent(sdo, currentTick+camera.getFrequency()));
                }
                //add if condition for the eventsTosend list
                if(!eventsToSend.isEmpty() && eventsToSend.get(0).getTimeToSend()==currentTick){
                    Future<Boolean> f = sendEvent(eventsToSend.firstElement());
                    eventsToSend.remove(0);
                    if(f!=null){
                        cameraFutures.add(f);
                    }
                }
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated)->{ 
            if(terminated.getSenderName().equals("TimeService")){
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed)->{
            LastFrames.getInstance().addCameraLastFrames("camera: " + camera.getId(), this.eventsToSend.lastElement().getDetectedObjects());
            camera.setStatus(STATUS.DOWN);
            terminate();
        });

    }    
}
