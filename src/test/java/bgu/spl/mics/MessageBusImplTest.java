package bgu.spl.mics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;


public class MessageBusImplTest {
	private static MessageBusImpl instance; 

	//Set up for a test.
     
    @BeforeEach 
	public void setUp() throws Exception {
        MessageBusImpl mb = instance.getInstance();
    }

	@Test
	public void testGetInstance() {

	}

	@Test
	public void testSubscribeEvent() {
	//	this.instance.subscribeEvent(new Event() {;},new MicroService("Test1") )
	//	getEventSubscribers
	}

	@Test
	public void testSubscribeBroadcast() {
		// TODO Auto-generated method stub

	}

	@Test
	public void testComplete() {
		// TODO Auto-generated method stub

	}

	@Test
	public void testSendBroadcast() {
		// TODO Auto-generated method stub

	}

	
	@Test
	public void testSendEvent() {
		// TODO Auto-generated method stub
=	}

	@Test
	public void testRegister() {
		// TODO Auto-generated method stub

	}

	@Test
	public void testUnregister() {
		// TODO Auto-generated method stub

	}

	@Test
	public void testAwaitMessage() throws InterruptedException {
		// TODO Auto-generated method stub
=	}

	@Test
	public void testRoundRobin(){

	}

	

}
