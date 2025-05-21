package main;

public class TimeOutThread extends KeepAliveThread{

	private volatile ListenerPacket packet;
	private InitiateListening reference;
	private int checkRate;
	private int timeoutPeriod;
	
	public TimeOutThread(ListenerPacket context, InitiateListening refIn, int checkTimer, int timeout) {
		super();
		packet = context;
		reference = refIn;
		checkRate = checkTimer;
		timeoutPeriod = timeout;
	}
	
	@Override
	public void run() {
		while(getKeepAliveStatus()) {
			try {
				Thread.sleep(checkRate);
				if(System.currentTimeMillis() - packet.getLastReceived() > timeoutPeriod && !packet.getLastReceived().equals(0L)) {
					System.out.println("Listener Thread timed out on communication with Python process, restarting");
					reference.setUpListening();
					break;
				}
				else {
					System.out.println("Listener Thread still in communication with Python process, last check in: " + packet.getLastReceived());
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
		reference = null;
	}
	
}
