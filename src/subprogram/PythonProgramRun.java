package subprogram;

import java.io.IOException;

public class PythonProgramRun implements SubProgram{
	
	private String programRunPath;
	private String[] arguments;
	
	public PythonProgramRun(String path, String ...args) {
		programRunPath = path;
		arguments = args;
	}
	
	@Override
	public String getContext() {
		return programRunPath;
	}
	
	public void initiateSubprogram(String listenPort, String sendPort, boolean quiet) {
		StringBuilder command = new StringBuilder();
		command.append("python " + programRunPath + (listenPort == null ? "" : (" " + listenPort + " " + sendPort)));
		for(String s : arguments) {
			command.append(" " + s);
		}
		try {
			Runtime.getRuntime().exec(command.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
