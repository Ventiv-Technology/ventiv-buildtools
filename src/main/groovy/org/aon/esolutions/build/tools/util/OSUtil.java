/**
 * Copyright (c) 2013 Aon eSolutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
