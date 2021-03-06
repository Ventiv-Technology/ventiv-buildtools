# Ventiv Technolgy Build Tools (Gradle) #

This project consists of several Plugins for Gradle, all were developed by Ventiv Technology in order to provide more efficient and readable build files.  Since there is no proprietary code in here, it has been released under the Apache-2.0 License for all to use.

## Exec Plugin ##

Enable this plugin by: `apply plugin: 'exec'`

See examples in: `pluginTestExec.gradle`

This plugin enables easy execution of programs on the host OS.  It works by finding the executable on the path, or by a specified location.  There are several built in executables:

- Java - The java executable (JavaExecutable)
- Npm - The NPM (Node Packaged Modules) executable.  Useful for installing Node.JS Scripts (NpmExecutable)
- NodeJS - Node JS executable.  Very useful for compiling JavaScript tasks (NodeJSExecutable)
- PhantomJS - A Headless browser. (PhantomJSExecutable)
- Gulp - No special configuration needed, adds task `installGulp` (uses NPM and executes if you run a gulp task) and invisible `gulp_<TASK>` tasks that you can call and/or depend on. 
Acts as a wrapper for gulp execution.  Ex: `./gradlew gulp_build` is equivalent to calling `gulp build`.  Your `gulpfile.js` must be present in the project root.

Each of these executables extend the Executable class.  Please see the GroovyDoc for each of the classes for any methods you can call.  For example, on the Java executable, you can do either of the following:

Execute Java Directly (with arguments and callbacks for Standard Out / Standard Error):

	java.run(["-version"], {
        println "Std Out: $it"
    }, {
        println "Std Err: $it"
    })

Or use a built in method:

	println "Version: ${java.getVersion()}"

There are also two containers that you can extend as well:

### Executables ###

This is a container so you can define any executables that you want.  The idea here is that you define the executable in a definition and then use that executable in a task.  For example:

	executables {
		gradle {
			location = "gradlew.bat"
		}
	}

Then in a task, you should be able to do the following:

	executables.gradle.run("--version")

PLEASE NOTE: This functionality is not working yet, but is planned.

### Node JS Scripts ###

Node JS Scripts are the primary reason why this plugin was developed.  It provides for a VERY simple way of executing NodeJS scripts and doing various tasks like Karma testing.  An example of the configuration would be like the following:

	nodeJsScripts {
	    karma {
	        scriptLocation = file("node_modules/karma/bin/karma")
	        standardOutLogLevel = "DEBUG"
	        standardErrorLogLevel = "ALWAYS"
	    }
	
	    plato {
	        standardOutLogLevel = "DEBUG"
	        standardErrorLogLevel = "ALWAYS"
	    }
	}

Then, in a task, you can do something like the following:

	nodeJsScripts.karma.run("start", pathToKarmaConfig, "--no-auto-watch", "--browsers", "PhantomJS", "--single-run")

In the NodeJSScript, you can configure several things (all of which are OPTIONAL):

- scriptLocation - Location of the script.  Defaults to asking NPM where the script exist.
- global - Should this be installed from NPM globally (true) or locally (false).  Defaults to false.
- scriptArguments - Arguments that should be provided when calling run.  These will OVERRIDE any arguments passed to the run method
- environmentVariables - Map of variables to place in the running environment.
- standardOutLogLevel - INFO, DEBUG, ALWAYS, or NONE.  Level to log the Standard output.  Defaults to NONE
- standardErrorLogLevel - INFO, DEBUG, ALWAYS, or NONE.  Level to log the Standard error.  Defaults to NONE 

## JaCoCo Results Plugin ##

Enable this plugin by: `apply plugin: 'jacoco-results'`

See examples in: `pluginTestJaCoCo.gradle` and `jacocoMultiprojectTest/build.gradle`

NOTE: This plugin requires at least Gradle 1.6, as it includes the JaCoCo plugin.

This plugin is to build on top of the JaCoCo plugin that gradle provides out of the box (currently incubating).  The point here is to fail the build if certain levels of coverage are not met.  This of course is done in a configurable manner, via the jacocoResults closure, for example:

    jacocoResults {
        aggregationLevel = "Project"
        type = "Instruction"
        threshold = 95
		excludes = [
            "org.ventiv.tech.build.tools.util"
    	]
    }

The following are the properties (none required - all default):

- aggregationLevel - What level do you want coverage aggregated up to?  Defaults to SubProject. Valid values are: 
	- Project - All projects in the build.  Including the root project, as well as any subprojects.
	- SubProject - At the sub-project level.  Only makes sense if doing multi-project builds in gradle, but will work the same as Project otherwise.
	- Package - Java Package Level.  If any single package is below the threshold, build fails.
	- Class - Java Class Level.  If any single class is below the threshold, build fails.
	- Method - Java Method Level.  If any single method is below the threshold, build fails.
- type - What Coverage type are you interested in?  Defaults to Line.  This is the list from JaCoCo: Class, Method, Complexity, Line, Branch, Instruction
- threshold - What percentage coverage is required to pass the build.  Any coverage below this percentage will fail.
- excludes - List of Regular Expression strings to exclude from failures.

## Test Task Plugin ##

Enable this plugin by: `apply plugin: 'test-task'`

See examples in: `pluginTestTestTask.gradle`

This plugin will look at all of your test classes that would normally run as part of 'gradle test' and separate them out by looking at the annotation @org.ventiv.tech.build.tools.test.GradleTestTask.  So, if you have the following as a test:

    @GradleTestTask("QA")
    class QATestSpec extends Specification { }

Then you will automatically get a new task (as seen by 'gradle tasks'):

    testQA - Runs the unit tests that are annotated with org.ventiv.tech.build.tools.test.GradleTestTask("QA")

These tests will ONLY run when you execute the task 'testQA' and will be removed from the normal 'test' task.  The outputs will also have their own directories as to not collide.

This is all accomplished by using Groovy (a natural dependency of Gradle) to 'compile' the test classes up to the 'CONVERSION' phase.  This way, Groovy just parses the classes into an AST, and doesn't try to resolve any classes (thus enabling it to read ANY source).  It will look for that particular annotation, and add it as an exclusion to the test task, and as an inclusion to a new Test task.