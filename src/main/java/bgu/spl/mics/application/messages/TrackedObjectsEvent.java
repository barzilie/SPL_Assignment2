package bgu.spl.mics.application.messages;

import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

public class TrackedObjectsEvent implements Event<T>{
    private ConcurrentLinkedQueue<TrackedObject> trackedObjects;

    public TrackedObjectsEvent(ConcurrentLinkedQueue<TrackedObject> trackedObjects){
        this.trackedObjects = trackedObjects;
    }
    
}
