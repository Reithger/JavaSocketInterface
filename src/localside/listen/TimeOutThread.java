package localside.listen;

import java.util.ArrayList;

import localside.Connection;
import localside.ListenerPacket;

/**
 * KeepAliveThread subclass for monitoring the Connection objects and checking
 * whether their respective connections have died and need to be terminated.
 * 
 * Irrelevant if timeout value is set to -1, as we do not close connections in that case.
 * 
 */

public class TimeOutThread extends KeepAliveThread{

	private volatile ListenerPacket packet;
	private int checkRate;
	private int timeoutPeriod;
	private int delay;
	
	/**
	 * 
	 * checkTimer is how often we check on the Connection objects
	 * 
	 * timeout is the time limit; if contact hasn't been made within that span of time (in ms), we close
	 * the connection
	 * 
	 * startTimingDelay is how much time we wait for an initial connection message to be sent before we start
	 * a timeout count
	 * 
	 * TODO: How much of this is necessary/relevant given we're using the Socket.accept() client?
	 * 
	 * @param context
	 * @param checkTimer
	 * @param timeout
	 * @param startTimingDelay
	 */
	
	public TimeOutThread(ListenerPacket context, int checkTimer, int timeout, int startTimingDelay) {
		super();
		packet = context;
		checkRate = checkTimer;
		timeoutPeriod = timeout;
		delay = startTimingDelay;
	}
	
	@Override
	public void run() {
		while(getKeepAliveStatus()) {
			ArrayList<String> removeList = new ArrayList<String>();
			try {
				Thread.sleep(checkRate);
				print("\n--- --- KeepAlive Checks on all Connections, Timeout : " + timeoutPeriod + " ms --- ---");
				packet.reserveConnectionList();
				for(Connection c : packet.getConnectionList()) {
					print("\n--- KeepAlive Check on Connection: " + c);
					if(!c.checkTimedOut(timeoutPeriod)) {
						print("!!!Ending Connection!!! Connection Thread (" + (c.getIdentity()) + ") timed out on communication");
						removeList.add(c.getTitle());
						continue;
					}
					else {
						if(!c.checkInitiated()) {
							if(c.hasTag(Connection.TAG_SENDER)) {
								print("\nInitial Communication Not Received: Sender Socket (" + (c) + ") has not received initial communication with process yet");
							}
							else {
								print("\nInitial Communication Not Received: Listener Thread (" + (packet.getServer()) + ") has not received initial communication with client " + c + " yet");
							}
						}
						else {
							if(c.hasTag(Connection.TAG_SENDER)) {
								print("\nStill Communicating: Sender Socket (" + (c) + ") still in communication with process, last check in: " + c.getLastReceived() + " (" + (System.currentTimeMillis() - c.getLastReceived()) + ") milliseconds");
							}
							else {
								print("\nStill Communicating: Listener Thread (" + (packet.getServer()) + ") still in communication with client " + c + ", last check in: " + c.getLastReceived() + " (" + (System.currentTimeMillis() - c.getLastReceived()) + ") milliseconds");
								
							}
						}
						if(c.initiationTimeout(delay)) {
							if(c.hasTag(Connection.TAG_SENDER)) {
								print("\nNo Message Received by Sender Socket (" + (c) + ") in " + delay + " MilliSeconds, Beginning Timeout Counter with Phantom Message Timestamp");
							}
							else {
								print("\nNo Message Received by  (" + (packet.getServer()) + ") from " + c + " in " + delay + " MilliSeconds, Beginning Timeout Counter with Phantom Message Timestamp");
							}
							c.forceSetLastReceived();
						}
					}
				}
				packet.releaseConnectionList();
				for(String s : removeList) {
					packet.terminateConnection(s);
				}
				
			} catch (Exception e) {
				packet.releaseConnectionList();
				for(String s : removeList) {
					packet.terminateConnection(s);
				}
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void end() {
		super.end();
		packet = null;
	}
	
}
