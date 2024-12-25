package bgu.spl.mics.application.objects;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private static LiDarDataBase instance;
    private ConcurrentLinkedQueue<StampedCloudPoints> cloudPoints;

    private LiDarDataBase(String filePath){
        
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        if (instance==null){
            synchronized(LiDarDataBase.class){
                if(instance==null){
                    instance = new LiDarDataBase(filePath);
                }
            }
        }
        return instance;
    }

    public StampedCloudPoints retrieveCloudPoint(int time, String id){
        for(StampedCloudPoints stampedCP:this.cloudPoints){
            if(stampedCP.getId() == id && stampedCP.getTime() == time){
                return stampedCP;
            }
        }
        return null;
    }
    
}
