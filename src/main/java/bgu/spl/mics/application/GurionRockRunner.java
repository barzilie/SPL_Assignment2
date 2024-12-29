package bgu.spl.mics.application;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.JsonConfigHandler.RootObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.services.TimeService;



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
            HashMap<String, ArrayList<Vector<StampedDetectedObjects>>> cameraMap = JsonCameraDataHandler.cameraDataHandler(rootObject.getCameras().getCamera_datas_path());
            ExecutorService executor = Executors.newCachedThreadPool();
            Vector<MicroService> microServices = JsonConfigHandler.buildServicesConfig(rootObject, cameraMap);
           // Vector<Thread> threads = new Vector<>();
            for(MicroService ms: microServices){
                //Thread thread = new Thread(ms);
                //threads.add(thread);
                //thread.start(); 
                executor.execute(ms);
            }
            StatisticalFolder SF = StatisticalFolder.getInstance();//TODO: remember this is arg for GSON output
            if (rootObject != null && rootObject.getTickTime() != 0 && rootObject.getDuration() != 0) {
                executor.execute(new TimeService(rootObject.getTickTime(), rootObject.getDuration()));
            } else {
                System.out.println("Error: ClockConfiguration is null.");
            }
            executor.shutdown(); 
            try{
                executor.awaitTermination(100, TimeUnit.SECONDS);
            }
            catch(InterruptedException e){

            }
            JsonOuputWriter.createOutput(SF);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading JSON file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        // TODO: Parse configuration file.
        // TODO: Initialize system components and services.
        // TODO: Start the simulation.
    }
}
