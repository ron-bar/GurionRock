package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private final int tickTime;
    private final int duration;
    private final long tickMultiplier = 1000L;
    private final AtomicInteger currentTick;
    private final AtomicBoolean crashed;

    /**
     * Constructor for TimeService.
     *
     * @param TickTime The duration of each tick in milliseconds.
     * @param Duration The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.tickTime = TickTime;
        this.duration = Duration;
        this.currentTick = new AtomicInteger(0);
        this.crashed = new AtomicBoolean(false);
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        MessageBusImpl.getInstance().register(this);

        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            String senderName = terminatedBroadcast.getSenderName();
            if (senderName.equals(getName()) || senderName.contains("FusionSlam")) {
                stopClock();
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast ->
        {
            this.crashed.set(true);
            stopClock();
            sendBroadcast(new TerminatedBroadcast(getName()));
            terminate();
        });

        new Thread(() -> {
            try {
                while (currentTick.get() < duration) {
                    currentTick.incrementAndGet();
                    sendBroadcast(new TickBroadcast(currentTick.get()));
                    System.out.println("Tick: " + currentTick); // DEBUG
                    Thread.sleep(tickTime * tickMultiplier);
                }
                if (!crashed.get())
                    sendBroadcast(new TerminatedBroadcast(this.getName()));
            } catch (InterruptedException ignored) {

            }
        }).start();
    }

    private void stopClock() {
        this.currentTick.set(Integer.MAX_VALUE);
    }
}
