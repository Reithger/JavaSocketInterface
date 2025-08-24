package localside.listen;

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
			try {
				Thread.sleep(checkRate);
				for(Connection c : packet.getConnectionList()) {
					if(!c.checkTimedOut(timeoutPeriod)) {
						print("!!!Ending Connection!!! Connection Thread (" + (c.getIdentity()) + ") timed out on communication");
						packet.terminateConnection(c.getTitle());
						break;
					}
					else {
						if(c.checkInitiated()) {
							print("Initial Communication Not Received: Listener Thread (" + (packet.getServer()) + ") has not received initial communication with process yet");
						}
						else {
							print("Still Communicating: Listener Thread (" + (packet.getServer()) + ") still in communication with process, last check in: " + c.getLastReceived() + " (" + (System.currentTimeMillis() - c.getLastReceived()) + ") milliseconds");
						}
						if(c.initiationTimeout(delay)) {
							print("No Message Received by  (" + (packet.getServer()) + ") in " + delay + " MilliSeconds, Beginning Timeout Counter with Phantom Message Timestamp");
							c.forceSetLastReceived();
						}
					}
				}
				
			} catch (Exception e) {
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
