package localside.listen;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;

public class SenderThread extends KeepAliveThread{

	private static final int CONNECTION_FAILED_TIMEOUT = 15;
	
	private int destinationPort;
	
	private Socket sender;
	private volatile BufferedWriter writer;
	
	private volatile LinkedList<String> messagesToSend;
	
	public SenderThread(int senderPort) {
		destinationPort = senderPort;
		messagesToSend = new LinkedList<String>();
	}
	
	@Override
	public void run() {
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
					writer.write(send);
					writer.newLine();
					writer.flush();
					messagesToSend.poll();
					System.out.println("To: " + destinationPort + ", message sent: " + send);
					Thread.sleep(10);
				}
				catch(Exception e) {
					e.printStackTrace();
					bailOnSending = true;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
		while(sender == null && getKeepAliveStatus() && count < CONNECTION_FAILED_TIMEOUT) {
			try {
				sender = new Socket("127.0.0.1", destinationPort);
				System.out.println("Connection established to destination port: " + destinationPort);
			}
			catch(Exception e) {
				//e.printStackTrace();
				System.err.println("Error: Sender thread not connecting to send messages on port: " + destinationPort);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
				count++;
			}
		}
		if(count >= CONNECTION_FAILED_TIMEOUT) {
			throw new Exception("Unable to connect Sender thread on port: " + destinationPort + ", terminating Message Sending capabilities.");
		}
	}
	
	public void queueMessage(String message) {
		messagesToSend.add(message);
	}
	
}
