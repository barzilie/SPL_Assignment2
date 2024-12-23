package bgu.spl.mics;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	private static MessageBusImpl instance;
	private Map<MicroService, BlockingQueue<Message>> microServiceQueues;
	private Map<Class<? extends Event>, LinkedList<MicroService>> eventSubscribers;
	private Map<Class<? extends Broadcast>, List<MicroService>> broadcastSubscribers;
	//private Map<Event, Future> futures;
	//private Map<Class<? extends Event>,Integer> roundRobinEventLocation;

	private MessageBusImpl() {
		microServiceQueues = new HashMap<>();
		eventSubscribers = new HashMap<>();
		broadcastSubscribers = new HashMap<>();
		//futures = new HashMap<>();
		//roundRobinEventLocation = new HashMap<>();
	}
	
	//singleton implementation
	public static MessageBusImpl getInstance() {
		if (instance == null) {
			synchronized (MessageBusImpl.class) {
				if (instance == null) { //double check for an edge case
					instance = new MessageBusImpl();
				}
			}
		}
		return instance;
	}
	

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized(eventSubscribers){ //why synchronized?
			List<MicroService> subscribedMicroServices = eventSubscribers.computeIfAbsent(type, k -> new LinkedList<>());
			subscribedMicroServices.add(m);
		}

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized(broadcastSubscribers){ //why synchronized?
			List<MicroService> subscribedMicroServices = broadcastSubscribers.computeIfAbsent(type, k -> new LinkedList<>());
			subscribedMicroServices.add(m);
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		List<MicroService> recepients = broadcastSubscribers.get(b.getClass());
		for(MicroService ms: recepients){
			microServiceQueues.get(ms).add(b);
		}
		
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		if(eventSubscribers.containsKey(e.getClass())) return null;
		LinkedList<MicroService> recepients = eventSubscribers.get(e.getClass());
		if(recepients.isEmpty()) return null;
		MicroService receiver = roundRobin(recepients);//dont forget to sync this method
		microServiceQueues.get(receiver).add(e);
		e.getRecievier
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(MicroService m) {
		microServiceQueues.put(m, new LinkedBlockingQueue<Message>());
	}

	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	//private round-robin implementation
	private MicroService roundRobin(LinkedList<MicroService> llist){
		MicroService ms = llist.remove();
		llist.addLast(ms);
		return ms;
	}

	

}
