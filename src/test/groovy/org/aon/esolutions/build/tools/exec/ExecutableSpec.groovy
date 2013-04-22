package org.aon.esolutions.build.tools.exec;

import static org.junit.Assert.*;

import org.junit.Test;

import spock.lang.Specification;

public class ExecutableSpec extends Specification {

	def "execute java version"() {
		when:
		Executable exe = ExecutableFinder.findExecutable("java")
		exe.run(["-version"] as String[])
		
		then:
		exe.getStandardError().indexOf("java version") > -1
	}
	
	def "utilize standard callback"() {
		when:
		boolean foundVersionString = false;
		Executable exe = ExecutableFinder.findExecutable("java")
		exe.run(["-version"] as String[], {}, { if (it.indexOf("java version") > -1) foundVersionString = true; });
		
		then:
		foundVersionString		
	}
	
	def "java version"() {
		when:
		String version = new JavaExecutable().getVersion()
		
		then:
		version
		version.startsWith("1.")
	}
}
