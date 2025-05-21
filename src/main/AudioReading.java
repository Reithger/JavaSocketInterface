package main;

import java.io.IOException;

/**
 * 
 * AudioReading class, the primary interface for the JavaPythonSocketTalk library.
 * 
 * This class manages the details of running a Java-end socket and calling the Python program (which
 * we assume will take an argument in the first position that is the matching socket number and set
 * up a sending-socket that our Java-socket listens to).
 * 
 * Simply instantiate this class with the file path to your Python file and provide a class that
 * has implemented the JavaReceiver interface and this should automate the regular sending of data
 * to your Java program from a support Python program.
 *
 * Just in case your Python program requires additional inputs from the command line to function, you
 * may also include a String array (or var args) as a third argument to the constructor which will be
 * passed AFTER the port number to your Python program.
 * 
 * We call your python program as the command line instruction "python [file_path][file_name.py] [port #] [arg1] [arg2] ..."
 *
 * The keyword 'exit' is used to end the communication between Java and Python sockets; if the Python program
 * sends the Java socket 'exit' it closes things down.
 * 
 * A 5-second timeout period is used to decide if the communication between sockets has broken down and requires
 * resetting; if this occurs, it automatically terminates the connection and starts a new one on the next port
 * number.
 *
 */

public class AudioReading implements InitiateListening{
	
//---  Constants   ----------------------------------------------------------------------------
	
	private final static int START_PORT = 5439;
	
	private final static int TIMEOUT_PERIOD = 5000;
	
//---  Instance Variables   -------------------------------------------------------------------
	
	private JavaReceiver passTo;
	private int currentPort;
	private volatile ListenerPacket packet;
	private String pythonPath;
	private String[] args;
	
//---  Constructors   -------------------------------------------------------------------------
	
	public AudioReading(String pythonFilePath, JavaReceiver reference, String ... inArgs) {
		pythonPath = pythonFilePath;
		args = inArgs;
		passTo = reference;
		currentPort = START_PORT;
		setUpListening();
	}
	
//---  Operations   ---------------------------------------------------------------------------
	
	/**
	 * This function is called automatically by lower-level classes in this package and should
	 * not be called directly by a programmer using this package.
	 * 
	 * This effectively restarts the current listening session and iterates the port number forward,
	 * so if you did need to reset the listening manually, you could call this.
	 * 
	 */
	
	public void setUpListening() {
		iteratePortNumber();
		if(packet != null) {
			packet.closeOutSession();
		}
		startLocalListener(passTo);
		callPythonCommunication();
	}
	
	/**
	 * Interrupts and ends the active Threads to stop the communication between the Java and
	 * Python sockets.
	 * 
	 */
	
	public void closeListening() {
		if(packet != null) {
			packet.closeOutSession();
		}
	}

	private void iteratePortNumber() {
		currentPort++;
		if(currentPort > 7500) {
			currentPort = 3500;
		}
	}
	
	private void startLocalListener(JavaReceiver reference){
		packet = new ListenerPacket();
		
		ListeningThread listen = new ListeningThread(packet, reference, currentPort);
		
		TimeOutThread timeOut = new TimeOutThread(packet, this, 1000, TIMEOUT_PERIOD);
		
		packet.assignThreads(listen, timeOut);
		
		listen.start();
		timeOut.start();
	}
	
	private void callPythonCommunication() {
		StringBuilder command = new StringBuilder();
		command.append("python " + pythonPath + " " + currentPort);
		for(String s : args) {
			command.append(" " + s);
		}
		try {
			Runtime.getRuntime().exec(command.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//---  Setter Methods   -----------------------------------------------------------------------

	public void setCurrentPortNumber(int in) {
		currentPort = in;
	}
	
//---  Getter Methods   -----------------------------------------------------------------------

	public int getCurrentPortNumber() {
		return currentPort;
	}


}
