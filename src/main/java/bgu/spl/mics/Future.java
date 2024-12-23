package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	private T futureObject;
	
	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		this.futureObject = null;
		//TODO: implement this
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * 	       
     */
	public synchronized T get() {
		while(!this.isDone()){
			try{
				this.wait();
			}
			catch(InterruptedException e){
				break;
			}
		}
		return futureObject;
		//TODO: implement this.
		//return null;
	}
	
	/**
     * Resolves the result of this Future object.
     */
	public synchronized void resolve (T result) {
		this.futureObject = result;
		this.notifyAll();
		//TODO: implement this.
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */
	public boolean isDone() {
		return (this.futureObject!=null);
		//TODO: implement this.
		//return false;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	public synchronized T get(long timeout, TimeUnit unit) {
		if(!this.isDone()){
			try{
				unit.timedWait(this, timeout);
			}
			catch(InterruptedException e){
		
			}
		}
			return this.futureObject;
	}
		//TODO: implement this.
		//return null;

}
