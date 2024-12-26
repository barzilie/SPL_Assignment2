package bgu.spl.mics.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.TimeService;

public class JsonConfigHandler {


    public static class CameraConfiguration {
        private int id;
        private int frequency;
        private String camera_key;


        public int getId() {
            return id;
        }

        public int getFrequency() {
            return frequency;
        }

        public String getCamera_key() {
            return camera_key;
        }
    }

    public static class LidarConfiguration {
        private int id;
        private int frequency;

        
        public int getId() {
            return id;
        }

        public int getFrequency() {
            return frequency;
        }
    }

    public static class Cameras {
        private List<CameraConfiguration> CamerasConfigurations;
        private String camera_datas_path;


        public List<CameraConfiguration> getCamerasConfigurations() {
            return CamerasConfigurations;
        }

        public String getCamera_datas_path() {
            return camera_datas_path;
        }
    }

    public static class Lidars {
        private List<LidarConfiguration> LidarConfigurations;
        private String lidars_data_path;


        public List<LidarConfiguration> getLidarConfigurations() {
            return LidarConfigurations;
        }

        public String getLidars_data_path() {
            return lidars_data_path;
        }
    }

    public static class RootObject {
        private Cameras Cameras;
        private Lidars Lidars;
        private String poseJsonFile;
        private int TickTime = 0;
        private int Duration = 0;


        public Cameras getCameras() {
            return Cameras;
        }

        public Lidars getLidars() {
            return Lidars;
        }

        public String getPoseJsonFile() {
            return poseJsonFile;
        }

        public int getTickTime() {
            return TickTime;
        }

        public int getDuration() {
            return Duration;
        }
    }

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
