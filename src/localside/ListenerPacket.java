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
		try {
			server = new ServerSocket(listenPort);
			print("Server established with: " + server);
		}
		catch(Exception e) {
			print("Error in restarting server for listening address: " + listenPort);
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
	
	public void sendMessage(String message, String tag) {
		for(Connection c : clients.values()) {
			if(c.hasTag(tag)) {
				c.queueMessage(message);
			}
		}
	}
	
	public void sendMessage(String message, ArrayList<String> tag) {
		for(Connection c : clients.values()) {
			for(String s : tag) {
				if(c.hasTag(s)) {
					c.queueMessage(message);
					break;
				}
			}
		}
	}
	
	public void assignThreads(KeepAliveThread listen, KeepAliveThread time, KeepAliveThread sendThread) {
		listener = listen;
		timeOut = time;
		messageSender = (SenderThread)sendThread;
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
	
//---  Getter Methods   -----------------------------------------------------------------------0
	
	public ServerSocket getServer() {
		return server;
	}
	
	public void print(String in) {
		if(!quiet) {
			System.out.println(in);
		}
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



}
