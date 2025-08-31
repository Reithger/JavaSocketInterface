package localside;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public interface ConnectionsManager {

	public abstract HashMap<String, Connection> getConnections();
	
	public abstract void reserveConnectionList();
	
	public abstract void releaseConnectionList();
	
	public abstract ArrayList<Connection> getConnectionList();
	
	public abstract Connection getConnection(String title);
	
	public abstract void addConnection(String title, Socket client);
	
	public abstract void addConnection(String title, int clientPort) throws Exception;
	
	public abstract void addTag(String title, String tag);
	
	public abstract void startConnection(String title);
	
	public abstract void terminateConnection(String title);
	
}
