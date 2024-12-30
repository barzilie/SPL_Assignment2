package bgu.spl.mics.application.messages;

import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

public class TrackedObjectsEvent implements Event<Boolean>{
    private ConcurrentLinkedQueue<TrackedObject> trackedObjects;
    private int timeToSend;

    public TrackedObjectsEvent(ConcurrentLinkedQueue<TrackedObject> trackedObjects, int timeToSend){
        this.trackedObjects = trackedObjects;
        this.timeToSend = timeToSend;
    }

    public int getTimeToSend(){
        return timeToSend;
    }

    public ConcurrentLinkedQueue<TrackedObject> getTrackedObjects(){
        return trackedObjects;
    }
    
}
