package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;


/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * <p>
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private final LiDarWorkerTracker lidarWorkerTracker;
    private int currentTick;
    private final PriorityQueue<ScheduledEvent> pendingEvents;
    private boolean error;
    private final CountDownLatch latch;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker, CountDownLatch latch) {
        super("LiDarService" + LiDarWorkerTracker.getId());
        this.lidarWorkerTracker = LiDarWorkerTracker;
        this.pendingEvents = new PriorityQueue<>(Comparator.comparingInt(ScheduledEvent::getProcessTime));
        this.latch = latch;
        error = false;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        MessageBusImpl.getInstance().register(this);

        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            sendBroadcast(new TerminatedBroadcast(this.getName()));
            terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if (terminatedBroadcast.getSenderName().equals("TimeService") || terminatedBroadcast.getSenderName().contains("LiDarService")) {
                if (terminatedBroadcast.getSenderName().contains("LiDarService"))
                    processEvents();
                sendBroadcast(new TerminatedBroadcast(this.getName()));
                terminate();
            }
        });

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            currentTick = tickBroadcast.getCurrentTick();
            processEvents();
        });

        subscribeEvent(DetectObjectsEvent.class, e -> {
            int detectionTime = e.getStampedDetectedObjects().getTime();
            int processTime = detectionTime + lidarWorkerTracker.getFrequency();
            pendingEvents.add(new ScheduledEvent(processTime, e));
            processEvents();
        });

        latch.countDown();
    }


    private void processEvents() {
        while (!pendingEvents.isEmpty() && currentTick >= pendingEvents.peek().getProcessTime() && !error) {
            ScheduledEvent e = pendingEvents.poll();
            processEvent(e.getEvent());
        }

        if (lidarWorkerTracker.getStatus() != STATUS.UP) {
            Broadcast b;
            if (lidarWorkerTracker.getStatus() == STATUS.ERROR)
                b = new CrashedBroadcast(lidarWorkerTracker.getName(), "Sensor disconnected");
            else
                b = new TerminatedBroadcast(getName());
            sendBroadcast(b);
            terminate();
        }
    }

    private void processEvent(DetectObjectsEvent event) {
        StampedDetectedObjects stampedObjects = event.getStampedDetectedObjects();
        List<TrackedObject> detection = lidarWorkerTracker.trackObjects(stampedObjects);

        if (lidarWorkerTracker.getStatus() == STATUS.ERROR) {
            this.error = true;
            return;
        }

        sendEvent(new StatUpdateEvent(lidarWorkerTracker.getName(), detection));
        sendEvent(new TrackedObjectsEvent(detection));
        complete(event, true);
    }

    private static class ScheduledEvent {
        private final DetectObjectsEvent event;
        private final int processTime;

        public ScheduledEvent(int processableTime, DetectObjectsEvent event) {
            this.processTime = processableTime;
            this.event = event;
        }

        public int getProcessTime() {
            return processTime;
        }

        public DetectObjectsEvent getEvent() {
            return event;
        }


    }
}
