package localside.listen;

public class SignOfLifeThread extends KeepAliveThread {

	private int messageSendTimer;
	private MessageSender messageSender;
	
	public SignOfLifeThread(MessageSender packet, int keepAliveTimer) {
		super();
		messageSendTimer = keepAliveTimer;
		messageSender = packet;
	}
	
	@Override
	public void run() {
		while(getKeepAliveStatus()) {
			try {
				Thread.sleep(messageSendTimer);
				messageSender.sendMessage("Keepalive message");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
