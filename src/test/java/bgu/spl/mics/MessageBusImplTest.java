package bgu.spl.mics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;

@TestInstance(Lifecycle.PER_CLASS)
public class MessageBusImplTest {

    private MessageBusImpl messageBus;
    private CameraService cameraService1;
    private CameraService cameraService2;

    private FusionSlamService fusionSlamService;
    private TickBroadcast tickBroadcast;
    private PoseEvent poseEvent;
    private CrashedBroadcast crashedBroadcast;

    @BeforeAll
    public void setUp(){
        System.out.println("entered setUp");
        messageBus = MessageBusImpl.getInstance();
        cameraService1 = new CameraService(new Camera(1, 2, "cam1"));
        cameraService2 = new CameraService(new Camera(5, 4, "cam2"));
        fusionSlamService = new FusionSlamService(FusionSlam.getInstance()); 
        tickBroadcast = new TickBroadcast();
        poseEvent = new PoseEvent(new Pose(2, 5, 20, 3));
        crashedBroadcast = new CrashedBroadcast(6);
        messageBus.unregister(cameraService1);
        messageBus.unregister(cameraService2);
        messageBus.unregister(fusionSlamService);

    }

    @Test
    public void testSubscribeEvent() {
        System.out.println("entered testSubscribeEvent");
        messageBus.register(fusionSlamService);
        messageBus.subscribeEvent(PoseEvent.class, fusionSlamService);
        assertTrue(messageBus.getEventSubscribers().get(PoseEvent.class).contains(fusionSlamService));
        messageBus.unregister(fusionSlamService);
    }

    @Test
    public void testSubscribeBroadcast() {
        System.out.println("entered testSubscribeBroadcast");

        messageBus.register(cameraService1);
        messageBus.subscribeBroadcast(TickBroadcast.class, cameraService1);
        assertTrue(messageBus.getBroadcastSubscribers().get(TickBroadcast.class).contains(cameraService1));
        messageBus.unregister(cameraService1);
    }

    @Test
    public void testSendBroadcast() {
        System.out.println("entered testSendBroadcast");

        messageBus.register(cameraService1);
        messageBus.register(fusionSlamService);
        messageBus.subscribeBroadcast(CrashedBroadcast.class, cameraService1);
        messageBus.subscribeBroadcast(CrashedBroadcast.class, fusionSlamService);
        messageBus.sendBroadcast(crashedBroadcast);
        try {
            assertEquals(crashedBroadcast, messageBus.awaitMessage(cameraService1));
            assertEquals(crashedBroadcast, messageBus.awaitMessage(fusionSlamService));
        } catch (Exception e) {
            fail("Broadcast delivery interrupted");
        }
        messageBus.unregister(cameraService1);
        messageBus.unregister(fusionSlamService);

    }

    @Test
    public void testSendEvent() {
        System.out.println("entered testSendEvent");

        messageBus.register(fusionSlamService);
        messageBus.subscribeEvent(PoseEvent.class, fusionSlamService);
        //messageBus.sendEvent(poseEvent);
        Future<Boolean> future = messageBus.sendEvent(poseEvent);
        assertNotNull(future);
        System.out.println("future is not null");
        try {
            assertEquals(poseEvent, messageBus.awaitMessage(fusionSlamService));
        } catch (Exception e) {
            fail("Event delivery interrupted");
        }
        System.out.println("EXITED");
        messageBus.unregister(fusionSlamService);
    }

    @Test
    public void testComplete() {
        System.out.println("entered testComplete");

        messageBus.register(fusionSlamService);
        messageBus.subscribeEvent(PoseEvent.class, fusionSlamService);
        Future<Boolean> future = messageBus.sendEvent(poseEvent);
        assertNotNull(future);
        messageBus.complete(poseEvent, true);
        assertTrue(future.isDone());
        assertEquals(true, future.get());
        messageBus.unregister(fusionSlamService);
    }

    @Test
    public void testRegisterAndUnregister() {
        System.out.println("entered testSubscribeEvent");

        messageBus.register(cameraService1);
        messageBus.subscribeEvent(PoseEvent.class, cameraService1);
        messageBus.subscribeBroadcast(CrashedBroadcast.class, cameraService1);

        assertTrue(messageBus.getMicroServiceQueues().containsKey(cameraService1));
        assertTrue(messageBus.getEventSubscribers().get(PoseEvent.class).contains(cameraService1));
        assertTrue(messageBus.getBroadcastSubscribers().get(CrashedBroadcast.class).contains(cameraService1));

        messageBus.unregister(cameraService1);
        assertFalse(messageBus.getMicroServiceQueues().containsKey(cameraService1));
        assertFalse(messageBus.getEventSubscribers().get(PoseEvent.class).contains(cameraService1));
        assertFalse(messageBus.getBroadcastSubscribers().get(CrashedBroadcast.class).contains(cameraService1));
    }


    @Test
    public void testAwaitMessage() {
        System.out.println("entered testAwaitMessage");

        messageBus.register(cameraService1);
        messageBus.subscribeEvent(PoseEvent.class, cameraService1);
        messageBus.sendEvent(poseEvent);
        try {
            Message message = messageBus.awaitMessage(cameraService1);
            System.out.println("got the message in try block of awaitmessage");
            assertEquals(poseEvent, message);
        } catch (Exception e) {
            fail("awaitMessage interrupted");
        }
        messageBus.unregister(cameraService1);
    }

    @Test
    public void testRoundRobin() {
        System.out.println("entered testRoundRobin");

        messageBus.register(cameraService2);
        messageBus.register(cameraService1);
        messageBus.register(fusionSlamService);
        messageBus.subscribeEvent(PoseEvent.class, cameraService2);
        messageBus.subscribeEvent(PoseEvent.class, cameraService1);
        messageBus.subscribeEvent(PoseEvent.class, fusionSlamService);

        PoseEvent event1 = new PoseEvent(new Pose(1, 2, 10, 1));
        PoseEvent event2 = new PoseEvent(new Pose(2, 4, -20, 2));
        PoseEvent event3 = new PoseEvent(new Pose(5, 2, 20, 3));

        messageBus.sendEvent(event1);
        messageBus.sendEvent(event2);
        messageBus.sendEvent(event3);

        try {
            assertEquals(event1, messageBus.awaitMessage(cameraService2));
            assertEquals(event2, messageBus.awaitMessage(cameraService1));
            assertEquals(event3, messageBus.awaitMessage(fusionSlamService));
        } catch (Exception e) {
            fail("Round-robin delivery interrupted");
        }
        messageBus.unregister(cameraService2);
        messageBus.unregister(cameraService1);
        messageBus.unregister(fusionSlamService);
    }
}
