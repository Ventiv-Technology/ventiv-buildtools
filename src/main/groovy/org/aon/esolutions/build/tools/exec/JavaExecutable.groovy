package org.aon.esolutions.build.tools.exec;

public class JavaExecutable extends Executable {

	public JavaExecutable() {
		super(ExecutableFinder.findExecutable("java"));
	}
	
	public String getVersion() {
		return runGrepOnStandardError(["-version"] as String[], /java version "(.*)"/, 1);
	}

}
