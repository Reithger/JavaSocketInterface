package subprogram;

public class SubProgramGenerator {

	public static SubProgram getPythonSubProgram(String path, String... arguments) {
		return new PythonProgramRun(path, arguments);
	}
	
	public static SubProgram getJavaSubProgram(String path, String... arguments) {
		return new JavaProgramRun(path, arguments);
	}
	
	public static void runPythonSubProgram(String path, String listenPort, boolean quiet, String ... arguments) {
		PythonProgramRun run = new PythonProgramRun(path, arguments);
		run.initiateSubprogram(listenPort,  quiet);
	}

	public static void runJavaSubProgram(String path, String listenPort, boolean quiet, String ... arguments) {
		JavaProgramRun run = new JavaProgramRun(path, arguments);
		run.initiateSubprogram(listenPort,  quiet);
	}
	
}
