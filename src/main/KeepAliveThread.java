package main;

public abstract class KeepAliveThread extends Thread {

	private volatile boolean keepAlive;
	
	public KeepAliveThread() {
		keepAlive = true;
	}
	
	public void end() {
		keepAlive = false;
	}
	
	protected boolean getKeepAliveStatus() {
		return keepAlive;
	}
	
}
