package localside.listen;

import core.JavaReceiver;
import localside.ListenerPacket;

public class ThreadGenerator {

	public static KeepAliveThread generateListeningThread(ListenerPacket packet, JavaReceiver receiver, int port) {
		return new ListeningThread(packet, receiver, port);
	}
	
	public static KeepAliveThread generateTimeOutThread(ListenerPacket packet, InitiateListening restart, int checkRate, int timeout, String context) {
		return new TimeOutThread(packet, restart, checkRate, timeout, context);
	}

	public static KeepAliveThread generateSignOfLifeThread(ListenerPacket packet, int keepalive) {
		return new SignOfLifeThread(packet, keepalive);
	}
	
}
