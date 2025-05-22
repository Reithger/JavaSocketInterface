package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 * This class automates the deployment and verifying integrity of Python files near your Java project
 * that you are calling upon.
 * 
 * 
 * When you want to access a file that is nearby in your dev environment but will be packaged into
 * a .jar as a library or .exe, you need to have two file path styles as the references are different.
 * 
 * As the function tries both, which is which is arbitrary, but if a file were located at:
 * 
 * [project name]/src/main/assets/my_python.py
 * 
 * You need one path that is "../assets/my_python.py" and one path that is "/main/assets/my_python.py"
 *
 */

public class PythonFileValidation {
	
	/**
	 * 
	 * Does the verification and fixing process automatically for a desired Python file to be in your
	 * deployment environment. First argument is where you expect to find your Python file to run it
	 * (this is also where the Python file will be copied/written to if it is not there); the second
	 * argument is the file name.
	 * 
	 * Arguments three and four are the internal references from your project of where the template
	 * Python file is to verify correctness of the external copy and, if needed, make a new copy of
	 * that file.
	 * 
	 * @param localFileStorage
	 * @param fileName
	 * @param localPath
	 * @param jarPath
	 */
	
	public static void verifyPythonFileNear(String localFileStorage, String fileName, String localPath, String jarPath) {
		File g = new File(localFileStorage);
		g.mkdirs();
		File f = new File(localFileStorage + "/" + fileName);
		if(!f.exists() || !validateFileCorrect(f, localPath, jarPath)) {
			try {
				ArrayList<String> contents = getTemplatePythonContents(localPath, jarPath);
				f.delete();
				f.createNewFile();
				RandomAccessFile raf = new RandomAccessFile(f, "rw");
				for(String s : contents) {
					raf.writeBytes(s + "\n");
				}
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private static boolean validateFileCorrect(File f, String localPath, String jarPath) {
		System.out.println("Validating " + f.getName() + " file correctness");
		Scanner sc = null;
		try {
			sc = new Scanner(f);
			ArrayList<String> compare = new ArrayList<String>();
			while(sc.hasNextLine()) {
				compare.add(sc.nextLine());
			}
			sc.close();
			ArrayList<String> correct = getTemplatePythonContents(localPath, jarPath);
			if(compare.size() != correct.size()) {
				System.out.println(f.getName() + " file not validated, rewrite");
				return false;
			}
			for(int i = 0; i < compare.size(); i++) {
				if(!compare.get(i).equals(correct.get(i))) {
					System.out.println(f.getName() + " file not validated, rewrite");
					return false;
				}
			}
			System.out.println(f.getName() + " file validated, safe to use");
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(f.getName() + " file not validated, no rewrite performed; error occured");
			return false;
		}
		
	}
	
	/**
	 * 
	 * When you want to access a file that is nearby in your dev environment but will be packaged into
	 * a .jar as a library or .exe, you need to have two file path styles as the references are different.
	 * 
	 * As the function tries both, which is which is arbitrary, but if a file were located at:
	 * 
	 * [project name]/src/main/assets/my_python.py
	 * 
	 * You need one path that is "../assets/my_python.py" and one path that is "/main/assets/my_python.py"
	 * 
	 * @param localPath
	 * @param jarPath
	 * @return
	 */
	
	private static ArrayList<String> getTemplatePythonContents(String localPath, String jarPath) {
		InputStream is = null;
		Scanner sc;
		try {
			is = PythonFileValidation.class.getResourceAsStream(localPath);
			sc = new Scanner(is);
		}
		catch(Exception e) {
			is = PythonFileValidation.class.getResourceAsStream(jarPath);
			sc = new Scanner(is);
		}
		ArrayList<String> out = new ArrayList<String>();
		while(sc.hasNextLine()) {
			out.add(sc.nextLine());
		}
		sc.close();
		return out;
	}
}
