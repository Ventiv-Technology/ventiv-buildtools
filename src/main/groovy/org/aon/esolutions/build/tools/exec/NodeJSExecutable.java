package org.aon.esolutions.build.tools.exec;

import groovy.lang.Closure;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodeJSExecutable extends Executable {

	public NodeJSExecutable() {
		super(ExecutableFinder.findExecutable("node"));
	}
	
	public int runScript(File appJsFile, String...applicationParameters) {
		List<String> arguments = new ArrayList<String>();
		Collections.addAll(arguments, applicationParameters);
		
		return run(arguments);				
	}
	
	public int runScript(File appJsFile, String[] applicationParameters, Closure stdOutCallback, Closure stdErrCallback) {
		List<String> arguments = new ArrayList<String>();
		Collections.addAll(arguments, applicationParameters);
		
		return run(arguments, stdOutCallback, stdErrCallback);
	}

}
