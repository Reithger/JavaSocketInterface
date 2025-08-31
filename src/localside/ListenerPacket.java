package localside;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
			ArrayList<String> titles = new ArrayList<String>();
			reserveConnectionList();
			for(int i = 0; i < getConnectionList().size(); i++) {
				Connection c = getConnectionList().get(i);
				titles.add(c.getTitle());
			}
			releaseConnectionList();
			for(String s : titles) {
				terminateConnection(s);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String title, String message) throws Exception{
		if(!messageSender.getActiveStatus()) {
			messageSender.start();
		}
		messageSender.queueMessage(title, message);
	}
	
	public void distributeMessage(String message, String tag) {
		if(!messageSender.getActiveStatus()) {
			messageSender.start();
		}
		reserveConnectionList();
		for(Connection c : getConnectionList()) {
			if(c.hasTag(tag)) {
				try {
					messageSender.queueMessage(c.getTitle(), message);
				}
				catch(Exception e) {
				}
			}
		}
		releaseConnectionList();
	}
	
	public void distributeMessage(String message, ArrayList<String> tag) {
		if(!messageSender.getActiveStatus()) {
			messageSender.start();
		}
		reserveConnectionList();
		for(Connection c : getConnectionList()) {
			for(String s : tag) {
				if(c.hasTag(s)) {
					try {
						messageSender.queueMessage(c.getTitle(), message);
					}
					catch(Exception e) {
					}
					break;
				}
			}
		}
		releaseConnectionList();
	}
	
	public void distributeMessage(String message, ArrayList<String> tagInclusive, ArrayList<String> tagExclusive) {
		if(!messageSender.getActiveStatus()) {
			messageSender.start();
		}
		reserveConnectionList();
		for(Connection c : getConnectionList()) {
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
				try {
					messageSender.queueMessage(c.getTitle(), message);
				}
				catch(Exception e) {
					
				}
			}
		}
		releaseConnectionList();
	}
	
	public void assignThreads(KeepAliveThread listen, KeepAliveThread time, KeepAliveThread sendThread) {
		listener = listen;
		timeOut = time;
		messageSender = (SenderThread)sendThread;
		setQuiet(quiet);
		if(keepalive != -1) {
			distributeMessage("Handshake Protocol Initated, Sender Thread Spin Up Begun", Connection.TAG_ALL);
		}
	}
	
	@Override
	public void startConnection(String title) {
		Connection c = getConnection(title);
		if(c != null) {
			c.start();
			print("\n---Connection '" + title + "' listener thread started");
		}
		else {
			print("\n---Failed to start Connection, wrong title: " + title);
		}
	}
	

	@Override
	public void terminateConnection(String title) {
		Connection c = getConnection(title);
		if(c != null) {
			reserveConnectionList();
			clients.remove(c.getTitle());
			releaseConnectionList();
			c.end();
			c.interrupt();
		}
	}
	

	@Override
	public void addConnection(String title, Socket client) {
		Connection c = new Connection(client, title);
		c.setQuiet(quiet);
		reserveConnectionList();
		clients.put(title, c);
		releaseConnectionList();
	}

	@Override
	public void addConnection(String title, int clientPort) throws Exception{
		Connection c = new Connection(clientPort, title);
		c.setQuiet(quiet);
		reserveConnectionList();
		clients.put(title, c);
		releaseConnectionList();
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

	private volatile boolean mutex;
	
	@Override
	public void reserveConnectionList() {
		while(mutex) {		}
		mutex = true;
	}
	
	@Override
	public void releaseConnectionList() {
		mutex = false;
	}
	
	@Override
	public ArrayList<Connection> getConnectionList() {
		return new ArrayList<Connection>(clients.values());
	}

	@Override
	public Connection getConnection(String title) {
		reserveConnectionList();
		Connection c = clients.get(title);
		releaseConnectionList();
		return c;
	}
	
//---  Support Methods   ----------------------------------------------------------------------
	
	public void print(String in) {
		if(!quiet) {
			System.out.println(in);
		}
	}



}
