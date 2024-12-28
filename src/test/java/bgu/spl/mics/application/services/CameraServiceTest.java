import bgu.spl.mics.Future;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.CameraService;
import org.junit.jupiter.api.*;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CameraServiceTest {

    private Camera mockCamera;
    private CameraService cameraService;

    @BeforeEach
    void setUp() {
        mockCamera = mock(Camera.class);
        when(mockCamera.getId()).thenReturn(1);
        when(mockCamera.getFrequency()).thenReturn(5);
        when(mockCamera.getDetectedObjectsList()).thenReturn(generateMockDetectedObjectsList());

        cameraService = new CameraService(mockCamera);
    }

    @Test
    void testHandleTick_NormalOperation() {
        // Simulate normal operation
        TickBroadcast tick = new TickBroadcast();
        cameraService.handleTick(tick);

        verify(mockCamera, atLeastOnce()).getDetectedObjectsList();
        verify(mockCamera, never()).setStatus(STATUS.DOWN);
        verify(mockCamera, never()).setStatus(STATUS.ERROR);
    }

    @Test
    void testHandleTick_ErrorDetected() {
        // Simulate a detected object with an "ERROR" ID
        StampedDetectedObjects errorObject = new StampedDetectedObjects(0, generateErrorDetectedObjects());
        when(mockCamera.getDetectedObjectsList()).thenReturn(new ConcurrentLinkedQueue<>(Vector.of(errorObject)));

        TickBroadcast tick = new TickBroadcast();
        cameraService.handleTick(tick);

        verify(mockCamera).setStatus(STATUS.ERROR);
        verify(mockCamera, atLeastOnce()).getDetectedObjectsList();
    }

    @Test
    void testHandleTick_Termination() {
        // Simulate termination condition
        when(mockCamera.getDetectedObjectsList()).thenReturn(generateMockDetectedObjectsList());

        TickBroadcast tick = new TickBroadcast();
        for (int i = 0; i <= 10; i++) {
            cameraService.handleTick(tick);
        }

        verify(mockCamera).setStatus(STATUS.DOWN);
        assertTrue(cameraService.isTerminated());
    }

    @Test
    void testHandleCrashed() {
        CrashedBroadcast crashed = new CrashedBroadcast();
        cameraService.handleCrashed(crashed);

        verify(mockCamera).setStatus(STATUS.DOWN);
        assertTrue(cameraService.isTerminated());
    }

    private ConcurrentLinkedQueue<StampedDetectedObjects> generateMockDetectedObjectsList() {
        ConcurrentLinkedQueue<StampedDetectedObjects> detectedObjectsList = new ConcurrentLinkedQueue<>();
        for (int i = 1; i <= 5; i++) {
            detectedObjectsList.add(new StampedDetectedObjects(i, generateValidDetectedObjects()));
        }
        return detectedObjectsList;
    }

    private ConcurrentLinkedQueue<DetectedObject> generateValidDetectedObjects() {
        ConcurrentLinkedQueue<DetectedObject> detectedObjects = new ConcurrentLinkedQueue<>();
        detectedObjects.add(new DetectedObject("ID1", "Car"));
        detectedObjects.add(new DetectedObject("ID2", "Person"));
        return detectedObjects;
    }

    private ConcurrentLinkedQueue<DetectedObject> generateErrorDetectedObjects() {
        ConcurrentLinkedQueue<DetectedObject> detectedObjects = new ConcurrentLinkedQueue<>();
        detectedObjects.add(new DetectedObject("ERROR", "Unknown"));
        return detectedObjects;
    }
}
