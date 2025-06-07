package core;

public interface JavaTeardown {

	/**
	 * 
	 * Provides a function to this library that can be called when the connection is
	 * terminated and the Java subprogram needs to self-terminate and end all of its
	 * active processes.
	 * 
	 */
	
	public abstract void teardownProgram();
	
}
