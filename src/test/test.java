package test;

import java.util.ArrayList;

import core.JavaReceiver;
import core.JavaSender;
import core.SocketControl;
import localside.MessageSender;

public class test {

	public static void main(String[] args) {
		SocketControl socket = new SocketControl();
		
		// For listening to and establishing a Captions sub-program that controls this program
		
		setupCaptionsInterpreter(socket);
		
		setupSender(socket, socket.getInstanceListenPort("text"));
		
		
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
		
		socket.setInstanceListenPortRandom("captions");
		
		socket.verifySubprogramReady("./distdac/", "StylizedCaptions.jar", "../test/assets/StylizedCaptions.jar", "/test/assets/StylizedCaptions.jar");		
		socket.setInstanceSubprogramJava("captions", "./distdac/StylizedCaptions.jar");
		
		reader r = new reader();
		socket.attachJavaReceiver("captions", r);
		
		socket.setInstanceKeepAlive("captions", 2000);
		socket.setInstanceTimeout("captions", 10000);
		
		try {
			socket.runSocketInstance("captions");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void setupSender(SocketControl socket, int port) {
		System.out.println("Check: " + port);
		socket.createSocketInstance("sending");
		
		socket.attachJavaReceiver("sending", new reader(false));
		
		sender s = new sender();
		
		socket.attachJavaSender("sending", s);
		
		socket.setInstanceKeepAlive("sending", 2000);
		
		socket.setInstanceQuiet("sending", true);
		
		try {
			socket.runSocketInstance("sending");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			socket.addInstanceSendPort("sending", "sender", port);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		socket.addInstanceSendPortTag("sending", "sender", "TEST");

		while(true) {
			s.sendMessage("Hello!");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void setupCaptionsInterpreter(SocketControl socket) {
		socket.createSocketInstance("text");
		
		socket.verifySubprogramReady("./captions", "voice-to-text.py", "../test/assets/voice-to-text.py", "/test/assets/voice-to-text.py");
		
		socket.setInstanceListenPortRandom("text");

		socket.attachJavaReceiver("text", new reader(true));
		
		socket.setInstanceSubprogramPython("text", "./captions/voice-to-text.py");
		
		socket.setInstanceKeepAlive("text", 2000);
		
		socket.setInstanceQuiet("text", true);

		try {
			socket.runSocketInstance("text");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
}

class sender implements JavaSender {

	private MessageSender ms;
	
	@Override
	public void receiveMessageSender(MessageSender sender) {
		ms = sender;
	}
	
	public void sendMessage(String in) {
		try {
			ms.sendMessage("sender", "Hello!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	public void receiveSocketData(String socketData, ArrayList<String> tags) {
		//System.out.println("Raw: " + socketData);
		if(clean) {
			if(tags.contains("VTT")) {
				if(socketData.contains("partial") || socketData.contains("text")) {
					socketData = cleanInput(socketData);
					//System.out.println("Tags: " + tags.toString());
					System.out.println("Host received message: " + socketData);
				}
			}
			else {
				System.out.println("~~~Sender Sent In: " + socketData + " " + tags);
			}
		}
		else {
			System.out.println("~~~Sender received message: " + socketData);
		}
	}
	
	private String cleanInput(String in) {
		String use = in.substring(in.indexOf("\"") + 1);
		use = use.substring(use.indexOf("\"") + 1);
		use = use.substring(use.indexOf("\"") + 1, use.length() - 1);
		return use;
	}

}