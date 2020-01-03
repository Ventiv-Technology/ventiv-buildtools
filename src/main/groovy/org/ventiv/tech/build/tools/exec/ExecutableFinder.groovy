/*
 * Copyright (c) 2020 Ventiv Technology
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
package org.ventiv.tech.build.tools.exec;

import org.ventiv.tech.build.tools.util.OSUtil;

public class ExecutableFinder {
	
	private static ExecutableFinder instance = new ExecutableFinder();

	public static final Executable findExecutable(String executableName) {
		instance.doFindExecutable(executableName);
	}
	
	public Executable doFindExecutable(String executableName) {
		// First, see if 'which' is available to us
		try {
			return new Executable(new File("which $executableName".execute().text.trim()));
		} catch (def e) {}
		
		// Next, see if it's on the PATH
		File pathExecutable = findExecutableOnPath(executableName);
		if (pathExecutable?.exists())
			return new Executable(pathExecutable);
		
	}
	
	private File findExecutableOnPath(String executableName)
	{
		String systemPath = System.getenv("PATH");
		if (systemPath == null || systemPath.length() == 0)
			systemPath = System.getenv("path");
			
		String[] pathDirs = systemPath.split(File.pathSeparator);
   
		File fullyQualifiedExecutable = null;
		for (String pathDir : pathDirs)
		{
			File file = new File(pathDir, executableName);
			// First, try our extensions
			for (String anExtension : OSUtil.getExecutableExtensions()) {
				file = new File(pathDir, executableName + anExtension);
				if (file.isFile())
					return file;
			}
			
			if (file.isFile())
				return file;
		}
		return fullyQualifiedExecutable;
	}

}
