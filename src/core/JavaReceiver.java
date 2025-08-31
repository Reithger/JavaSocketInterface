package core;

import java.util.ArrayList;

public interface JavaReceiver {

	/**
	 * 
	 * In your own program that uses this library, you want a class to implement this and be passed to
	 * an instance of JavaSocketInterface so that it can periodically receive String data from the associated
	 * sockets that are sending data over.
	 * 
	 * It provides the sent data and the tags associated to that Connection for managing incoming data sources.
	 * 
	 * @param socketData - String object representing the data sent by a Connection socket
	 * @param tags - ArrayList of String objects representing label tags associated to the Connection that sent this data
	 */
	
	public abstract void receiveSocketData(String socketData, ArrayList<String> tags);
	
}
