package localside.listen;

import localside.InitiateListening;
import localside.ListenerPacket;

public class TimeOutThread extends KeepAliveThread{

	private volatile ListenerPacket packet;
	private InitiateListening reference;
	private int checkRate;
	private int timeoutPeriod;
	private int delay;
	
	private long timeInitiated;
	
	private String subProgramID;
	
	public TimeOutThread(ListenerPacket context, InitiateListening refIn, int checkTimer, int timeout, int startTimingDelay, String programContext) {
		super();
		subProgramID = programContext == null ? "Subprogram" : programContext; 
		packet = context;
		reference = refIn;
		checkRate = checkTimer;
		timeoutPeriod = timeout;
		delay = startTimingDelay;
		timeInitiated = System.currentTimeMillis();
	}
	
	@Override
	public void run() {
		while(getKeepAliveStatus()) {
			try {
				Thread.sleep(checkRate);
				if(System.currentTimeMillis() - packet.getLastReceived() > timeoutPeriod && !packet.getLastReceived().equals(new Long(0))) {
					print("!!!Restarting Connection!!! Listener Thread (" + (packet.getServer()) + ") timed out on communication with " + subProgramID + " process, restarting");
					reference.setUpListening();
					break;
				}
				else {
					if(packet.getLastReceived().equals(new Long(0))) {
						print("Initial Communication Not Received: Listener Thread (" + (packet.getServer()) + ") has not received initial communication with " + subProgramID + " process yet");
					}
					else {
						print("Still Communicating: Listener Thread (" + (packet.getServer()) + ") still in communication with " + subProgramID + " process, last check in: " + packet.getLastReceived() + " (" + lastCheckIn() + ") milliseconds");
					}
					if(myAge() > delay && packet.getLastReceived().equals(new Long(0))) {
						print("No Message Received by  (" + (packet.getServer()) + ") in " + delay + " MilliSeconds, Beginning Timeout Counter with Phantom Message Timestamp");
						packet.updateLastReceived();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private long lastCheckIn() {
		return System.currentTimeMillis() - packet.getLastReceived();
	}
	
	private long myAge() {
		return System.currentTimeMillis() - timeInitiated;
	}
	
	@Override
	public void end() {
		super.end();
		packet = null;
		reference = null;
	}
	
}
