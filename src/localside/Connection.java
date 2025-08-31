package localside;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import core.JavaReceiver;
import localside.listen.KeepAliveThread;

/**
 * Class that represents a connection to a particular client/end-point.
 * 
 * Maintains meta-data about that connection and contains methods for managing a
 * message queue to send to that end-point.
 * 
 * Each Connection has access to the Socket object and can write to that Socket.
 * 
 * We denote a Connection's identity by a labeled title and a list of tags.
 * 
 * Each Connection needs a listening thread to receive data from the Socket, so for now
 * this class is also a KeepAliveThread to be able to do that.
 * 
 * We automatically assign a tag based on the constructor used; if a Connection
 * is made by being given a target Port to connect to, it is a TAG_SENDER. If a
 * connection is provided a Client socket from a ServerSocket.accept() function,
 * it is a TAG_CLIENT.
 * 
 * The notion is that we have expected behaviors for the program based on whether
 * another service connects to us vs. us initiating a connection to another service.
 * 
 */

public class Connection extends KeepAliveThread{

	/** The Sender tag indicates a connection is a result of this program establishing a novel
	 *  connection to another service*/
	public final static String TAG_SENDER = "sender";
	/** The Client tag indicates a connection is a result of another service connecting to this
	 * 	program's listening Server.*/
	public final static String TAG_CLIENT = "client";
	
	public final static String TAG_ALL = "ALL";
	
	public final static String INDICATE_TAG_REQUEST = "REQUEST TAG";
	
	public final static String INDICATE_TAG_SENDING = "TAG LIST";
	
	public final static String INDICATE_TAG_SUCCESS = "TAG RECEIVED";
	
//---  Instance Variables   -------------------------------------------------------------------
	
	private Socket client;
	
	private String title;
	
	private ArrayList<String> tags;
	
	private volatile LinkedList<String> messageQueue;
	
	private volatile BufferedWriter writer;
	
	private JavaReceiver reference;
	
	private volatile long lastReceived;
	
	private volatile long birthTime;
	
	private volatile boolean tagSatisfied;
	
//---  Constructors   -------------------------------------------------------------------------
	
	/**
	 * 
	 * This constructor indicates we are creating a Client who has connected to our listening
	 * server, as we have a Socket object from Server.accept().
	 * 
	 * We tag it as a Client to denote it is someone who has connected to us.
	 * 
	 * A Client needs to tell us any relevant tags it has by sending a message prefixed
	 * with INDICATE_TAG_SENDING to us; we send it an INDICATE_TAG_REQUEST if we have not
	 * received any tags yet.
	 * 
	 * @param inClient
	 * @param inTitle
	 */
	
	public Connection(Socket inClient, String inTitle) {
		print("\nEstablishing new Server Side Connection as Client: " + inClient);
		client = inClient;
		title = inTitle;
		lastReceived = 0L;
		tags = new ArrayList<String>();
		tags.add(TAG_CLIENT);
		tags.add(TAG_ALL);
		messageQueue = new LinkedList<String>();
		queueMessage(INDICATE_TAG_REQUEST);
		birthTime = System.currentTimeMillis();
		establishWriter();
	}
	
	/**
	 * Alt constructor that takes a port number; this constructor is meant to establish
	 * a Connection that is being made to an existing Server port as a Client.
	 * 
	 * We tag it as a Sender to denote that it will send messages that originate from us to the
	 * target port address. It should send a message formatted as INDICATE_TAG_SENDING followed
	 * by a list of its tags when it receives a message of INDICATE_TAG_REQUEST.
	 * 
	 * @param targetPort
	 * @param inTitle
	 * @throws Exception
	 */
	
	public Connection(int targetPort, String inTitle) throws Exception{
		print("\nEstablishing new Sender Connection to Port: " + targetPort);
		client = new Socket("127.0.0.1", targetPort);
		title = inTitle;
		lastReceived = 0L;
		tags = new ArrayList<String>();
		tags.add(TAG_SENDER);
		tags.add(TAG_ALL);
		messageQueue = new LinkedList<String>();
		sendTagData();
		birthTime = System.currentTimeMillis();
		establishWriter();
	}
	
