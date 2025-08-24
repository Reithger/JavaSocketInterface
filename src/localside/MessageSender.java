package localside;

import java.util.ArrayList;

public interface MessageSender {
	
	public final static String TAG_SENDER = "sender";
	
	public final static String TAG_CLIENT = "client";

	public abstract void sendMessage(String message, String tag) throws Exception;

	public abstract void sendMessage(String message, ArrayList<String> tag) throws Exception;
	
}
