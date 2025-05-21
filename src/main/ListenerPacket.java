package main;

import java.net.ServerSocket;
import java.net.Socket;

public class ListenerPacket {
	
//---  Instance Variables   -------------------------------------------------------------------

	private ServerSocket server;
	private Socket client;
	private Long lastReceived;
	private KeepAliveThread listener;
	private KeepAliveThread timeOut;
	
//---  Constructors   -------------------------------------------------------------------------
	
	public ListenerPacket() {
		lastReceived = 0L;
	}
	
	public ListenerPacket(int port) {
		try {
			server = new ServerSocket(port);
			client = server.accept();
			lastReceived = System.currentTimeMillis();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
//---  Operations   ---------------------------------------------------------------------------
	
	public void updateLastReceived() {
		lastReceived = System.currentTimeMillis();
	}
	
	public void restartServer(int port) {
		try {
			if(server != null) {
				server.close();
				client.close();
			}
			server = new ServerSocket(port);
			client = server.accept();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void closeOutSession() {
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
		if(listener != null) {
			listener.end();
			listener.interrupt();
		}
		if(timeOut != null) {
			timeOut.end();
			timeOut.interrupt();
		}
		
	}

	public void assignThreads(KeepAliveThread listen, KeepAliveThread time) {
		listener = listen;
		timeOut = time;
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

}
