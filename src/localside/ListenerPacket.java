package localside;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import localside.listen.KeepAliveThread;
import localside.listen.MessageSender;

public class ListenerPacket implements MessageSender{
	
//---  Instance Variables   -------------------------------------------------------------------

	private ServerSocket server;
	private Socket client;
	private Long lastReceived;
	private KeepAliveThread listener;
	private KeepAliveThread timeOut;
	private KeepAliveThread keepAlive;
	
	private Writer writer;
	
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
		if(keepAlive != null) {
			keepAlive.end();
			keepAlive.interrupt();
		}
		if(writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void sendMessage(String message) {
		if(writer == null) {
			try {
				writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			writer.write(message, 0, message.length());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	public MessageSender getMessageSender() {
		return this;
	}

}
