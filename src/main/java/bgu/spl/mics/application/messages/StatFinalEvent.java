package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.LandMark;

import java.util.List;

public class StatFinalEvent implements Event<Void> {
    private final List<LandMark> landmarks;
    private final CrashedBroadcast crashedBroadcast;

    public StatFinalEvent(List<LandMark> landmarks) {
        this.landmarks = landmarks;
        crashedBroadcast = null;
    }

    public StatFinalEvent(List<LandMark> landmarks, CrashedBroadcast crashedBroadcast) {
        this.landmarks = landmarks;
        this.crashedBroadcast = crashedBroadcast;
    }

    public List<LandMark> getLandmarks() {
        return landmarks;
    }

    public CrashedBroadcast getCrash() {
        return crashedBroadcast;
    }
}
