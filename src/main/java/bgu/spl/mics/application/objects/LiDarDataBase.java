package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private ConcurrentLinkedQueue<StampedCloudPoints> cloudPoints;

    private static class LiDarDataBaseHolder {
        private static final LiDarDataBase instance = new LiDarDataBase();
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */

     public static LiDarDataBase getInstance(String filePath){ 
        LiDarDataBase db = LiDarDataBaseHolder.instance;
        db.initializeDB(filePath);
        return db;
    }

    public StampedCloudPoints retrieveCloudPoint(int time, String id){
        for(StampedCloudPoints stampedCP:this.cloudPoints){
            if(stampedCP.getId() == id && stampedCP.getTime() == time){
                return stampedCP;
            }
        }
        return null;
    }

    private ConcurrentLinkedQueue<StampedCloudPoints> initializeDB(String filePath){
        if(this.cloudPoints!=null) return cloudPoints;
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ConcurrentLinkedQueue<StampedCloudPoints>>() {}.getType();
            ConcurrentLinkedQueue<StampedCloudPoints> dataPoints = gson.fromJson(reader, listType);
            return dataPoints;

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return null;
    }

    
    
}
