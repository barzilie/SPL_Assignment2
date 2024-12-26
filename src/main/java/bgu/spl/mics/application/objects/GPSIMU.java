package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick = 0;
    private STATUS status = STATUS.UP;
    private ConcurrentLinkedQueue<Pose> poseList;

    public GPSIMU(String filePath){
        PoseInitizlizer(filePath);
    }

    public Pose retrievePose(int time){
        for(Pose p: poseList){
            if(p.getTime() == time) return p;
        }
        return null;
    }

    public void PoseInitizlizer(String filePath){
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ConcurrentLinkedQueue<Pose>>() {}.getType();
            ConcurrentLinkedQueue<Pose> poseDataList = gson.fromJson(reader, listType);
            this.poseList = poseDataList;
        } 
        catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }


}
