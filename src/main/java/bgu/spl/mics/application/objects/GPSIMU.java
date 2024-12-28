package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.Future;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick = 0;
    private STATUS status = STATUS.UP;

    private Vector<Pose> poseList;

    public GPSIMU(String filePath){
        PoseInitizlizer(filePath);
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public Pose retrievePose(int time){
        if(poseList.size()<time-1) return null;
        return poseList.get(time-1);
    }

    public void PoseInitizlizer(String filePath){
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<Vector<Pose>>() {}.getType();
            Vector<Pose> poseDataList = gson.fromJson(reader, listType);
            this.poseList = poseDataList;
        } 
        catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    public Vector<Pose> errorPoseList(int errorTime){
        Vector<Pose> output = new Vector<>();
        for(int i=0; i<errorTime; i++){
            output.add(this.poseList.get(i));
        }
        return output;
    }

    public boolean failedTosendEvent(Future<Boolean> f){
        if(f == null){
            if(StatisticalFolder.getInstance().getError() != null){
                StatisticalFolder.getInstance().setPoses(errorPoseList(StatisticalFolder.getInstance().getSystemRuntime()));
            }
            return true;
        }
        return false;
    }
}
