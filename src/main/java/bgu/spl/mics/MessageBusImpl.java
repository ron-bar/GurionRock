package bgu.spl.mics;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

    private final ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> msgQueues;
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
            LinkedBlockingQueue<Message> queue = msgQueues.get(m);
            if (queue != null) {
                queue.add(b);
            }
        }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        // finding the subscriber list for this event type
        ConcurrentLinkedQueue<MicroService> subs = eventSubs.get(e.getClass());
        if (subs == null || subs.isEmpty())
            return null;

        // finding the target microservice
        MicroService target;
        synchronized (subs) {
            target = subs.poll(); // remove first element
            if (target != null)
                subs.add(target); // re-add it to the back of the queue
        }
        if (target == null) return null;

        Future<T> future = new Future<>();
        LinkedBlockingQueue<Message> queue = msgQueues.get(target);
        if (queue == null)
            return null;
        queue.add(e);
        eventFutures.put(e, future);
        return future;

    }

    @Override
    public void register(MicroService m) {
        msgQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
    }

    @Override
    public void unregister(MicroService m) {
        msgQueues.remove(m);

        // synchronize separately to avoid unnecessary blocking
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
        LinkedBlockingQueue<Message> queue = msgQueues.get(m);
        if (queue == null)
            throw new IllegalStateException("MicroService is not registered");
        return queue.take();
    }

    public static MessageBusImpl getInstance() {
        return MessageBusImplHolder.instance;
    }


}
