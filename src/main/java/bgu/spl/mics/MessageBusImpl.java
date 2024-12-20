package bgu.spl.mics;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

    private final ConcurrentHashMap<MicroService, ConcurrentLinkedQueue<Message>> msgQueues;
    private final ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentLinkedQueue<MicroService>> eventSubs;
    private final ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcastSubs;
    private final ConcurrentHashMap<Event<?>, Future<?>> eventFutures;


    private static class MessageBusImplHolder {
        private static final MessageBusImpl instance = new MessageBusImpl();
    }

    private MessageBusImpl() {
        msgQueues = new ConcurrentHashMap<>();
        eventSubs = new ConcurrentHashMap<>();
        broadcastSubs = new ConcurrentHashMap<>();
        eventFutures = new ConcurrentHashMap<>();
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        eventSubs.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>()).add(m);
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        broadcastSubs.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>()).add(m);
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        @SuppressWarnings("unchecked")
        Future<T> future = (Future<T>) eventFutures.remove(e);
        if (future != null)
            future.resolve(result);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        ArrayList<MicroService> snapshot;
        synchronized (broadcastSubs) {
            ConcurrentLinkedQueue<MicroService> subs = broadcastSubs.get(b.getClass());
            if (subs == null) return;
            snapshot = new ArrayList<>(subs);
        }

        for (MicroService m : snapshot) {
            ConcurrentLinkedQueue<Message> queue = msgQueues.get(m);
            if (queue != null){
                queue.add(b);
                queue.notifyAll();
            }
        }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {

        // finding the subscriber list for this event type
        ConcurrentLinkedQueue<MicroService> subs;
        synchronized (eventSubs) {
            subs = eventSubs.get(e.getClass());
            if (subs == null || subs.isEmpty())
                return null;
        }

        // finding the target microservice
        MicroService target;
        synchronized (subs) {
            target = subs.poll(); // remove first element
            if (target != null)
                subs.add(target); // re-add it to the back of the queue
        }

        if (target == null) return null;

        Future<T> future = new Future<>();
        ConcurrentLinkedQueue<Message> queue;
        synchronized (msgQueues) {
            queue = msgQueues.get(target);
            if (queue != null) { // make sure the microservice is still registered
                queue.add(e);
                queue.notifyAll();
                eventFutures.put(e, future);
                return future;
            }
        }
        return null;
    }

    @Override
    public void register(MicroService m) {
        msgQueues.putIfAbsent(m, new ConcurrentLinkedQueue<>());
    }

    @Override
    public void unregister(MicroService m) {
        msgQueues.remove(m);

        // we will synchronize separately to avoid unnecessary blocking
        synchronized (eventSubs) {
            for (ConcurrentLinkedQueue<MicroService> subs : eventSubs.values())
                subs.remove(m); // no need to check if it exists
        }
        synchronized (broadcastSubs) {
            for (ConcurrentLinkedQueue<MicroService> subs : broadcastSubs.values())
                subs.remove(m);
        }
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        ConcurrentLinkedQueue<Message> queue = msgQueues.get(m);
        if (queue == null)
            throw new IllegalStateException("MicroService is not registered");

        synchronized (queue) {
            while (queue.isEmpty())
                queue.wait();
            return queue.poll();
        }
    }

    public static MessageBusImpl getInstance() {
        return MessageBusImplHolder.instance;
    }


}
