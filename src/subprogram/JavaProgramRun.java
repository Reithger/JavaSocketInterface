package subprogram;

import java.io.IOException;

/**
 * 
 * SubProgram class for running Java programs; this assumes you have an executable .jar file
 * that will be run. This does not do compilation to get .class files and then run java on that,
 * it runs "java -jar [path to jar file]" on your file.
 * 
 */

public class JavaProgramRun implements SubProgram {

	private String runPath;
	private String[] arguments;
	
	public JavaProgramRun(String javaRunPath, String ... args) {
		runPath = javaRunPath;
		arguments = args;
	}
	
	@Override
	public String getContext() {
		return runPath;
	}
	
	@Override
	public void initiateSubprogram(String listenPort, String sendPort) {
		StringBuilder command = new StringBuilder();
		command.append("java -jar " + runPath + (listenPort == null ? "" : (" " + listenPort + " " + sendPort)));
		for(String s : arguments) {
			command.append(" " + s);
		}
		try {
			Runtime.getRuntime().exec(command.toString());
			System.out.println("Java Subprogram: " + runPath + " has been executed and is unattached");
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

}
