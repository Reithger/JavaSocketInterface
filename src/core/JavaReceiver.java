package core;

public interface JavaReceiver {

	/**
	 * 
	 * In your own program that uses this library, you want a class to implement this and be passed to
	 * an instance of JavaPythonSocket so that it can periodically receive String data from the associated
	 * Python program that is running and sending data over.
	 * 
	 * This will be customized to provide more methods of interaction than one-way receiving of data.
	 * 
	 * @param pythonData
	 */
	
	public abstract void receiveSocketData(String socketData);
	
}
