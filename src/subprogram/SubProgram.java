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
	 * For any sub-program, we can define two ports of relevance: the listening port that this program
	 * will listen to for receiving messages, and the sending port that this program will use to send a
	 * message to the subprogram.
	 * 
	 * It sounds a little bit backwards but basically, the first port is how the subprogram talks to the
	 * Java host, the second port is how the Java host talks to the subprogram.
	 * 
	 * @param listenPort
	 * @param sendPort
	 */

	public abstract void initiateSubprogram(String listenPort, String sendPort);
	
	public abstract String getContext();
	
}
