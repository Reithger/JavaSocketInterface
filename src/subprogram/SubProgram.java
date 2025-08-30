package subprogram;

/**
 * 
 * This interface provides a common definition of the behavior a subprogram class should be able to perform; basically,
 * that it can, given a port number, run the specified program and provide it that port number to communicate back to
 * this program context (a subprogram is provided to a JavaSocket instance which, on its set-up and re-setup, will call
 * on this class to initiate its subprogram; the assumption is that if communication is lost between JavaSocket and
 * the program initiated by this Subprogram then that subprogram is designed to end itself).
 * 
 * Basically: whatever program is called by SubProgram is ideally able to send data via socket to the given port; an
 * instance of JavaSocket will listen to that port.
 * 
 * That program should terminate itself if it loses communication with its associated JavaSocket instance.
 * 
 * That JavaSocket instance calls 'initiateSubprogram' whenever it sets up its listener threads; it does this on the same port
 * given to initiateSubprogram here.
 * 
 * 'initiateSubprogram' is called when the JavaSocket associated to this starts up and every time it loses connection and resets.
 * 
 * The assumption is that any program run through this interface exists solely to communicate to JavaSocket and can gracefully
 * terminate itself if the connection suddenly dies; re-establishing a connection is not possible (the port number changes)
 * 
 */

public interface SubProgram {
	
	/**
	 * 
	 * For any sub-program, we define one port of relevance: the port that this main server context
	 * is listening on which the sub-program will attempt to connect to for sending data
	 * back and forth.
	 * 
	 * @param listenPort
	 * @param quiet
	 */

	public abstract void initiateSubprogram(String listenPort, boolean quiet);
	
	public abstract String getContext();
	
}
