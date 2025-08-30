package localside;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import localside.listen.KeepAliveThread;
import localside.listen.SenderThread;

/**
 * Poorly named class after the design revisions we've gone through.
 * 
 * Contains consistent access to relevant data needed for various Threads to
 * operate in sync, initiation logic for starting the Threads and their operation,
 * and manages the dynamically present Connections.
 * 
 * This class is the core logic pivot between the user-visible side of things in
 * SocketControl and JavaSocket and the various Threads and other backend logic.
 * 
 */

public class ListenerPacket implements ConnectionsManager {
	
//---  Instance Variables   -------------------------------------------------------------------

	private volatile ServerSocket server;
	private volatile HashMap<String, Connection> clients;
	
	private KeepAliveThread listener;
	private KeepAliveThread timeOut;
	private SenderThread messageSender;
	
	private int listenPort;
	
	private int keepalive;
	
	private boolean quiet;
	
//---  Constructors   -------------------------------------------------------------------------
	
	public ListenerPacket(int inListen, int keepAliveTimer, boolean inQuiet) {
		listenPort = inListen;
		keepalive = keepAliveTimer;
		quiet = inQuiet;
		clients = new HashMap<String, Connection>();
	}
	
//---  Operations   ---------------------------------------------------------------------------
	
	public void startServer() {
		if(listenPort == -1) {
			print("Main Listening Socket not established due to no port (manual or random) being set");
			return;
		}
		try {
			server = new ServerSocket(listenPort == -2 ? 0 : listenPort);
			listenPort = server.getLocalPort();
			print("Main Server established with: " + server);
		}
		catch(Exception e) {
			print("Error in starting server for listening address: " + listenPort);
			e.printStackTrace();
		}
		
	}
	
	public void closeOutSession() {
		if(listener != null) {
			listener.end();
			listener.interrupt();
		}
		if(timeOut != null) {
			timeOut.end();
			timeOut.interrupt();
		}
		if(messageSender != null) {
			messageSender.end();
			messageSender.interrupt();
		}
		
		try {
			if(server != null && !server.isClosed()) {
				server.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		try {
			for(Connection c : clients.values()) {
				this.terminateConnection(c.getTitle());
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String title, String message) {
		if(!messageSender.getActiveStatus()) {
			messageSender.start();
		}
		this.getConnection(title).queueMessage(message);
	}
	
	public void distributeMessage(String message, String tag) {
		if(!messageSender.getActiveStatus()) {
			messageSender.start();
		}
		for(Connection c : clients.values()) {
			if(c.hasTag(tag)) {
				c.queueMessage(message);
			}
		}
	}
	
	public void distributeMessage(String message, ArrayList<String> tag) {
		if(!messageSender.getActiveStatus()) {
			messageSender.start();
		}
		for(Connection c : clients.values()) {
			for(String s : tag) {
				if(c.hasTag(s)) {
					c.queueMessage(message);
					break;
				}
			}
		}
	}
	
	public void distributeMessage(String message, ArrayList<String> tagInclusive, ArrayList<String> tagExclusive) {
		if(!messageSender.getActiveStatus()) {
			messageSender.start();
		}
		for(Connection c : clients.values()) {
			boolean fail = false;
			for(String s : tagInclusive) {
				if(!c.hasTag(s)) {
					fail = true;
				}
			}
			for(String s : tagExclusive) {
				if(c.hasTag(s)) {
					fail = true;
				}
			}
			if(!fail) {
				c.queueMessage(message);;
			}
		}
	}
	
	public void assignThreads(KeepAliveThread listen, KeepAliveThread time, KeepAliveThread sendThread) {
		listener = listen;
		timeOut = time;
		messageSender = (SenderThread)sendThread;
		setQuiet(quiet);
		if(keepalive != -1) {
			sendMessage("Handshake Protocol Initated, Sender Thread Spin Up Begun", Connection.TAG_ALL);
		}
	}
	
	@Override
	public void startConnection(String title) {
		Connection c = getConnection(title);
		if(c != null) {
			c.start();
		}
	}
	

	@Override
	public void terminateConnection(String title) {
		Connection c = getConnection(title);
		if(c != null) {
			c.end();
			c.interrupt();
		}
	}
	

	@Override
	public void addConnection(String title, Socket client) {
		clients.put(title, new Connection(client, title));
	}

	@Override
	public void addConnection(String title, int clientPort) throws Exception{
		clients.put(title, new Connection(clientPort, title));
	}

	@Override
	public void addTag(String title, String tag) {
		Connection c = getConnection(title);
		if(c != null) {
			c.addTag(tag);
		}
	}
	
	public void setQuiet(boolean shh) {
		listener.setQuiet(shh);
		timeOut.setQuiet(shh);
		messageSender.setQuiet(shh);
	}
	
//---  Getter Methods   -----------------------------------------------------------------------0
	
	public ServerSocket getServer() {
		return server;
	}
	
	public boolean isServerEstablished() {
		return server != null;
	}

	public int getServerPort() {
		return listenPort;
	}
	
	@Override
	public HashMap<String, Connection> getConnections() {
		return clients;
	}

	@Override
	public Collection<Connection> getConnectionList() {
		return clients.values();
	}

	@Override
	public Connection getConnection(String title) {
		return clients.get(title);
	}
	
//---  Support Methods   ----------------------------------------------------------------------
	
	public void print(String in) {
		if(!quiet) {
			System.out.println(in);
		}
	}



}
