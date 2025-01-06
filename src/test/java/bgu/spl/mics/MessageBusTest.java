package bgu.spl.mics;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.messages.TerminatedBroadcast;

class MessageBusTest {

    private MessageBusImpl messageBus;
    private MicroService microService;

    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getInstance();
        microService = new MicroService("TestService") {
            @Override
            protected void initialize() {
            }
        };
    }

    @Test
    void testSubscribeBroadcast() {
        Class<TerminatedBroadcast> broadcastType = TerminatedBroadcast.class;

        assertFalse(messageBus.isSubscribedToBroadcast(broadcastType, microService),
                "MicroService should not be subscribed yet");

        messageBus.register(microService);
        messageBus.subscribeBroadcast(broadcastType, microService);

        assertTrue(messageBus.isSubscribedToBroadcast(broadcastType, microService),
                "MicroService should be subscribed to the broadcast");
    }

    @Test
    void testRegister() {
        assertFalse(messageBus.isMicroServiceRegistered(microService),
                "MicroService should not be registered yet");

        messageBus.register(microService);

        assertTrue(messageBus.isMicroServiceRegistered(microService),
                "MicroService should be registered");
    }

    @Test
    void testUnregister() {
        messageBus.register(microService);

        assertTrue(messageBus.isMicroServiceRegistered(microService),
                "MicroService should be registered already");

        messageBus.unregister(microService);

        assertFalse(messageBus.isMicroServiceRegistered(microService),
                "MicroService should not be registered");
    }
}
