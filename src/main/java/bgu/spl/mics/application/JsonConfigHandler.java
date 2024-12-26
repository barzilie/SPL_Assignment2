package bgu.spl.mics.application;

import java.util.List;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;

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


}
