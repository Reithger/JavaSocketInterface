package core;

import java.util.HashMap;

import localside.JavaSocket;
import subprogram.SubProgramGenerator;
import subprogram.SubprogramFileValidation;

/**
 * 
 * Idea is that, from here, you can decide if your Socket is just listening, if it is also sending, if it should run a subprogram to communicate with,
 * what language that subprogrma wou
 * 
 * Your Socket Is:
 *  - Listening on what/which Port(s)?
 *  - Sending what data on that/those Port(s)?
 *  - Generating a subprogram to talk to?
 *   - Is this subprogram in Python, Java, other languages?
 *  - What is the Timeout period on no communication?
 *   - Is there even a keep-alive status?
 *   
 * So, range of functions to add sub-program generators (implementations of SubProgram)
 * 
 * Constructor that initiates this context
 * 
 * Functions to add a listening context on a particular port and instructions on whether to do keep-alive/subprogram restarting
 * 
 * Functions to send data out on a particular port and whether to do keep-alive messages in the meantime
 * 
 * Idea is that this single context can govern multiple Socket connections to multiple sub-programs or other software
 * 
 * Also allow running subprograms without an attached listener for versatility.
 * 
 */

public class SocketControl {
	
//---  Instance Variables   -------------------------------------------------------------------
	
	private HashMap<String, JavaSocket> socketInstances;
	
//---  Constructors   -------------------------------------------------------------------------
	
	public SocketControl() {
		socketInstances = new HashMap<String, JavaSocket>();
	}
	
//---  Operations   ---------------------------------------------------------------------------
	
	public void createSocketInstance(String label) {
		socketInstances.put(label, new JavaSocket());
	}
	
	public void createSocketInstance(String label, int port, int timeout, int keepalive) {
		socketInstances.put(label, new JavaSocket());
	}
	
	public void runSocketInstance(String label) {
		socketInstances.get(label).setUpListening();
	}
	
	public void endSocketInstance(String label) {
		socketInstances.get(label).closeListening();
	}
	
	public void removeSocketInstance(String label) {
		endSocketInstance(label);
		socketInstances.remove(label);
	}
	
	public void attachJavaReceiver(String label, JavaReceiver reference) {
		socketInstances.get(label).setReceiver(reference);
	}
	
	public void attachJavaSender(String label, JavaSender reference) {
		reference.receiveMessageSender(socketInstances.get(label).getMessageSender());
	}
	
	public void runPythonSubProgramUnattached(String programPath, String port, String ... arguments) {
		SubProgramGenerator.runPythonSubProgram(programPath, port, arguments);
	}
	
	public void runJavaSubProgramUnattached(String programPath, String port, String ... arguments) {
		SubProgramGenerator.runJavaSubProgram(programPath, port, arguments);
	}
	
	public boolean verifySubprogramReady(String localFileContext, String fileName, String localReferencePath, String localJarPath) {
		return SubprogramFileValidation.verifySubprogramFileNear(localFileContext, fileName, localReferencePath, localJarPath);
	}
	
//---  Setter Methods   -----------------------------------------------------------------------
	
	public void setInstancePort(String label, int port) {
		socketInstances.get(label).setPort(port);
	}
	
	public void setInstanceTimeout(String label, int timeout) {
		socketInstances.get(label).setTimeout(timeout);
	}
	
	public void setInstanceKeepAlive(String label, int keepalive) {
		socketInstances.get(label).setKeepAlive(keepalive);
	}
	
	public void setInstanceSubprogramJava(String label, String programPath, String ... arguments) {
		socketInstances.get(label).setSubprogram(SubProgramGenerator.getJavaSubProgram(programPath, arguments));
	}
	
	public void setInstanceSubprogramPython(String label, String programPath, String ... arguments) {
		socketInstances.get(label).setSubprogram(SubProgramGenerator.getPythonSubProgram(programPath, arguments));
	}
	
	
}
