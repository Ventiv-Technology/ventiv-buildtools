package org.aon.esolutions.build.tools.util;

public class OSUtil {
	
	private static String OS = System.getProperty("os.name").toLowerCase();

	public static boolean isWindows() {
		 return (OS.indexOf("win") >= 0);
 	}
 
	public static boolean isMac() {
 		return (OS.indexOf("mac") >= 0);
 	}
 
	public static boolean isUnix() {
 		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
 	}
 
	public static boolean isSolaris() {
 		return (OS.indexOf("sunos") >= 0);
 	}
	
	public static String[] getExecutableExtensions() {
		if (isWindows())
			return new String[] { ".exe", ".bat", ".cmd" };
		else if (isUnix())
			return new String[] { ".sh" };
		else
			return new String[0];
	}

}
