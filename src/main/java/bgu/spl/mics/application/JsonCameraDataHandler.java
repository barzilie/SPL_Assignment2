package bgu.spl.mics.application;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JsonCameraDataHandler {

    public static  HashMap<String, ArrayList<Vector<StampedDetectedObjects>>> cameraDataHandler(String path) {
        try (FileReader reader = new FileReader(path)) {
            Gson gson = new Gson();

            // Define the Type for the List of StampedDetectedObjects
            //Type StampedDetectedObjectssListType = new TypeToken<ArrayList<ConcurrentLinkedQueue<StampedDetectedObjects>>>() {}.getType();

            // Parse the JSON
            HashMap<String, ArrayList<Vector<StampedDetectedObjects>>> cameraData = gson.fromJson(reader, new TypeToken<HashMap<String, ArrayList<Vector<StampedDetectedObjects>>>>() {}.getType());
            return cameraData;
        } catch (Exception e) {
            System.err.println("Unexpected error during deserialization: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
}
