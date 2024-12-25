package bgu.spl.mics;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	private static MessageBusImpl instance; //holds the single instance
	//maybe split to two different hashmaps of broadcast and events
	private ConcurrentHashMap<MicroService, BlockingQueue<Message>> microServiceQueues; //stores message queues for each MicroService
	private ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentLinkedQueue<MicroService>> eventSubscribers; //stores subscribers for different event types
	private ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcastSubscribers; //stores subscribers for different broadcast types
	@SuppressWarnings("rawtypes")
	private ConcurrentHashMap<Event, Future> futures; //stores futures for each event
	//maybe add a lock object to the queue we are locking???
	//check about put method: throws exception

	private MessageBusImpl() {
		microServiceQueues = new ConcurrentHashMap<>();
		eventSubscribers = new ConcurrentHashMap<>();
		broadcastSubscribers = new ConcurrentHashMap<>();
		futures = new ConcurrentHashMap<>();
		//roundRobinEventLocation = new ConcurrentHashMap<>();
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
		ConcurrentLinkedQueue<MicroService> subscribedMicroServices = eventSubscribers.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>());
		subscribedMicroServices.add(m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		ConcurrentLinkedQueue<MicroService> subscribedMicroServices = broadcastSubscribers.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>());
		subscribedMicroServices.add(m);
	}

	@Override
	@SuppressWarnings("unchecked") 
	public <T> void complete(Event<T> e, T result) {
		if(futures.get(e) != null) futures.get(e).resolve(result);
		// futures.remove(e); //does it need to be removed?
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		//synchronized(broadcastSubscribers){} needed?
		if(broadcastSubscribers.containsKey(b.getClass())){
			ConcurrentLinkedQueue<MicroService> recepients = broadcastSubscribers.get(b.getClass());
			if(recepients != null && !recepients.isEmpty()){
				for(MicroService ms: recepients){
					BlockingQueue<Message> q = microServiceQueues.get(ms);
					synchronized(q){ //TODO what happens if already locked? maybe change to wait
						q.add(b);
						q.notifyAll(); //notifies the waiting thread that the queue is no longer empty
					}	
				}
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		//synchronized(eventSubscribers){} needed?
		ConcurrentLinkedQueue<MicroService> recepients = eventSubscribers.get(e.getClass());
		if(eventSubscribers.containsKey(e.getClass()) && recepients != null && !recepients.isEmpty()){
			Future<T> f = new Future<>();
			MicroService receiver = roundRobin(recepients);//dont forget to sync this method
			futures.put(e, f);
			BlockingQueue<Message> q = microServiceQueues.get(receiver);
			synchronized(q){ //TODO what happens if already locked? maybe change to wait
				q.add(e);
				q.notifyAll(); //notifies the waiting thread that the queue is no longer empty
			}
			return f;
		}
		complete(e, null);
		return null;
	}

	@Override
	public void register(MicroService m) {
		microServiceQueues.put(m, new LinkedBlockingQueue<Message>());
		//do we need to cunscribe here to event\broadcast ? 
	}

	@Override
	public void unregister(MicroService m) { 
        for(ConcurrentLinkedQueue<MicroService> q: eventSubscribers.values()){
			if(q.contains(m)){
				synchronized(q){
					q.remove(m);
				}
			}
		}
		for(ConcurrentLinkedQueue<MicroService> q: broadcastSubscribers.values()){
			if(q.contains(m)){
				synchronized(q){
					q.remove(m);
				}
			}
		}
		//since no events or broadcasts are subscribed by the MicroService - no one will reach it's queue so no need to synchronize
        microServiceQueues.remove(m);
    }


	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		BlockingQueue<Message> msgq = microServiceQueues.get(m);
		if(msgq!=null){
			synchronized(msgq){
				try{
					return msgq.take();
				}
				catch(InterruptedException e){
					throw new InterruptedException();
				}
			}
		}
		return null;
	}

	//private round-robin implementation
	private MicroService roundRobin(ConcurrentLinkedQueue<MicroService> microServiceQueue){
		MicroService ms = microServiceQueue.remove();
		microServiceQueue.add(ms);
		return ms;
	}

	//getters for testings 

	public Map<MicroService, BlockingQueue<Message>> getMicroServiceQueues(){
		return this.microServiceQueues;
	}
	
	public ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentLinkedQueue<MicroService>> getEventSubscribers(){
		return this.eventSubscribers;
	}

	public ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> getBroadcastSubscribers(){
		return this.broadcastSubscribers;
	}

	@SuppressWarnings("rawtypes")
	public Map<Event, Future> getFutures(){
		return this.futures;
	}

}
