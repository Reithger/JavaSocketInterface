package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ListeningThread extends KeepAliveThread{

	private volatile ListenerPacket packet;
	private JavaReceiver reference;
	private int currentPort;
	
	public ListeningThread(ListenerPacket context, JavaReceiver refSend, int currPort) {
		super();
		packet = context;
		reference = refSend;
		currentPort = currPort;
	}
	
	@Override
	public void run() {
		System.out.println("Starting Local Listener Service");
		try {
			packet.restartServer(currentPort);
			Socket client = packet.getClient();
			System.out.println(client);
			BufferedReader receiver = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String received = receiver.readLine();
			while(received != null && !received.equals("exit") && getKeepAliveStatus()) {
				if(!received.equals(""))
					reference.receivePythonData(received);
				received = receiver.readLine();
				packet.updateLastReceived();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("Connection Died, Restarting Listener Processes");
		}
	}
	
	@Override
	public void end() {
		super.end();
		packet = null;
		reference = null;
	}
	
}
