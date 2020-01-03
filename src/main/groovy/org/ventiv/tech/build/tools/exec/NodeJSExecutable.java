/**
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
		arguments.add(appJsFile.getAbsolutePath());
		Collections.addAll(arguments, applicationParameters);
		
		return run(arguments);				
	}
	
	public int runScript(File appJsFile, String[] applicationParameters, Closure stdOutCallback, Closure stdErrCallback) {
		List<String> arguments = new ArrayList<String>();
		arguments.add(appJsFile.getAbsolutePath());

        if (applicationParameters != null && applicationParameters.length > 0)
		    Collections.addAll(arguments, applicationParameters);
		
		return run(arguments, stdOutCallback, stdErrCallback);
	}

}
