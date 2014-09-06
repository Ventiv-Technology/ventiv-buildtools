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
package org.ventiv.tech.build.tools.exec;

import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class Executable {
	
	public static final Logger log = LoggerFactory.getLogger(Executable.class)
	
	private File executableFile = null;
	private StringBuffer standardOut = new StringBuffer();
	private StringBuffer standardError = new StringBuffer();
	private Map<String, String> environmentVariables = [:];

	public Executable(File executableFile) {
		this.executableFile = executableFile;
	}
	
	public Executable(Executable executable) {
		if (executable)
			executableFile = executable.getExecutableFile();
	}
	
	public int run(List<String> arguments, Closure stdOutCallback = null, Closure sdtErrCallback = null) {
		return run(arguments as String[], stdOutCallback, sdtErrCallback);
	}
	
	/**
	 * Executes the file that was passed in on the constructor.  Optionally takes a callback
	 * for the Standard Output and Standard Error streams.  Will pass each line as the parameter
	 * into the callback.
	 * 
	 * @param stdOutCallback Callback for Standard In, called once for each line
	 * @param sdtErrCallback Callback for Standard error, called once for each line
	 */
	public int run(String[] arguments, Closure stdOutCallback = null, Closure sdtErrCallback = null) {
		standardOut.setLength(0);
		standardError.setLength(0);
		
		def arugmentsWithFile = [executableFile.getAbsolutePath()]
		arugmentsWithFile.addAll(arguments)
		
		List<String> env = environmentVariables.collect { k, v -> "$k=$v" }
		env += System.getenv().collect { k, v -> "$k=$v" }
		
		log.info("Executing Shell Command: $arugmentsWithFile")
		log.info("Additional Environment Variables: $environmentVariables")
		
		def proc;		
		if (environmentVariables)
			proc = arugmentsWithFile.execute(env, new File("."));
		else
			proc = arugmentsWithFile.execute();
		
		proc.consumeProcessOutput(standardOut, standardError);
		proc.waitFor()
		
		if (log.isDebugEnabled()) {
			log.debug("Shell standard out: $standardOut")
			log.debug("Shell standard error: $standardError")
		}		
		
		if (stdOutCallback)
			standardOut.eachLine(stdOutCallback)
			
		if (sdtErrCallback)
			standardError.eachLine(sdtErrCallback)
			
		if (proc.exitValue()!=0){
			throw new RuntimeException("Error Executing $executableFile.  Return code ${proc.exitValue()}")
		}
		
		if (log.isInfoEnabled())
			log.info("Shell Command finished executing. Return code: ${proc.exitValue()}")
		
		return proc.exitValue();
	}
	
	public String runGrepOnStandardOut(String[] arguments, String patternToMatch, int groupToReturn) {
		String foundMatch = null;
		
		run(arguments, {
			def matcher = (it =~ patternToMatch)
			if (matcher.matches())
				foundMatch = matcher[0][groupToReturn]
		})
		
		return foundMatch;
	}
	
	public String runGrepOnStandardError(String[] arguments, String patternToMatch, int groupToReturn) {
		String foundMatch = null;
		
		run(arguments, null, {
			def matcher = (it =~ patternToMatch)
			if (matcher.matches())
				foundMatch = matcher[0][groupToReturn]
		})	
		
		return foundMatch;	
	}
	
	public void setEnvironmentVariable(String variableName, String variableValue) {
		if (log.isDebugEnabled())
			log.debug("Adding environment variable: $variableName = $variableValue")
		
		environmentVariables.put(variableName, variableValue);
	}
	
	public void addEnvironmentVariables(Map<String, String> envVariables) {
		if (log.isDebugEnabled())
			log.debug("Adding environment variables: $envVariables")
			
		environmentVariables.putAll(envVariables);
	}
	
	public String getStandardOut() {
		return standardOut.toString()
	}
	
	public String getStandardError() {
		return standardError.toString();
	}	
	
	public File getExecutableFile() {
		return executableFile;
	}
	
	public void setExecutableFile(File executableFile) {
		this.executableFile = executableFile;
	}
	
}
