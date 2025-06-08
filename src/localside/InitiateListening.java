package localside;

public interface InitiateListening {

	/**
	 * 
	 * This function instructs the implementing class to set up the listening environment for
	 * the current port number; this is assumed to be called by the keep-alive threads when
	 * the socket connection has gone down and needs to be restarted, so a lower-level context
	 * can communicate to a higher-level context and request a restart of processes.
	 * 
	 * Basically, implement this in a class as the function where you set up your sockets and
	 * the listening/timeout threads because this should be the function that gets an entirely new
	 * instance of socket communication spun up.
	 * 
	 * This interface really only exists so that I didn't have to give TimeOutThread access to the
	 * entire JavaPythonSocket class and could instead give it only one 'button' to press.
	 * 
	 */
	
	public abstract void setUpListening();
	
}
