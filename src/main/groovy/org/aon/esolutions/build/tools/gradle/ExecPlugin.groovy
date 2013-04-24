package org.aon.esolutions.build.tools.gradle;

import java.io.File;

import org.aon.esolutions.build.tools.exec.Executable;
import org.aon.esolutions.build.tools.exec.ExecutableFinder;
import org.aon.esolutions.build.tools.exec.JavaExecutable
import org.aon.esolutions.build.tools.exec.NodeJSExecutable
import org.aon.esolutions.build.tools.exec.NpmExecutable
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.StdinSwapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecPlugin implements Plugin<Project>  {
	
	public static final Logger log = LoggerFactory.getLogger(ExecPlugin.class)

	@Override
	public void apply(Project project) {
		project.extensions.java = new JavaExecutable();
		project.extensions.npm = new NpmExecutable();
		project.extensions.nodeJs = new NodeJSExecutable();
		
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
		String[] scriptArguments
		String standardOutLogLevel = LogLevel.NONE.toString();
		String standardErrorLogLevel = LogLevel.NONE.toString();
		NodeJSExecutable nodeJs = new NodeJSExecutable();
		
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
