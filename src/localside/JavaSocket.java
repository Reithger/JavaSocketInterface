package localside;

import core.JavaReceiver;
import localside.listen.InitiateListening;
import localside.listen.KeepAliveThread;
import localside.listen.MessageSender;
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

public class JavaSocket implements InitiateListening {
	
//---  Constants   ----------------------------------------------------------------------------
	
	private final static int START_PORT = 5439;
	
	private final static int TIMEOUT_PERIOD = 5000;
	
	private final static int KEEP_ALIVE_DEFAULT = -1;
	
//---  Instance Variables   -------------------------------------------------------------------
	
	private JavaReceiver passTo;
	private SubProgram subProgram;
	private int currentPort;
	
	/** int value used to denote how long to wait for a message from the listened-to port before resetting the connection;
	 * a value of -1 denotes do not track last message received for timing out*/
	private int timeout;
	/** int value used to denote how frequently to send a keepalive message to the connected-to port;
	 * a value of -1 denotes do not send keep alive messages*/
	private int keepalive;
	
	private volatile ListenerPacket packet;
	
//---  Constructors   -------------------------------------------------------------------------
	
	public JavaSocket() {
		setPort(START_PORT);
		setTimeout(TIMEOUT_PERIOD);
		setKeepAlive(KEEP_ALIVE_DEFAULT);
	}
	
	public JavaSocket(int port, int timeout, int keepalive) {
		setPort(port);
		setTimeout(timeout);
		setKeepAlive(keepalive);
	}
	
	public JavaSocket(JavaReceiver reference) {
		passTo = reference;
		setPort(START_PORT);
		setTimeout(TIMEOUT_PERIOD);
		setKeepAlive(KEEP_ALIVE_DEFAULT);
	}

	
	public JavaSocket(JavaReceiver reference, SubProgram runnable) {
		passTo = reference;
		subProgram = runnable;
		setPort(START_PORT);
		setTimeout(TIMEOUT_PERIOD);
		setKeepAlive(KEEP_ALIVE_DEFAULT);
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
		if(subProgram != null)
			subProgram.initiateSubprogram("" + currentPort);
	}
	
	public void sendMessage(String message) {
		if(packet != null) {
			packet.sendMessage(message);
		}
	}
	
	private void startLocalListener(JavaReceiver reference){
		packet = new ListenerPacket();
		
		KeepAliveThread listen = ThreadGenerator.generateListeningThread(packet, reference, currentPort);
		
		KeepAliveThread timeOut = timeout == -1 ? null : ThreadGenerator.generateTimeOutThread(packet, this,  1000,  timeout, subProgram == null ? null : subProgram.getContext());
		
		KeepAliveThread keepAlive = keepalive == -1 ? null : ThreadGenerator.generateSignOfLifeThread(packet, keepalive);
		
		packet.assignThreads(listen, timeOut, keepAlive);
				
		
		listen.start();
		
		if(timeOut != null) {
			timeOut.start();
		}
		
		if(keepAlive != null) {
			keepAlive.start();
		}
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

//---  Setter Methods   -----------------------------------------------------------------------

	public void setPort(int in) {
		currentPort = in >= 2000 && in <= 9999 ? in : START_PORT;
	}
	
	public void setTimeout(int in) {
		timeout = in < 0 ? -1 : in;
	}
	
	public void setKeepAlive(int in) {
		keepalive = in < 0 ? -1 : in;
	}
	
	public void setReceiver(JavaReceiver in) {
		passTo = in;
	}
	
	public void setSubprogram(SubProgram in) {
		subProgram = in;
	}
	
//---  Getter Methods   -----------------------------------------------------------------------

	public int getCurrentPortNumber() {
		return currentPort;
	}
	
	public MessageSender getMessageSender() {
		return packet;
	}

}
