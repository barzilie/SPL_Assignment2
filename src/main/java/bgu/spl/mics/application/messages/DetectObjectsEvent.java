package bgu.spl.mics.application.messages;

import java.util.Vector;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class DetectObjectsEvent implements Event<Vector<CloudPoint>>{
    private StampedDetectedObjects detectedObjects;
    private Camera sentBy;

    public DetectObjectsEvent(StampedDetectedObjects detectedObjects, Camera sentBy){
        this.detectedObjects = detectedObjects;
        this.sentBy = sentBy;
    }

    public StampedDetectedObjects getDetectedObjects(){
        return this.detectedObjects;
    }
}
