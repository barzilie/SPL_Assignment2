package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast{
    private int crashTime;

    public CrashedBroadcast(int time){
        this.crashTime = time;
    }

    public int getCrashTime(){
        return this.crashTime;
    }
    
}
