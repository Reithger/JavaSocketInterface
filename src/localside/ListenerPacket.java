package localside;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.io.OutputStreamWriter;
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
	private KeepAliveThread keepAlive;
	private SenderThread messageSender;
	
	private int listenPort;
	private int sendPort;
	
//---  Constructors   -------------------------------------------------------------------------
	
	public ListenerPacket(int inListen, int inSend) {
		lastReceived = new Long(0);
		listenPort = inListen;
		sendPort = inSend;
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
			System.out.println("Client connection established with: " + client);
		}
		catch(Exception e) {
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
		if(keepAlive != null) {
			keepAlive.end();
			keepAlive.interrupt();
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
		if(messageSender == null) {
			messageSender = new SenderThread(sendPort);
			messageSender.start();
		}
		messageSender.queueMessage(message);
	}
	
	public void assignThreads(KeepAliveThread listen, KeepAliveThread time, KeepAliveThread signOfLife) {
		listener = listen;
		timeOut = time;
		keepAlive = signOfLife;
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
