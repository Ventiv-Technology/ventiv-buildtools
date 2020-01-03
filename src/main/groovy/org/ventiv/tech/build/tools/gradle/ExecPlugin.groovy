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
package org.ventiv.tech.build.tools.gradle

import org.gradle.api.GradleException
import org.gradle.api.Task;
import org.ventiv.tech.build.tools.exec.Executable
import org.ventiv.tech.build.tools.exec.ExecutableFinder
import org.ventiv.tech.build.tools.exec.JavaExecutable
import org.ventiv.tech.build.tools.exec.NodeJSExecutable
import org.ventiv.tech.build.tools.exec.NpmExecutable
import org.ventiv.tech.build.tools.exec.PhantomJSExecutable
import org.ventiv.tech.build.tools.exec.NpmExecutable.NpmPackage;
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.ventiv.tech.build.tools.gradle.tasks.GulpTask

public class ExecPlugin implements Plugin<Project>  {
	
	public static final Logger log = LoggerFactory.getLogger(ExecPlugin.class)

    public static final String GULP_TASK_PREFIX = 'gulp_'

	@Override
	public void apply(Project project) {
		project.extensions.add("javaExec", new JavaExecutable());
		project.extensions.add("npmExec", new NpmExecutable());
		project.extensions.add("nodeJsExec", new NodeJSExecutable());
		project.extensions.add("phantomJsExec", new PhantomJSExecutable());
		
		def executableContainer = project.container(ExecutableContainer)
        project.extensions.add("executables", executableContainer)
		
		def nodeJsContainer = project.container(NodeJSContainer)
		project.extensions.add("nodeJsScripts", nodeJsContainer)

        addGulpTasks(project)
	}

    protected void addGulpTasks(Project project) {
        project.task('installGulp') {
            description 'install gulp locally using npm'

            outputs.dir(project.file('node_modules/gulp'))

            doLast {
                logger.lifecycle('Installing gulp...')
                project.npm.installPackage('gulp')

                if (!project.file(GulpTask.GULP_PATH).isFile()) {
                    throw new GradleException('gulp failed to install, try with npm manually and look for errors')
                }
            }
        }

        project.tasks.addRule("Pattern: ${GULP_TASK_PREFIX}<TASK>  to run `gulp <TASK>`") { String taskName ->
            if (taskName.startsWith(GULP_TASK_PREFIX)) {
                Task task = project.tasks.create(taskName, GulpTask.class)
                task.dependsOn 'installGulp'
                task.ext.args = [taskName - GULP_TASK_PREFIX]
                return task
            }
        }
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
        boolean npmPackage = true;
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
            NpmPackage npmPackage;
            if (npmPackage) {
                npmPackage = npm.listPackages(global).find { it.packageName == name };
                if (npmPackage == null) {
                    npm.installPackage(name, global);
                    npmPackage = npm.listPackages(global).find { it.packageName == name };
                }
            }
				
			// Set the Environment Variables
			nodeJs.addEnvironmentVariables(environmentVariables);
			
			File scriptToExecute = npmPackage?.getPath();
			if (this.scriptLocation && this.scriptLocation.exists())
				scriptToExecute = this.scriptLocation;

			// Final check to ensure we found the script location
			if (scriptToExecute == null || scriptToExecute.exists() == false)
				throw new RuntimeException("Unable to find executable for $name in: $scriptToExecute");
				
			// Run it			
			if (scriptArguments == null || scriptArguments.length == 0)
				nodeJs.runScript(scriptToExecute, this.scriptArguments, stdOutLogger, stdErrLogger);
			else
				nodeJs.runScript(scriptToExecute, scriptArguments, stdOutLogger, stdErrLogger);
		}
		
		public String getStandardOut() {
			return nodeJs.getStandardOut();
		}
		
		public String getStandardError() {
			return nodeJs.getStandardError();
		}
	}
	
	public static final enum LogLevel {
		INFO, DEBUG, ALWAYS, NONE
	}
	
}
