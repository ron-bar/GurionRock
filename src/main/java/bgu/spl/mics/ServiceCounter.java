package bgu.spl.mics;

import java.util.concurrent.atomic.AtomicInteger;

public class ServiceCounter {
    private static final AtomicInteger serviceCounter = new AtomicInteger(0);

    public static void registerService() {
        serviceCounter.incrementAndGet();
    }

    public static void unregisterService() {
        serviceCounter.decrementAndGet();
    }

    public static int getServiceCount() {
        return serviceCounter.get();
    }

}
