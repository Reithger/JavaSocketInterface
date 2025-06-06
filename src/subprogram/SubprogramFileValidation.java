package subprogram;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 * This class automates the deployment and verifying integrity of Subprogram files near your Java project
 * that you are calling upon.
 * 
 * 
 * When you want to access a file that is nearby in your dev environment but will be packaged into
 * a .jar as a library or .exe, you need to have two file path styles as the references are different.
 * 
 * As the function tries both, which is which is arbitrary, but if a file were located at:
 * 
 * [project name]/src/main/assets/my_file.py
 * 
 * You need one path that is "../assets/my_file.py" and one path that is "/main/assets/my_file.py"
 *
 */

public class SubprogramFileValidation {
	
	/**
	 * 
	 * Does the verification and fixing process automatically for a desired Subprogram file to be in your
	 * deployment environment. First argument is where you expect to find your Subprogram file to run it
	 * (this is also where the Subprogram file will be copied/written to if it is not there); the second
	 * argument is the file name.
	 * 
	 * Arguments three and four are the internal references from your project of where the template
	 * Subprogram file is to verify correctness of the external copy and, if needed, make a new copy of
	 * that file.
	 * 
	 * @param localFileStorage
	 * @param fileName
	 * @param localPath
	 * @param jarPath
	 */
	
	public static boolean verifySubprogramFileNear(String localFileStorage, String fileName, String localPath, String jarPath) {
		File g = new File(localFileStorage);
		g.mkdirs();
		File f = new File(localFileStorage + "/" + fileName);
		if(!f.exists() || !validateFileCorrect(f, localPath, jarPath)) {
			try {
				ArrayList<String> contents = getTemplateSubprogramContents(localPath, jarPath);
				f.delete();
				f.createNewFile();
				RandomAccessFile raf = new RandomAccessFile(f, "rw");
				for(String s : contents) {
					raf.writeBytes(s + "\n");
				}
				raf.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			
		}
		return true;
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
			ArrayList<String> correct = getTemplateSubprogramContents(localPath, jarPath);
			if(compare.size() != correct.size()) {
				System.err.println(f.getName() + " file not validated, rewrite");
				return false;
			}
			for(int i = 0; i < compare.size(); i++) {
				if(!compare.get(i).equals(correct.get(i))) {
					System.err.println(f.getName() + " file not validated, rewrite");
					return false;
				}
			}
			System.out.println(f.getName() + " file validated, safe to use");
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println(f.getName() + " file not validated, no rewrite performed; error occured");
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
	 * [project name]/src/main/assets/my_file.py
	 * 
	 * You need one path that is "../assets/my_file.py" and one path that is "/main/assets/my_file.py"
	 * 
	 * @param localPath
	 * @param jarPath
	 * @return
	 */
	
	private static ArrayList<String> getTemplateSubprogramContents(String localPath, String jarPath) {
		InputStream is = null;
		Scanner sc;
		try {
			is = SubprogramFileValidation.class.getResourceAsStream(localPath);
			if(is == null) {
				is = SubprogramFileValidation.class.getResourceAsStream(jarPath);
			}
			sc = new Scanner(is);
		}
		catch(Exception e) {
			is = SubprogramFileValidation.class.getResourceAsStream(jarPath);
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
