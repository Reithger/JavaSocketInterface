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
			print("---Sender Thread Activated");
			boolean bailOnSending = false;
			while(messagesLeft() && !bailOnSending) {
				try {
					for(Connection c : connections.getConnectionList()) {
						while(c.hasMessage()) {
							c.sendViableMessage();
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					bailOnSending = true;
				}
			}
			try {
				Thread.sleep(keepAlive == -1 ? 10000 : keepAlive);
				if(keepAlive != -1) {
					for(Connection c : connections.getConnectionList()) {
						c.queueMessage("Keepalive message from " + c.getIdentity());
					}
				}
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}
	
	private boolean messagesLeft() {
		for(Connection c : connections.getConnectionList()) {
			if(c.hasMessage()) {
				return true;
			}
		}
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
