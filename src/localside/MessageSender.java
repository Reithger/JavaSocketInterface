package localside;

import java.util.ArrayList;

/**
 * 
 * Interface for the object received from the JavaSocketInterface that handles the sending of messages.
 * 
 * We have two kinds of Connections: the Listening Socket which is attached to many Clients, and manually
 * established Sender Socket Connections that the user establishes a target port number and tags for.
 * 
 * Thus we have two kinds of message sending: to all attached clients (there is only one server per instance)
 * that match a particular tag or set of tags, and using a specific Sender already connected to its sole target.
 * 
 * This interface is implemented by JavaSocket.class and is passed to your implementing class when it
 * implements the JavaSender interface (and is then attached to that Socket instance).
 * 
 */

public interface MessageSender {
	
	public final static String TAG_SENDER = "sender";
	
	public final static String TAG_CLIENT = "client";

	/**
	 * Sending function that distributes a message to all Clients attached to our sole Listening Socket.
	 * 
	 * You can identify a particular tag that filters the recipients of this message according to those
	 * that possess the tag identified.
	 * 
	 * @param message
	 * @param tag
	 * @throws Exception
	 */
	
	public abstract void distributeMessage(String message, String tag) throws Exception;

	/**
	 * Variation on the other distributeMessage function that permits a set of tags to be identified,
	 * for which all Clients that possess at least one of the tags in this list will be sent the message.
	 * 
	 * @param message
	 * @param tag
	 * @throws Exception
	 */
	
	public abstract void distributeMessage(String message, ArrayList<String> tag) throws Exception;
	
	/**
	 * Variation on the other distributeMessage functions that permits identifying a set of inclusive and
	 * exclusive tags; the message will be sent to all Clients that possess all of the inclusive
	 * tags but has none of the exclusive tags.
	 * 
	 * @param message
	 * @param tagInclusive
	 * @param tagExclusive
	 * @throws Exception
	 */
	
	public abstract void distributeMessage(String message, ArrayList<String> tagInclusive, ArrayList<String> tagExclusive) throws Exception;
	
	/**
	 * Sending function that uses the established Sender Socket title labeled when creating it
	 * (JavaSocket.addSendPort()) to send a message. This Socket is already established with its
	 * target and needs no further context besides its name to identify it and the message.
	 * 
	 * @param title
	 * @param message
	 * @throws Exception
	 */
	
	public abstract void sendMessage(String title, String message) throws Exception;
	
}
