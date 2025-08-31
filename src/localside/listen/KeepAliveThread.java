package localside.listen;

public abstract class KeepAliveThread extends Thread {

	private volatile boolean keepAliveStatus;
	
	private boolean quiet;
	
	public KeepAliveThread() {
		keepAliveStatus = true;
	}
	
	public void end() {
		keepAliveStatus = false;
	}
	
	public void setQuiet(boolean shh) {
		quiet = shh;
	}
	
	public boolean getQuiet() {
		return quiet;
	}
	
	protected boolean getKeepAliveStatus() {
		return keepAliveStatus;
	}
	
	public void print(String in) {
		if(!quiet) {
			System.out.println(in);
		}
	}
	
	public void error(String in) {
		if(!quiet) {
			System.err.println(in);
		}
	}
	
}
