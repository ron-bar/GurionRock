package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.concurrent.CountDownLatch;

public class StatisticsService extends MicroService {
    private final StatisticalFolder stat;
    private final CountDownLatch latch;

    public StatisticsService(CountDownLatch latch) {
        super("StatisticsService");
        stat = new StatisticalFolder();
        this.latch = latch;
    }

    @Override
    protected void initialize() {
        MessageBusImpl.getInstance().register(this);

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> stat.incrementTime());

        subscribeEvent(StatUpdateEvent.class, event -> stat.updateStats(event.getSensorName(), event.getFrame()));

        subscribeEvent(StatFinalEvent.class, event -> {
            stat.setLandMarks(event.getLandmarks());
            CrashedBroadcast b = event.getCrash();
            if (b != null)
                stat.generateOutput(b.getFaultySensor(), b.getDescription());
            else
                stat.generateOutput();

            sendBroadcast(new TerminatedBroadcast(getName()));
            terminate();
        });

        this.latch.countDown();
    }

}