	public void establishWriter() {
		try {
			if(writer != null) {
				writer.close();
			}
			if(client != null && !client.isClosed()) {
				print("\n---Establishing Writer for Connection: " + this);
				writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//---  Operations   ---------------------------------------------------------------------------
	
	//-- Listening Mechanism  ---------------------------------
	
	@Override
	public void run() {
		try {
			BufferedReader receiver = new BufferedReader(new InputStreamReader(client.getInputStream()));
			print("\n---!!!---New Connection Started With Identity: " + getIdentity());
			String received = receiver.readLine();
			while(received != null && !received.equals("exit") && getKeepAliveStatus()) {
				//print("\n---" + getIdentity() + " received: '" + received + "'");
				if(received.equals(INDICATE_TAG_REQUEST)) {
					sendTagData();
				}
				else if(received.startsWith(INDICATE_TAG_SENDING)) {
					processTagData(received);
					queueMessage(INDICATE_TAG_SUCCESS);
				}
				else if(received.equals(INDICATE_TAG_SUCCESS)) {
					tagSatisfied = true;
				}
				else if(!received.equals("")) {
					reference.receiveSocketData(received, tags);
				}
				if(!tagSatisfied) {
					queueMessage(INDICATE_TAG_REQUEST);
				}
				received = receiver.readLine();
				lastReceived = System.currentTimeMillis();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendTagData() {
		ArrayList<String> out = new ArrayList<String>();
		for(String s : tags) {
			if(!s.equals(Connection.TAG_CLIENT) && !s.equals(Connection.TAG_SENDER)) {
				out.add(s);
			}
		}
		queueMessage(INDICATE_TAG_SENDING + " " + out.toString());
		print("Connection " + this + " queueing message to send Tag data: " + out.toString());
	}
	
	private void processTagData(String in) {
		String[] parts = in.split(INDICATE_TAG_SENDING);
		String relevant = parts[1].trim();
		if(relevant.startsWith("[")) {
			relevant = relevant.substring(1, relevant.length() - 1);
		}
		String[] use = relevant.split(relevant.contains(",") ? ", " : " ");
		for(String s : use) {
			if(!tags.contains(s)) {
				tags.add(s);
			}
		}
		tagSatisfied = true;
		queueMessage(INDICATE_TAG_SUCCESS);
	}
	
	@Override
	public void end() {
		super.end();
		try {
			client.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean checkInitiated() {
		return lastReceived != 0L;
	}
	
	public boolean initiationTimeout(long connectionDelayLimit) {
		return (System.currentTimeMillis() - birthTime > connectionDelayLimit) && lastReceived == 0L;
	}
	
	public boolean checkTimedOut(long timeCap) {
		return (System.currentTimeMillis() - lastReceived < timeCap) || lastReceived == new Long(0);
	}
	
	//-- Sending Mechanism  -----------------------------------
	
	public void sendViableMessage() throws Exception{
		String message = getNextMessage();
		if(message != null) {
			writer.write(message);
			writer.newLine();
			writer.flush();
			iterateNextMessage();
		}
	}

	private void iterateNextMessage() {
		messageQueue.poll();
	}
	
	public void queueMessage(String in) {
		print("\nMessage queued: " + in + " via Connection Client " + this.getSocket().getLocalPort());
		messageQueue.add(in);
	}
	
	public void addTag(String in) {
		tags.add(in);
		sendTagData();
	}
	
	
//---  Getter Methods   -----------------------------------------------------------------------
	
	public Socket getSocket() {
		return client;
	}
	
	public String getTitle() {
		return title;
	}
	
	public ArrayList<String> getTags(){
		return tags;
	}
	
	public boolean hasMessage() {
		return messageQueue.size() > 0;
	}
	
	private String getNextMessage() {
		if(messageQueue.size() > 0) {
			return messageQueue.peek();
		}
		return null;
	}
	
	public String getIdentity() {
		return title + ", " + client + ", " + tags;
	}

	public long getLastReceived() {
		return lastReceived;
	}
	
	public boolean hasTag(String inTag) {
		return tags.contains(inTag);
	}
	
//---  Setter Methods   -----------------------------------------------------------------------

	public void setReceiver(JavaReceiver in) {
		reference = in;
	}
	
	public void forceSetLastReceived() {
		lastReceived = System.currentTimeMillis();
	}
	
	@Override
	public String toString() {
		return getIdentity();
	}
	
}
