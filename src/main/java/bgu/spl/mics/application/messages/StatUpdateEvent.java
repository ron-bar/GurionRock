package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class StatUpdateEvent implements Event<Void> {
    private final Object frame;
    private final String sensorName;

    public StatUpdateEvent(String sensorName, Object frame) {
        this.frame = frame;
        this.sensorName = sensorName;
    }

    public Object getFrame() {
        return frame;
    }

    public String getSensorName() {
        return sensorName;
    }
}
