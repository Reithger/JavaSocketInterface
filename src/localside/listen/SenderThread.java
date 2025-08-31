package localside.listen;

import localside.Connection;
import localside.ConnectionsManager;

/**
 * KeepAliveThread subclass that processes queued messages to send out to clients/end-points.
 * 
 * Relies on ConnectionManager interface for accessing Connection objects.
 * 
 * Sends out a KeepAlive message on all Connections periodically if set to do so.
 * 
 * TODO: Make each Connection have a unique keepalive timer?
 * 
 */

public class SenderThread extends KeepAliveThread{

//---  Instance Variables   -------------------------------------------------------------------
	
	private int keepAlive;
	
	private ConnectionsManager connections;
	
	private boolean active;
	
//---  Constructors   -------------------------------------------------------------------------

	public SenderThread(int keepAliveTimer, ConnectionsManager inCon) {
		connections = inCon;
		keepAlive = keepAliveTimer;
	}
	
//---  Operations   ---------------------------------------------------------------------------
	
	@Override
	public void run() {
		try {
			active = true;
			processMessageQueue();
		} catch (Exception e) {
			e.printStackTrace();
			end();
		}
	}
	
	private void processMessageQueue() {
		while(getKeepAliveStatus()) {
			print("\n---Sender Thread Activated");
			while(messagesLeft()) {
				connections.reserveConnectionList();
				for(int i = 0; i < connections.getConnectionList().size(); i++) {
					Connection c = connections.getConnectionList().get(i);
					boolean bailOnSending = false;
					while(c.hasMessage() && !bailOnSending) {
						try {
							c.sendViableMessage();
						}
						catch(Exception e) {
							bailOnSending = true;
							c.establishWriter();
						}
					}
				}
				connections.releaseConnectionList();
			}
			try {
				if(keepAlive != -1) {
					connections.reserveConnectionList();
					for(int i = 0; i < connections.getConnectionList().size(); i++) {
						Connection c = connections.getConnectionList().get(i);
						c.queueMessage("Keepalive message to " + (c.hasTag(Connection.TAG_SENDER) ? c.getIdentity() : c.getTitle()));
						print("Queued Keepalive for " + c);
					}
					connections.releaseConnectionList();
				}
				Thread.sleep(keepAlive == -1 ? 10000 : keepAlive);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}
	
	private boolean messagesLeft() {
		connections.reserveConnectionList();
		for(int i = 0; i < connections.getConnectionList().size(); i++) {
			Connection c = connections.getConnectionList().get(i);
			if(c.hasMessage()) {
				connections.releaseConnectionList();
				return true;
			}
		}
		connections.releaseConnectionList();
		return false;
	}

	public void queueMessage(String title, String message) throws Exception {
		Connection sender = connections.getConnection(title);
		if(sender != null) {
			sender.queueMessage(message);
			this.interrupt();
		}
		else {
			throw new Exception("Attempt to send message via non-existent Connection: " + title);
		}
	}
	
//--- Getter Methods   ------------------------------------------------------------------------

	public boolean getActiveStatus() {
		return active;
	}
	
}
