package bgu.spl.mics.application;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import bgu.spl.mics.MicroService;
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

        public void setCamera_datas_path(String camera_datas_path) {
            this.camera_datas_path = camera_datas_path;
        }
    }

    public static class LidarWorkers {
        private List<LiDarWorkerTracker> LidarConfigurations;
        private String lidars_data_path;

        public List<LiDarWorkerTracker> getLidarsObjects() {
            return LidarConfigurations;
        }

        public String getLidars_data_path() {
            return lidars_data_path;
        }

        public void setLidars_data_path(String lidars_data_path) {
            this.lidars_data_path = lidars_data_path;
        }
    }

    public static class RootObject {
        private Cameras Cameras;
        private LidarWorkers LiDarWorkers;
        private String poseJsonFile;
        private int TickTime = 0;
        private int Duration = 0;


        public Cameras getCameras() {
            return Cameras;
        }

        public LidarWorkers getLidars() {
            return LiDarWorkers;
        }

        public String getPoseJsonFile() {
            return poseJsonFile;
        }

        public void setPoseJsonFile(String poseJsonFile) {
            this.poseJsonFile = poseJsonFile;
        }


        public int getTickTime() {
            return TickTime;
        }

        public int getDuration() {
            return Duration;
        }

        public void setParentPaths(String configPath){
            //find parent path
             Path configFilePath = Paths.get(configPath);
             Path parentPath = configFilePath.getParent();

             //convert relative to absolute path 
             Path absolutePoses = parentPath.resolve(this.getPoseJsonFile()).normalize();
             Path absoluteCamera = parentPath.resolve(this.Cameras.getCamera_datas_path()).normalize();
             Path absoluteLidar = parentPath.resolve(this.LiDarWorkers.getLidars_data_path()).normalize();

             //reassign corrected paths
             this.setPoseJsonFile(absolutePoses.toString());
             this.Cameras.setCamera_datas_path(absoluteCamera.toString());
             this.LiDarWorkers.setLidars_data_path(absoluteLidar.toString());
             System.out.println("PATH: " + this.getPoseJsonFile());
             System.out.println("PATH: " + this.Cameras.getCamera_datas_path());
             System.out.println("PATH: " + this.LiDarWorkers.getLidars_data_path());
        }

    }

    public static Vector<MicroService> buildServicesConfig(RootObject rootObject,
     HashMap<String, Vector<StampedDetectedObjects>> cameraMap){
        Vector<MicroService> microServices = new Vector<>();
        FusionSlamService fusionSlam = new FusionSlamService(FusionSlam.getInstance());
        microServices.add(fusionSlam);
        if (rootObject != null && rootObject.getCameras() != null && rootObject.getCameras().getListCameras() != null) {
            for (Camera camera : rootObject.getCameras().getListCameras()) {
                camera.setDetectedObjectsList(cameraMap.get(camera.getCameraKey()));
                camera.setStatus(STATUS.UP);
                fusionSlam.incrementNumOfSensors();
                microServices.add(new CameraService(camera));
            }
        } else {
            System.out.println("Error: CamerasConfigurations is null.");
        }
        if (rootObject != null && rootObject.getLidars() != null && rootObject.getLidars().getLidarsObjects() != null) {
            System.out.println("LIDAR IS NOT NULL IN ROOT");
            for (LiDarWorkerTracker lidar : rootObject.getLidars().getLidarsObjects()) {
                lidar.setStatus(STATUS.UP);
                fusionSlam.incrementNumOfSensors();
                System.out.println("LIDAR LOOP ENTERED");
                microServices.add(new LiDarService(lidar, rootObject.getLidars().getLidars_data_path()));
            }
        } else {
            System.out.println("Error: LidarConfigurations is null.");
        }
        if (rootObject != null && rootObject.getPoseJsonFile() != null) {
            microServices.add(new PoseService(new GPSIMU(rootObject.getPoseJsonFile())));
        } else {
            System.out.println("Error: PoseConfiguration is null.");
        }
        return microServices;
        
    }



}
