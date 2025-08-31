package localside.listen;

import core.JavaReceiver;
import localside.ConnectionsManager;
import localside.ListenerPacket;

public class ThreadGenerator {

	public static KeepAliveThread generateListeningThread(ListenerPacket packet, JavaReceiver receiver, ConnectionsManager conMan) {
		return new ListeningThread(packet, receiver, conMan);
	}
	
	public static KeepAliveThread generateTimeOutThread(ListenerPacket packet, int checkRate, int timeout, int timingDelay) {
		return new TimeOutThread(packet, checkRate, timeout, timingDelay);
	}
	
	public static KeepAliveThread generateSenderThread(ConnectionsManager conMan, int keepAliveTime) {
		return new SenderThread(keepAliveTime, conMan);
	}

}
