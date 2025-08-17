package localside.listen;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;

public class SenderThread extends KeepAliveThread{

	
	private int destinationPort;
	private int keepAlive;
	private int connectionAttempts;
	
	private Socket sender;
	private volatile BufferedWriter writer;
	
	private volatile LinkedList<String> messagesToSend;
	
	private volatile boolean active;

	public SenderThread(int senderPort, int keepAliveTimer, int numConnectionAttempts) {
		destinationPort = senderPort;
		messagesToSend = new LinkedList<String>();
		keepAlive = keepAliveTimer;
		connectionAttempts = numConnectionAttempts;
		active = false;
	}
	
	@Override
	public void run() {
		active = true;
		try {
			connectToDestination();
			
			establishWriter();

			processMessageQueue();
		} catch (Exception e) {
			e.printStackTrace();
			end();
		}
		
	}
	
	private void processMessageQueue() {
		while(getKeepAliveStatus()) {
			boolean bailOnSending = false;
			while(!messagesToSend.isEmpty() && !bailOnSending) {
				String send = messagesToSend.peek();
				try {
					sendMessage(send);
					messagesToSend.poll();
				}
				catch(Exception e) {
					e.printStackTrace();
					bailOnSending = true;
				}
			}
			try {
				Thread.sleep(keepAlive == -1 ? 10000 : keepAlive);
				if(keepAlive != -1) {
					messagesToSend.add("Keepalive message");
				}
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}
	
	private void sendMessage(String send) throws Exception{
		writer.write(send);
		writer.newLine();
		writer.flush();
		print("To: " + destinationPort + ", message sent: " + send);
	}
	
	private void establishWriter() {
		if(writer == null && sender != null) {
			try {
				writer = new BufferedWriter(new OutputStreamWriter(sender.getOutputStream(), "UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void connectToDestination() throws Exception{
		int count = 0;
		while(sender == null && getKeepAliveStatus() && (count < connectionAttempts || connectionAttempts == -1)) {
			try {
				sender = new Socket("127.0.0.1", destinationPort);
				print("Connection established to destination port: " + destinationPort);
			}
			catch(Exception e) {
				//e.printStackTrace();
				error("Error: Sender thread not connecting to send messages on port: " + destinationPort);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
				count++;
			}
		}
		if(count >= connectionAttempts && connectionAttempts != -1) {
			throw new Exception("Unable to connect Sender thread on port: " + destinationPort + ", terminating Message Sending capabilities.");
		}
	}
	
	public void queueMessage(String message) {
		messagesToSend.add(message);
		if(sender != null)
			this.interrupt();
	}
	
	public boolean getActiveStatus() {
		return active;
	}
	
}
