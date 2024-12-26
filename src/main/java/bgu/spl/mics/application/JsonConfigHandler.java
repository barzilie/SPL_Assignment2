package bgu.spl.mics.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;

public class JsonConfigHandler {


    public static class Cameras {
        private List<Camera> CamerasConfigurations;

        private String camera_datas_path;

        public List<Camera> getListCameras() {
            return CamerasConfigurations;
        }

        public String getCamera_datas_path() {
            return camera_datas_path;
        }
    }

    public static class Lidars {
        private List<LiDarWorkerTracker> LidarConfigurations;
        private String lidars_data_path;


        public List<LiDarWorkerTracker> getLidarsObjects() {
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
            if (rootObject != null && rootObject.getCameras() != null && rootObject.getCameras().getListCameras() != null) {
                for (Camera camera : rootObject.getCameras().getListCameras()) {
                    camera.setDetectedObjectsList(cameraMap.get(camera.getCameraKey()).get(0));
                    camera.setStatus(STATUS.UP);
                    new CameraService(camera);
                }
            } 
            else {
                System.out.println("Error: CamerasConfigurations is null.");
            }
            if (rootObject != null && rootObject.getLidars() != null && rootObject.getLidars().getLidarsObjects() != null) {
                for (LiDarWorkerTracker lidar : rootObject.getLidars().getLidarsObjects()) {
                    lidar.setStatus(STATUS.UP);
                    new LiDarService(lidar, rootObject.getLidars().getLidars_data_path());
                }
            } 
            else {
               System.out.println("Error: LidarConfigurations is null.");
            }
            if (rootObject != null && rootObject.getTickTime() != 0 && rootObject.getDuration() != 0) {
                new TimeService(rootObject.getTickTime(), rootObject.getDuration());
            } 
            else {
                System.out.println("Error: ClockConfiguration is null.");
            }
            if(rootObject != null && rootObject.getPoseJsonFile() != null){
                new PoseService(new GPSIMU(rootObject.getPoseJsonFile()));
            }
            else{
                System.out.println("Error: PoseConfiguration is null.");
            }
            new FusionSlamService(FusionSlam.getInstance());
    }


}
