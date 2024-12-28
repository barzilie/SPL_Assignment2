package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class DetectObjectsEvent implements Event<Boolean>{
    private StampedDetectedObjects detectedObjects;
    private int timeTosend;

    public DetectObjectsEvent(StampedDetectedObjects detectedObjects, int timeTosend){
        this.detectedObjects = detectedObjects;
        this.timeTosend = timeTosend;
    }

    public StampedDetectedObjects getDetectedObjects(){
        return this.detectedObjects;
    }

    public int getTimeToSend(){
        return this.timeTosend;
    }
}
