package bgu.spl.mics.application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class JsonOuputWriter {

    public static void createOutput(Object StatisticalFolder, String path){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //find parent path
        Path coutputFilePath = Paths.get(path);
        Path parentPath = coutputFilePath.getParent();

        File file = new File(parentPath.toString() + "/output_file.json");
        try (FileWriter writer = new FileWriter(file, true)) {
            gson.toJson(StatisticalFolder, writer);
            System.out.println("JSON data written to output.json");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static class NullExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            try {
                String fieldName = f.getName(); 
                Class<?> declaringClass = f.getDeclaringClass(); 
                java.lang.reflect.Field field = declaringClass.getDeclaredField(fieldName); 
                field.setAccessible(true); 
                Object obj = f.getDeclaringClass().getDeclaredConstructor().newInstance(); 
                Object fieldValue = field.get(obj); 

                return fieldValue == null; 
            } catch (Exception e) {
                e.printStackTrace(); 
                return false; // Handle exceptions gracefully
            }
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false; 
        }
        
    }
}
