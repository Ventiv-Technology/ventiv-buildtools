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

import static org.junit.Assert.*;

import org.aon.esolutions.build.tools.exec.NpmExecutable.NpmPackage;
import org.junit.Test;

import spock.lang.Specification;

public class NpmExecutableSpec extends Specification {
	
	def "check if karma is installed"() {
		when:
		NpmExecutable exec = new NpmExecutable();
		
		then:
		exec.isInstalled();
		
		when:
		String karmaVersion = exec.getInstalledPackageVersion("karma")
		
		then:
		karmaVersion == null;
	}
	
	def "install / check / remove karma"() {
		when:
		NpmExecutable exec = new NpmExecutable();
		
		then:
		exec.isInstalled();
		
		when:
		boolean success = exec.installPackage("karma")
		
		then:
		success
		
		when:
		String karmaVersion = exec.getInstalledPackageVersion("karma")
		println "Installed Karma v$karmaVersion"
		
		then:
		karmaVersion.length() > 0;
		
		when:
		boolean rmSuccess = exec.removePackage("karma")
		
		then:
		rmSuccess		
	}
	
	def "install fail"() {
		when:
		NpmExecutable exec = new NpmExecutable();
		
		then:
		exec.isInstalled();
		
		when:
		boolean success = exec.installPackage("asdfasdfasdfasdfasdf")
		
		then:
		success == false
	}

	def "list local installed packages"() {
		when:
		NpmExecutable exec = new NpmExecutable();
		
		then:
		exec.isInstalled();
		
		when:
		List<NpmPackage> globalPackages = exec.listPackages(true);
		
		then:
		globalPackages;
		globalPackages[0].path
	}
	
	def "find node"() {
		when:
		NodeJSExecutable nodeJs = new NodeJSExecutable();
		
		then:
		nodeJs
		nodeJs.getExecutableFile().getParentFile().isDirectory()
	}

}