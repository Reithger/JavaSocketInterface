package localside;

import java.net.ServerSocket;
import java.net.Socket;

import localside.listen.KeepAliveThread;
import localside.listen.SenderThread;

public class ListenerPacket {
	
//---  Instance Variables   -------------------------------------------------------------------

	private volatile ServerSocket server;
	private volatile Socket client;
	
	private Long lastReceived;
	private KeepAliveThread listener;
	private KeepAliveThread timeOut;
	private SenderThread messageSender;
	
	private int listenPort;
	private int sendPort;
	
	private int keepalive;
	
	private boolean quiet;
	
//---  Constructors   -------------------------------------------------------------------------
	
	public ListenerPacket(int inListen, int inSend, int keepAliveTimer, boolean inQuiet) {
		lastReceived = new Long(0);
		listenPort = inListen;
		sendPort = inSend;
		keepalive = keepAliveTimer;
		quiet = inQuiet;
	}
	
//---  Operations   ---------------------------------------------------------------------------
	
	public void updateLastReceived() {
		lastReceived = System.currentTimeMillis();
	}

	public void restartServer() {
		try {
			if(server != null) {
				server.close();
				client.close();
			}
			server = new ServerSocket(listenPort);
			client = server.accept();
			print("Client connection established with: " + client);
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
			if(client != null && !client.isClosed()) {
				client.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message) {
		if(!messageSender.getActiveStatus()) {
			messageSender.start();
		}
		messageSender.queueMessage(message);
	}
	
	public void assignThreads(KeepAliveThread listen, KeepAliveThread time, KeepAliveThread sendThread) {
		listener = listen;
		timeOut = time;
		messageSender = (SenderThread)sendThread;
		if(keepalive != -1) {
			sendMessage("Handshake Protocol Initated, Sender Thread Spin Up Begun");
		}
	}

//---  Getter Methods   -----------------------------------------------------------------------0
	
	public ServerSocket getServer() {
		return server;
	}
	
	public Socket getClient() {
		return client;
	}
	
	public Long getLastReceived() {
		return lastReceived;
	}
	
	public void print(String in) {
		if(!quiet) {
			System.out.println(in);
		}
	}
}
