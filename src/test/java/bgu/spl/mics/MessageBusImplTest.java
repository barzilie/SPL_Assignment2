package bgu.spl.mics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

import bgu.spl.mics.*;
import org.junit.jupiter.api.*;
import java.util.concurrent.*;
import static org.mockito.Mockito.*;


import static org.mockito.Mockito.*;


// public class MessageBusImplTest {
// 	private static MessageBusImpl instance; 

	//Set up for a test.
     
//     @BeforeEach 
// 	public void setUp() throws Exception {
//         MessageBusImpl mb = MessageBusImpl.getInstance();
//     }

// 	@Test
// 	public void testGetInstance() {

// 	}

// 	@Test
// 	public void testSubscribeEvent() {
// 	//	this.instance.subscribeEvent(new Event() {;},new MicroService("Test1") )
// 	//	getEventSubscribers
// 	}

// 	@Test
// 	public void testSubscribeBroadcast() {
// 		// TODO Auto-generated method stub

// 	}

// 	@Test
// 	public void testComplete() {
// 		// TODO Auto-generated method stub

// 	}

// 	@Test
// 	public void testSendBroadcast() {
// 		// TODO Auto-generated method stub

// 	}

	
// 	@Test
// 	public void testSendEvent() {
// 		// TODO Auto-generated method stub
// 	}

// 	@Test
// 	public void testRegister() {
// 		// TODO Auto-generated method stub

// 	}

// 	@Test
// 	public void testUnregister() {
// 		// TODO Auto-generated method stub

// 	}

// 	@Test
// 	public void testAwaitMessage() throws InterruptedException {
// 		// TODO Auto-generated method stub
// 	}

// 	@Test
// 	public void testRoundRobin(){

// 	}

	

// }




//AMIT second option

class MessageBusImplTest{

    private MessageBusImpl messageBus;
    private MicroService testService1;
    private MicroService testService2;

    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getInstance();
        testService1 = mock(MicroService.class);
        testService2 = mock(MicroService.class);
        when(testService1.getName()).thenReturn("TestService1");
        when(testService2.getName()).thenReturn("TestService2");
    }

    @AfterEach
    void tearDown() {
        messageBus.unregister(testService1);
        messageBus.unregister(testService2);
    }

    @Test
    void testRegisterAndUnregister() {
        messageBus.register(testService1);
        assertTrue(messageBus.getMicroServiceQueues().containsKey(testService1));

        messageBus.unregister(testService1);
        assertFalse(messageBus.getMicroServiceQueues().containsKey(testService1));
    }

    @Test
    void testSubscribeAndSendEvent() throws InterruptedException {
        class TestEvent implements Event<String> {}

        TestEvent event = new TestEvent();
        Future<String> future;

        messageBus.register(testService1);
        messageBus.subscribeEvent(TestEvent.class, testService1);

        future = messageBus.sendEvent(event);
        assertNotNull(future);

        Message message = messageBus.awaitMessage(testService1);
        assertEquals(event, message);

        messageBus.complete(event, "Result");
        assertEquals("Result", future.get());
    }

    @Test
    void testSubscribeAndSendBroadcast() throws InterruptedException {
        class TestBroadcast implements Broadcast {}

        TestBroadcast broadcast = new TestBroadcast();

        messageBus.register(testService1);
        messageBus.register(testService2);
        messageBus.subscribeBroadcast(TestBroadcast.class, testService1);
        messageBus.subscribeBroadcast(TestBroadcast.class, testService2);

        messageBus.sendBroadcast(broadcast);

        Message message1 = messageBus.awaitMessage(testService1);
        Message message2 = messageBus.awaitMessage(testService2);

        assertEquals(broadcast, message1);
        assertEquals(broadcast, message2);
    }

    @Test
    void testAwaitMessageThrowsException() {
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(testService1));
    }

    @Test
    void testRoundRobinForEvents() throws InterruptedException {
        class TestEvent implements Event<String> {}

        TestEvent event1 = new TestEvent();
        TestEvent event2 = new TestEvent();

        messageBus.register(testService1);
        messageBus.register(testService2);
        messageBus.subscribeEvent(TestEvent.class, testService1);
        messageBus.subscribeEvent(TestEvent.class, testService2);

        messageBus.sendEvent(event1);
        messageBus.sendEvent(event2);

        Message message1 = messageBus.awaitMessage(testService1);
        Message message2 = messageBus.awaitMessage(testService2);

        assertEquals(event1, message1);
        assertEquals(event2, message2);
    }
}
