package test;

import core.JavaReceiver;
import core.SocketControl;

public class test {

	public static void main(String[] args) {
		SocketControl socket = new SocketControl();
		
		// For listening to and establishing a Captions sub-program that controls this program
		
		setupCaptionsInterpreter(socket);
		
		
		// For listening to a remote accessor that can cause things to happen
		/*
		socket.createSocketInstance("remote_access");
		
		socket.setInstanceListenPort("remote_access", 6500);
		socket.setInstanceSendPort("remote_access", 6505);
		
		socket.attachJavaReceiver("remote_access", r);
		
		socket.setInstanceKeepAlive("remote_access", 2000);
		socket.setInstanceTimeout("remote_access", -1);
		socket.setInstanceSenderConnectionAttempts("remote_access", -1);
		
		socket.runSocketInstance("remote_access");
		*/
	}
	
	private static void setupCaptionsInterpreterInterpreter(SocketControl socket) {
		socket.createSocketInstance("captions");
		
		socket.setInstanceListenPort("captions", 6000);
		
		socket.verifySubprogramReady("./distdac/", "StylizedCaptions.jar", "../assets/StylizedCaptions.jar", "/control/assets/StylizedCaptions.jar");		
		socket.setInstanceSubprogramJava("captions", "./distdac/StylizedCaptions.jar");
		
		reader r = new reader();
		socket.attachJavaReceiver("captions", r);
		
		socket.setInstanceKeepAlive("captions", 2000);
		socket.setInstanceTimeout("captions", 10000);
		
		socket.runSocketInstance("captions");
	}
	
	private static void setupCaptionsInterpreter(SocketControl socket) {
		socket.createSocketInstance("text");
		
		socket.verifySubprogramReady("./captions", "voice-to-text.py", "../assets/voice-to-text.py", "/control/assets/voice-to-text.py");
		
		socket.setInstanceListenPort("text", 3500);

		socket.attachJavaReceiver("text", new reader(true));
		
		socket.setInstanceSubprogramPython("text", "./captions/voice-to-text.py");

		socket.runSocketInstance("text");	
	}
	
}

class reader implements JavaReceiver{

	private boolean clean;
	
	public reader() {
		clean = false;
	}
	
	public reader(boolean inClean) {
		clean = inClean;
	}
	
	@Override
	public void receiveSocketData(String socketData) {
		if(clean) {
			if(socketData.contains("partial") || socketData.contains("text")) {
				socketData = cleanInput(socketData);
				System.out.println("Host received message: " + socketData);
			}
		}
		else {
			System.out.println("Host received message: " + socketData);
		}
	}
	
	private String cleanInput(String in) {
		String use = in.substring(in.indexOf("\"") + 1);
		use = use.substring(use.indexOf("\"") + 1);
		use = use.substring(use.indexOf("\"") + 1, use.length() - 1);
		return use;
	}

}