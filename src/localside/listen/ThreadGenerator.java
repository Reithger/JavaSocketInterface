package localside.listen;

import core.JavaReceiver;
import localside.InitiateListening;
import localside.ListenerPacket;
import localside.MessageSender;

public class ThreadGenerator {

	public static KeepAliveThread generateListeningThread(ListenerPacket packet, JavaReceiver receiver) {
		return new ListeningThread(packet, receiver);
	}
	
	public static KeepAliveThread generateTimeOutThread(ListenerPacket packet, InitiateListening restart, int checkRate, int timeout, int timingDelay, String context) {
		return new TimeOutThread(packet, restart, checkRate, timeout, timingDelay, context);
	}

}
