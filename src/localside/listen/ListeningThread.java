package localside.listen;

import java.net.Socket;

import core.JavaReceiver;
import localside.ConnectionsManager;
import localside.ListenerPacket;

/**
 * Subclass of KeepAliveThread that maintains a listening status on the ServerSocket
 * object for new connections being made; if one occurs, a new Connection object is
 * instantiated through the ConnectionsManager and the JavaReceiver object for this
 * project is assigned to it.
 * 
 */

public class ListeningThread extends KeepAliveThread{

	private volatile ListenerPacket packet;
	private JavaReceiver reference;
	private ConnectionsManager connections;
	
	public ListeningThread(ListenerPacket context, JavaReceiver refSend, ConnectionsManager conMan) {
		super();
		connections = conMan;
		packet = context;
		reference = refSend;
	}
	
	@Override
	public void run() {
		print("Starting Local Listener Service at " + packet.getServer());
		try {
			packet.startServer();
			while(!packet.getServer().isClosed() && this.getKeepAliveStatus()) {
				Socket client = packet.getServer().accept();
				connections.addConnection("" + client.getPort(), client);
				connections.getConnection("" + client.getPort()).setReceiver(reference);
				connections.startConnection("" + client.getPort());
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			print("Connection Died or Ended, Restarting Listener Processes");
		}
	}
	
	@Override
	public void end() {
		super.end();
		packet = null;
		reference = null;
	}
	
}
