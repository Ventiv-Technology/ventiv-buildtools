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
package org.aon.esolutions.build.tools.gradle;

import org.aon.esolutions.build.tools.exec.Executable
import org.aon.esolutions.build.tools.exec.ExecutableFinder
import org.aon.esolutions.build.tools.exec.JavaExecutable
import org.aon.esolutions.build.tools.exec.NodeJSExecutable
import org.aon.esolutions.build.tools.exec.NpmExecutable
import org.aon.esolutions.build.tools.exec.PhantomJSExecutable
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class ExecPlugin implements Plugin<Project>  {
	
	public static final Logger log = LoggerFactory.getLogger(ExecPlugin.class)

	@Override
	public void apply(Project project) {
		project.extensions.java = new JavaExecutable();
		project.extensions.npm = new NpmExecutable();
		project.extensions.nodeJs = new NodeJSExecutable();
		project.extensions.phantomJs = new PhantomJSExecutable();
		
		def executableContainer = project.container(ExecutableContainer)
        project.extensions.executables = executableContainer
		
		def nodeJsContainer = project.container(NodeJSContainer)
		project.extensions.nodeJsScripts = nodeJsContainer
	}
	
	public static class ExecutableContainer {
		final String name;
		File location;
		
		ExecutableContainer(String name) {
			this.name = name;
		}
		
		public Executable getExecutable() {
			Executable answer = ExecutableFinder.findExecutable(name);
			
			if (location && location.exists() && location.isFile()) {
				if (answer == null)
					answer = new Executable(location);
				else
					answer.setExecutableFile(location);
			}
			
			return answer;
		}
	}
	
	public static class NodeJSContainer {
		final String name;
		File scriptLocation;
		boolean global = false;
		String[] scriptArguments
		Map<String, String> environmentVariables = [:];
		String standardOutLogLevel = LogLevel.NONE.toString();
		String standardErrorLogLevel = LogLevel.NONE.toString();
		NodeJSExecutable nodeJs = new NodeJSExecutable();
		NpmExecutable npm = new NpmExecutable();
		
		private Closure stdOutLogger = {
			if (LogLevel.valueOf(standardOutLogLevel) == LogLevel.INFO)
				log.info(it)
			else if (LogLevel.valueOf(standardOutLogLevel) == LogLevel.DEBUG)
				log.debug(it)
			else if (LogLevel.valueOf(standardOutLogLevel) == LogLevel.ALWAYS)
				println (it)	
		}
		
		private Closure stdErrLogger = {
			if (LogLevel.valueOf(standardErrorLogLevel) == LogLevel.INFO)
				log.info(it)
			else if (LogLevel.valueOf(standardErrorLogLevel) == LogLevel.DEBUG)
				log.debug(it)
			else if (LogLevel.valueOf(standardErrorLogLevel) == LogLevel.ALWAYS)
				println (it)
		}
		
		NodeJSContainer(String name) {
			this.name = name;
		}
		
		public int run(String...scriptArguments) {
			// Ensure that this is installed
			String installedVersion = npm.getInstalledPackageVersion(name, global);
			if (installedVersion == null || installedVersion.trim().length() == 0)
				npm.installPackage(name, global);
				
			// Set the Environment Variables
			nodeJs.addEnvironmentVariables(environmentVariables);
				
			// TODO: Automatically set scriptLocation based on NpmExecutable's package def (when it can read script location)
				
			// Run it			
			if (scriptArguments == null || scriptArguments.length == 0)
				nodeJs.runScript(scriptLocation, this.scriptArguments, stdOutLogger, stdErrLogger);
			else
				nodeJs.runScript(scriptLocation, scriptArguments, stdOutLogger, stdErrLogger);
		}
	}
	
	public static final enum LogLevel {
		INFO, DEBUG, ALWAYS, NONE
	}
	
}
