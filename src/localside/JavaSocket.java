package localside;

import java.util.ArrayList;

import core.JavaReceiver;
import core.JavaTeardown;
import localside.listen.KeepAliveThread;
import localside.listen.ThreadGenerator;
import subprogram.SubProgram;

/**
 * 
 * JavaSocket class, an instance of a listening station on a particular port.
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

public class JavaSocket implements MessageSender {
	
//---  Constants   ----------------------------------------------------------------------------
	
	private final static int START_PORT = -1;
	
	private final static int TIMEOUT_PERIOD = 5000;
	
	private final static int KEEP_ALIVE_DEFAULT = -1;
	
	private final static int TIMING_DELAY = 5000;
	
	private static final int CONNECTION_FAILED_TIMEOUT = 15;
	
//---  Instance Variables   -------------------------------------------------------------------
	
	private JavaReceiver passTo;
	private SubProgram subProgram;
	
	private int currentListenPort;
	
	/** int value used to denote how long to wait for a message from the listened-to port before resetting the connection;
	 * a value of -1 denotes do not track last message received for timing out*/
	private int timeout;
	/** int value used to denote how frequently to send a keepalive message to the connected-to port;
	 * a value of -1 denotes do not send keep alive messages*/
	private int keepalive;
	/** int value used to denote */
	private int timingDelay;
	/** int value to denote how many failed attempts to set up a sender connection should be made before throwing an error;
	 * set to -1 to never stop trying to establish a connection*/
	private int connectionAttempts;
	
	private volatile ListenerPacket packet;
	
	private JavaTeardown terminate;
	
	private boolean quiet;
	
//---  Constructors   -------------------------------------------------------------------------
	
	public JavaSocket() {
		setListenPort(START_PORT);
		setTimeout(TIMEOUT_PERIOD);
		setTimingDelay(TIMING_DELAY);
		setKeepAlive(KEEP_ALIVE_DEFAULT);
		setSenderConnectionAttempts(CONNECTION_FAILED_TIMEOUT);
	}
	
	public JavaSocket(int listenPort, int timeout, int keepalive, int timingDelay, int connectAttempts) {
		setListenPort(listenPort);
		setTimeout(timeout);
		setKeepAlive(keepalive);
		setTimingDelay(timingDelay);
		setSenderConnectionAttempts(connectAttempts);
	}
	
	public JavaSocket(JavaReceiver reference) {
		passTo = reference;
		setListenPort(START_PORT);
		setTimeout(TIMEOUT_PERIOD);
		setTimingDelay(TIMING_DELAY);
		setKeepAlive(KEEP_ALIVE_DEFAULT);
		setSenderConnectionAttempts(CONNECTION_FAILED_TIMEOUT);
	}
	
	public JavaSocket(JavaReceiver reference, SubProgram runnable) {
		passTo = reference;
		subProgram = runnable;
		setListenPort(START_PORT);
		setTimeout(TIMEOUT_PERIOD);
		setTimingDelay(TIMING_DELAY);
		setKeepAlive(KEEP_ALIVE_DEFAULT);
		setSenderConnectionAttempts(CONNECTION_FAILED_TIMEOUT);
	}

