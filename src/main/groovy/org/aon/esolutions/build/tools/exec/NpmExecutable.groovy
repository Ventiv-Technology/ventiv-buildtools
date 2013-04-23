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
package org.aon.esolutions.build.tools.exec;

public class NpmExecutable extends Executable {
	
	public NpmExecutable() {
		super(ExecutableFinder.findExecutable("npm"));
	}
	
	public boolean isInstalled() {
		try {
			return getExecutableFile().exists() && run([] as String[]) == 0
		} catch (Exception e) {
			return false;
		}
	}
	
	public List<NpmPackage> listPackages(boolean global = false) {
		List<NpmPackage> answer = [];
		
		run(getArguments(global, "ls")) {
			def matcher = (it =~ /.* (.*)@(.*)/)
			if (matcher.matches()) {
				answer.add(new NpmPackage([packageName: matcher[0][1], versionNumber: matcher[0][2]]))
			}
		}
		
		return answer;
	}
	
	public String getInstalledPackageVersion(String packageName, boolean global = false) {
		NpmPackage npmPackage = listPackages(global).find { it.packageName == packageName }
		if (npmPackage) {
			return npmPackage.versionNumber
		} else
			return null;
	}
	
	public boolean installPackage(String packageName, boolean global = false) {
		try {
			return run(getArguments(global, "install", packageName)) == 0
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean removePackage(String packageName, boolean global = false) {
		try {
			return run(getArguments(global, "rm", packageName)) == 0
		} catch (Exception e) {
			return false;
		}		
	}
	
	private String[] getArguments(boolean global, String...args) {
		List<String> arguments = [];
		if (global)
			arguments = ["-g"]
		
		arguments.addAll(args);
		
		return arguments;
	}	
	
	public static class NpmPackage {
		public String packageName;
		public String versionNumber;
	}
	

}
