package bgu.spl.mics.application;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.JsonConfigHandler.Lidars;
import bgu.spl.mics.application.JsonConfigHandler.RootObject;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.TimeService;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;



/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");
        try (FileReader reader = new FileReader(args[0])) {
            Gson gson = new Gson();

            // Parse the JSON
            RootObject rootObject = gson.fromJson(reader, RootObject.class);
            HashMap<String, ArrayList<ConcurrentLinkedQueue<StampedDetectedObjects>>> cameraMap = JsonCameraDataHandler.cameraDataHandler(rootObject.getCameras().getCamera_datas_path());

            // Access data
            JsonConfigHandler.buildServicesConfig(rootObject, cameraMap);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading JSON file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during deserialization: " + e.getMessage());
            e.printStackTrace();
        }

        // TODO: Parse configuration file.
        // TODO: Initialize system components and services.
        // TODO: Start the simulation.
    }
}
