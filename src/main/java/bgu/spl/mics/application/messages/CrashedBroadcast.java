package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {
    private final String faultySensor;
    private final String description;

    public CrashedBroadcast(String faultySensor, String description) {
        this.faultySensor = faultySensor;
        this.description = description;
    }

    public String getFaultySensor() {
        return faultySensor;
    }

    public String getDescription() {
        return description;
    }
}
