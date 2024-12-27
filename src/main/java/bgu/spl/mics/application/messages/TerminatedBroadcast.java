package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TerminatedBroadcast implements Broadcast{
    private String senderName;

    public TerminatedBroadcast(String name){
        this.senderName = name;
    }

    public String getSenderName() {
        return senderName;
    }
    
}
