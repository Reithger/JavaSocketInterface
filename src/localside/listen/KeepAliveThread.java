package localside.listen;

public abstract class KeepAliveThread extends Thread {

	private volatile boolean keepAlive;
	
	private static boolean quiet;
	
	public KeepAliveThread() {
		keepAlive = true;
	}
	
	public void end() {
		keepAlive = false;
	}
	
	public static void setQuiet(boolean shh) {
		quiet = shh;
	}
	
	public boolean getQuiet() {
		return quiet;
	}
	
	protected boolean getKeepAliveStatus() {
		return keepAlive;
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
