package org.aon.esolutions.build.tools.exec;

public class NodeJSExecutable extends Executable {

	public NodeJSExecutable() {
		super(ExecutableFinder.findExecutable("node"));
	}

}
