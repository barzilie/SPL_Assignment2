package bgu.spl.mics;
import java.time.Duration;
import java.time.Instant;
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
	private ConcurrentHashMap<MicroService, BlockingQueue<Message>> microServiceQueues; //stores message queues for each MicroService
	private ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentLinkedQueue<MicroService>> eventSubscribers; //stores subscribers for different event types
	private ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcastSubscribers; //stores subscribers for different broadcast types
	@SuppressWarnings("rawtypes")
	private ConcurrentHashMap<Event, Future> futures; //stores futures for each event

    private static class MessageBusImplHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }

	private MessageBusImpl() {
		microServiceQueues = new ConcurrentHashMap<>();
		eventSubscribers = new ConcurrentHashMap<>();
		broadcastSubscribers = new ConcurrentHashMap<>();
		futures = new ConcurrentHashMap<>();
	}

	public static MessageBusImpl getInstance(){
        return MessageBusImplHolder.instance;
    }

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		ConcurrentLinkedQueue<MicroService> subscribedMicroServices = eventSubscribers.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>());
		System.out.println("contain the next event: " + eventSubscribers.containsKey(type));
		System.out.println("type "+type.toString());
		subscribedMicroServices.add(m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		ConcurrentLinkedQueue<MicroService> subscribedMicroServices = broadcastSubscribers.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>());
		System.out.println("contain the next event: " + broadcastSubscribers.containsKey(type));
		System.out.println("type "+type.toString());
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
		if(broadcastSubscribers.containsKey(b.getClass())){
			ConcurrentLinkedQueue<MicroService> recepients = broadcastSubscribers.get(b.getClass());
			if(recepients != null && !recepients.isEmpty()){
				synchronized(recepients){
					Instant start = Instant.now();
					System.out.println("BROADLOCKING recepients for: " + Thread.currentThread().getName());
					if(recepients != null && !recepients.isEmpty()){
						for(MicroService ms: recepients){
							BlockingQueue<Message> q = microServiceQueues.get(ms);
							synchronized(q){
								q.add(b);
								q.notifyAll(); 
							}	
						}
					}
					Instant finish = Instant.now();
					long timeElapsed = Duration.between(start, finish).toMillis();
					System.out.println(timeElapsed + " MILISECONDSBROAD");
				}
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		if(eventSubscribers.containsKey(e.getClass())){
			System.out.println(" entered first if send event: " + Thread.currentThread());
			ConcurrentLinkedQueue<MicroService> recepients = eventSubscribers.get(e.getClass());
			if(recepients != null && !recepients.isEmpty()){
				MicroService ms = null;
				//added synchronized for recepients for safety of termination
				synchronized(recepients){
					Instant start = Instant.now();
					System.out.println("LOCKING recepients for: " + Thread.currentThread().getName());
					if(recepients != null && !recepients.isEmpty()){
						ms = roundRobin(recepients);
					}
					Instant finish = Instant.now();
					long timeElapsed = Duration.between(start, finish).toMillis();
					System.out.println(timeElapsed + " MILISECONDS");
				}
				Future<T> f = new Future<>();
				futures.put(e, f);
				if(ms!=null){
					BlockingQueue<Message> q = microServiceQueues.get(ms);
					if(q!=null){
						synchronized(q){ 
							if(q!=null){
								System.out.println(Thread.currentThread()+": "+e.getClass()+" sent to:"+ ms.getName());
								q.add(e);
								//q.notifyAll(); //notifies the waiting thread that the queue is no longer empty
							}
						}
					}
				}

				return f;
			}
			System.out.println("not entered SECOND ONLY if send event: " + Thread.currentThread()+" : "+e.getClass());
		}
		System.out.println(e.getClass().toString());
		System.out.println("not entered 2 if send event: " + Thread.currentThread());
		complete(e, null);
		return null;
	}

	@Override
	public void register(MicroService m) {
		microServiceQueues.put(m, new LinkedBlockingQueue<Message>());
		System.out.println(m.getClass() + ": registered to bus");
		//do we need to cunscribe here to event\broadcast ? 
	}

	// public void unregister(MicroService m) { 
    //     for(ConcurrentLinkedQueue<MicroService> q: eventSubscribers.values()){
	// 		if(q.contains(m)){
	// 			synchronized(q){
	// 				q.remove(m);
	// 			}
	// 		}
	// 	}
	// 	for(ConcurrentLinkedQueue<MicroService> q: broadcastSubscribers.values()){
	// 		if(q.contains(m)){
	// 			synchronized(q){
	// 				q.remove(m);
	// 			}
	// 		}
	// 	}
	// 	//since no events or broadcasts are subscribed by the MicroService - no one will reach it's queue so no need to synchronize
    //     microServiceQueues.remove(m);
    // }
	
	@Override
	public void unregister(MicroService m) {
        synchronized (eventSubscribers) {
			eventSubscribers.forEach((eventType, microServiceList) -> microServiceList.remove(m));
        }
        synchronized(broadcastSubscribers){
            broadcastSubscribers.forEach((broadcastType,microServiceList) -> microServiceList.remove(m));
        }
        synchronized(microServiceQueues) {
			microServiceQueues.remove(m);
        }
    }


	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		try{
			BlockingQueue<Message> msgq = microServiceQueues.get(m);
			synchronized (m) {
				try {
					return msgq.take();
				} 
				catch (InterruptedException e) {
					throw new InterruptedException();
				}
			}
		}
		catch(NullPointerException e){
			throw new IllegalStateException();
		}
	}

	//private round-robin implementation
	private MicroService roundRobin(ConcurrentLinkedQueue<MicroService> microServiceQueue){
		synchronized(microServiceQueue){
			MicroService ms = microServiceQueue.remove();
			microServiceQueue.add(ms);
			return ms;
		}
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
