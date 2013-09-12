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

import groovy.json.JsonSlurper;
import groovy.util.logging.Commons;

public class NpmExecutable extends Executable {
	
	private JsonSlurper jsonSlurper = new JsonSlurper(); 
	
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
		
		// Get the path to the node modules - https://npmjs.org/doc/global.html (Node Modules)
		run(getArguments(global, "prefix"));
		def prefixPath = getStandardOut().trim();
		File nodeModules = new File(prefixPath + "/lib/node_modules");
		if (nodeModules.exists() == false)
			nodeModules = new File(prefixPath + "/node_modules");				
		
		run(getArguments(global, "ls"));
		def lsOutput = jsonSlurper.parseText(getStandardOut());
		
		lsOutput.dependencies.each { packageName, packageObj ->
			NpmPackage npmPackage = new NpmPackage();
			answer << npmPackage
			
			npmPackage.packageName = packageName;
			npmPackage.versionNumber = packageObj.version;
			
			if (nodeModules.exists()) {
				npmPackage.path = new File(nodeModules, "${packageName}/bin/${packageName}");
				
				if (npmPackage.path.exists() == false)
					npmPackage.path = null;
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
		if (getInstalledPackageVersion(packageName, global) != null)
			return false;
			
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
		List<String> arguments = ["-json"];
		if (global)
			arguments = ["-json", "-g"]
		
		arguments.addAll(args);
		
		return arguments;
	}	
	
	/**
	 * Does a depth first search (iterative) to prevent a stack overflow.
	 * 
	 * @param collector
	 * @param dependencyMap
	 */
	private void addDependenciesToList(List<NpmPackage> collector, def dependencyMap) {
			currentDependency?.each { packageName, packageObj ->
				NpmPackage npmPackage = new NpmPackage();
				collector << npmPackage;
				
				npmPackage.packageName = packageName;
				npmPackage.versionNumber = packageObj.version;
				if (packageObj.path)
					npmPackage.path = new File(packageObj.path);
					
				queue.push(packageObj.dependencies);
			}
	}
	
	public static class NpmPackage {
		public String packageName;
		public String versionNumber;
		public File path;
		
		@Override
		public String toString() {
			return "{packageName: $packageName, versionNumber: $versionNumber, path: $path}"
		}
		
		public File getPath() {
			return path;
		}
	}
	
	
	

}
