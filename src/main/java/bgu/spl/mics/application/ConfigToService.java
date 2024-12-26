package bgu.spl.mics.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.application.JsonConfigHandler.CameraConfiguration;
import bgu.spl.mics.application.JsonConfigHandler.LidarConfiguration;
import bgu.spl.mics.application.JsonConfigHandler.RootObject;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.TimeService;

public class ConfigToService {
    
    public static void buildServicesConfig(RootObject rootObject,
     HashMap<String, ArrayList<ConcurrentLinkedQueue<StampedDetectedObjects>>> cameraMap) throws Exception{
            if (rootObject != null && rootObject.getCameras() != null && rootObject.getCameras().getCamerasConfigurations() != null) {
                for (CameraConfiguration cameraConfig : rootObject.getCameras().getCamerasConfigurations()) {
                    new CameraService(new Camera(cameraConfig.getId(), cameraConfig.getFrequency(), cameraConfig.getCamera_key(),cameraMap.get(cameraConfig.getCamera_key()).get(0)));
                }
            } else {
                throw new Exception("Error: CamerasConfigurations is null.");
            }
            if (rootObject != null && rootObject.getLidars() != null && rootObject.getLidars().getLidarConfigurations() != null) {
                for (LidarConfiguration lidarConfig : rootObject.getLidars().getLidarConfigurations()) {
                    new LiDarService(new LiDarWorkerTracker(lidarConfig.getId(), lidarConfig.getFrequency()), rootObject.getLidars().getLidars_data_path());
                }
            } else {
               throw new Exception("Error: LidarConfigurations is null.");
            }
            if (rootObject != null && rootObject.getTickTime() != 0 && rootObject.getDuration() != 0) {
                new TimeService(rootObject.getTickTime(), rootObject.getDuration());
            } else {
                throw new Exception("Error: ClockConfiguration is null.");
            }
        
    }
    
}
