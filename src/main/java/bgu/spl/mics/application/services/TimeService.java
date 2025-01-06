package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private int tickTime;
    private int duration;
    private int timeLeft;
    private StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();


    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.tickTime = TickTime;
        this.duration = Duration;
        this.timeLeft = Duration;
        System.out.println("total time: " + duration);
        initialize();
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        while(timeLeft>0 && !statisticalFolder.terminateClock()){
            sendBroadcast(new TickBroadcast());
            System.out.println("time left is " + timeLeft );
            statisticalFolder.increaseSystemRuntime();
            try{
                Thread.sleep(tickTime*1000);
            }catch(InterruptedException e){
                break;
            }
            timeLeft--;
        }
        System.out.println("clock terminated");
        terminate();

    }
}