//---  Operations   ---------------------------------------------------------------------------
	
	public void activate() throws Exception{
		print("\nActivating Java Socket Interface:");
		if(passTo == null) {
			throw new Exception("No receiver for data received via socket communication assigned, terminating");
		}
		if(currentListenPort != -1) {
			print("\n---Starting listener socket on port: " + (currentListenPort != -2 ? currentListenPort : "auto-selected"));
		}
		startLocalListener(passTo);
		if(currentListenPort != -1 && subProgram != null) {
			print("\n---Establishing subprogram: " + subProgram.getContext() + " that will connect to port " + currentListenPort);
			subProgram.initiateSubprogram("" + currentListenPort, quiet);
		}
	}

	public void deactivate() {
		if(packet != null) {
			packet.closeOutSession();
		}
	}

	@Override
	public void sendMessage(String title, String message) throws Exception {
		if(packet != null) {
			packet.sendMessage(title, message);
		}
		else {
			throw new Exception("Message Sending Capabilities Not Ready Yet, Please Try Again");
		}
	}
	
	@Override
	public void distributeMessage(String message, String tag) throws Exception {
		if(packet != null) {
			packet.distributeMessage(message, tag);
		}
		else {
			throw new Exception("Message Sending Capabilities Not Ready Yet, Please Try Again");
		}
	}
	
	@Override
	public void distributeMessage(String message, ArrayList<String> tag) throws Exception {
		if(packet != null) {
			packet.distributeMessage(message, tag);
		}
		else {
			throw new Exception("Message Sending Capabilities Not Ready Yet, Please Try Again");
		}
	}
	
	@Override
	public void distributeMessage(String message, ArrayList<String> tagInclusive, ArrayList<String> tagExclusive) throws Exception {
		if(packet != null) {
			packet.distributeMessage(message, tagInclusive, tagExclusive);
		}
		else {
			throw new Exception("Message Sending Capabilities Not Ready Yet, Please Try Again");
		}
	}
	
	private void startLocalListener(JavaReceiver reference){
		packet = new ListenerPacket(currentListenPort, keepalive, quiet);
		packet.startServer();
		
		if(packet.getServerPort() != -1) {
			currentListenPort = packet.getServerPort();
		}
		
		KeepAliveThread listen = ThreadGenerator.generateListeningThread(packet, reference, packet);
		
		KeepAliveThread timeOut = timeout == -1 ? null : ThreadGenerator.generateTimeOutThread(packet,  1000,  timeout, timingDelay);
		
		KeepAliveThread sender = ThreadGenerator.generateSenderThread(packet, keepalive);
		
		packet.assignThreads(listen, timeOut, sender);
				
		
		listen.start();
		
		if(timeOut != null) {
			timeOut.start();
		}
		
	}

	public void assignTearDown(JavaTeardown tear) {
		terminate = tear;
	}
	
//---  Setter Methods   -----------------------------------------------------------------------

	public void setListenPort(int in) {
		currentListenPort = in >= 2000 && in <= 9999 ? in : START_PORT;
	}
	
	public void setListenPortRandom() {
		currentListenPort = -2;
	}
	
	public void addSendPort(int in, String title) throws Exception {
		if(packet == null) {
			print("Socket instance: " + title + " not started yet");
			return;
		}
		int counter = 0;
		print("\nEstablishing Sender Connection to port: " + in);
		while(counter < connectionAttempts) {
			print("\n---Attempt to establish sender to port: " + in + " on connection attempt " + (counter + 1));
			try {
				packet.addConnection(title, in);
				packet.getConnection(title).setReceiver(passTo);
				packet.startConnection(title);
				print("\n---Sender Connection established to port: " + in);
				return;
			}
			catch(Exception e) {
				counter++;
			}
		}
		throw new Exception("Failure to establish Sender Connection on port: " + in + " after " + connectionAttempts + " attempts");
	}
	
	public void addSenderTag(String title, String tag){
		if(packet == null) {
			print("Socket instance: " + title + " not started yet");
			return;
		}
		packet.addTag(title, tag);
	}
	
	public void activateSendPort(String title) {
		packet.startConnection(title);
	}
	
	public void setTimeout(int in) {
		timeout = in < 0 ? -1 : in;
	}
	
	public void setKeepAlive(int in) {
		keepalive = in < 0 ? -1 : in;
	}
	
	public void setTimingDelay(int in) {
		timingDelay = in < 0 ? 0 : in;
	}
	
	public void setSenderConnectionAttempts(int in) {
		connectionAttempts = in < 0 ? -1 : in;
	}
	
	public void setReceiver(JavaReceiver in) {
		passTo = in;
	}
	
	public void setSubprogram(SubProgram in) {
		subProgram = in;
	}
	
	public void setQuiet(boolean shh) {
		quiet = shh;
		if(packet != null) {
			packet.setQuiet(shh);
		}
	}
	
//---  Getter Methods   -----------------------------------------------------------------------

	public int getCurrentListenPortNumber() {
		return currentListenPort;
	}
	
//---  Support Methods   ----------------------------------------------------------------------
	
	private void print(String in) {
		if(!quiet) {
			System.out.println(in);
		}
	}
	
}
