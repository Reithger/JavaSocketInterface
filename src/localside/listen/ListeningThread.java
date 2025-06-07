package localside.listen;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import core.JavaReceiver;
import localside.ListenerPacket;

public class ListeningThread extends KeepAliveThread{

	private volatile ListenerPacket packet;
	private JavaReceiver reference;
	
	public ListeningThread(ListenerPacket context, JavaReceiver refSend) {
		super();
		packet = context;
		reference = refSend;
	}
	
	@Override
	public void run() {
		System.out.println("Starting Local Listener Service");
		try {
			packet.restartServer();
			Socket client = packet.getClient();
			BufferedReader receiver = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String received = receiver.readLine();
			while(received != null && !received.equals("exit") && getKeepAliveStatus()) {
				packet.updateLastReceived();
				if(!received.equals(""))
					reference.receiveSocketData(received);
				received = receiver.readLine();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("Connection Died or Ended, Restarting Listener Processes");
		}
	}
	
	@Override
	public void end() {
		super.end();
		packet = null;
		reference = null;
	}
	
}
