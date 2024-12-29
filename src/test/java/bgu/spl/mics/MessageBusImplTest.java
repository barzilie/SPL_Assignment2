package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.TimeService;


import static org.junit.jupiter.api.Assertions.*;

public class MessageBusImplTest {

    private MessageBusImpl messageBus;
    private CameraService cameraService;
    private LiDarService lidarService;
    private TimeService timeService;
    private TickBroadcast tickBroadcast;
    private PoseEvent poseEvent;
    private CrashedBroadcast crashedBroadcast;

    @BeforeEach
    public void setUp() {
        messageBus = MessageBusImpl.getInstance();
        cameraService = new CameraService(new Camera(1, 2, "cam1"));
        lidarService = new LiDarService(new LiDarWorkerTracker(2, 3), "LidarDBPath"); 
        timeService = new TimeService(2, 30); 

        tickBroadcast = new TickBroadcast();
        poseEvent = new PoseEvent(new Pose(2, 5, 20, 3));
        crashedBroadcast = new CrashedBroadcast(6);

        messageBus.register(cameraService);
        messageBus.register(lidarService);
        messageBus.register(timeService);
    }

    @Test
    public void testSubscribeEvent() {
        messageBus.subscribeEvent(PoseEvent.class, lidarService);
        assertTrue(messageBus.getEventSubscribers().get(PoseEvent.class).contains(lidarService));
    }

    @Test
    public void testSubscribeBroadcast() {
        messageBus.subscribeBroadcast(TickBroadcast.class, cameraService);
        assertTrue(messageBus.getBroadcastSubscribers().get(TickBroadcast.class).contains(cameraService));
    }

    @Test
    public void testComplete() {
        messageBus.subscribeEvent(PoseEvent.class, timeService);
        Future<Boolean> future = messageBus.sendEvent(poseEvent);
        assertNotNull(future);
        messageBus.complete(poseEvent, true);
        assertTrue(future.isDone());
        assertEquals(true, future.get());
    }

    @Test
    public void testSendBroadcast() {
        messageBus.subscribeBroadcast(CrashedBroadcast.class, lidarService);
        messageBus.subscribeBroadcast(CrashedBroadcast.class, timeService);
        messageBus.sendBroadcast(crashedBroadcast);
        try {
            assertEquals(crashedBroadcast, messageBus.awaitMessage(lidarService));
            assertEquals(crashedBroadcast, messageBus.awaitMessage(timeService));
        } catch (Exception e) {
            fail("Broadcast delivery interrupted");
        }
    }

    @Test
    public void testSendEvent() {
        messageBus.subscribeEvent(PoseEvent.class, timeService);
        Future<Boolean> future = messageBus.sendEvent(poseEvent);
        assertNotNull(future);
        try {
            assertEquals(poseEvent, messageBus.awaitMessage(timeService));
        } catch (Exception e) {
            fail("Event delivery interrupted");
        }
    }

    @Test
    public void testRegisterAndUnregister() {
        messageBus.register(cameraService);
        messageBus.subscribeEvent(PoseEvent.class, cameraService);
        messageBus.subscribeBroadcast(CrashedBroadcast.class, cameraService);

        assertTrue(messageBus.getMicroServiceQueues().containsKey(cameraService));
        assertTrue(messageBus.getEventSubscribers().get(PoseEvent.class).contains(cameraService));
        assertTrue(messageBus.getBroadcastSubscribers().get(CrashedBroadcast.class).contains(cameraService));

        messageBus.unregister(cameraService);
        assertFalse(messageBus.getMicroServiceQueues().containsKey(cameraService));
        assertFalse(messageBus.getEventSubscribers().get(PoseEvent.class).contains(cameraService));
        assertFalse(messageBus.getBroadcastSubscribers().get(CrashedBroadcast.class).contains(cameraService));
    }


    @Test
    public void testAwaitMessage() {
        messageBus.subscribeEvent(PoseEvent.class, lidarService);
        messageBus.sendEvent(poseEvent);
        try {
            Message message = messageBus.awaitMessage(lidarService);
            assertEquals(poseEvent, message);
        } catch (Exception e) {
            fail("awaitMessage interrupted");
        }
    }

    @Test
    public void testRoundRobin() {
        messageBus.subscribeEvent(PoseEvent.class, lidarService);
        messageBus.subscribeEvent(PoseEvent.class, cameraService);
        messageBus.subscribeEvent(PoseEvent.class, timeService);

        PoseEvent event1 = new PoseEvent(new Pose(1, 2, 10, 1));
        PoseEvent event2 = new PoseEvent(new Pose(2, 4, -20, 2));
        PoseEvent event3 = new PoseEvent(new Pose(5, 2, 20, 3));

        messageBus.sendEvent(event1);
        messageBus.sendEvent(event2);
        messageBus.sendEvent(event3);

        try {
            assertEquals(event1, messageBus.awaitMessage(lidarService));
            assertEquals(event2, messageBus.awaitMessage(cameraService));
            assertEquals(event3, messageBus.awaitMessage(timeService));
        } catch (Exception e) {
            fail("Round-robin delivery interrupted");
        }
    }
}
