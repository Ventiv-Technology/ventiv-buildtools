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
package org.aon.esolutions.build.tools.gradle.tasks

import org.aon.esolutions.build.tools.gradle.JaCoCoResultsPluginExtension
import org.aon.esolutions.build.tools.gradle.exception.ThresholdMissedException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 *
 * @author John Crygier
 */
class JaCoCoResultsTaskSpec extends Specification {

    Project project;

    def setup() {
        project = ProjectBuilder.builder().build();

        project.apply plugin: 'jacoco-results'
        project.apply plugin: 'groovy'
    }

    def cleanup() {
        project.getRootDir().deleteDir()
    }

    @Unroll("Verify AggregationLevel(#aggregationLevel) with Type(#type) And Threshold (#threshold) Runs Successfully")
    def "successful run"(String aggregationLevel, String type, double threshold, int expectedNumberOfCounters) {
        when:
        JaCoCoResultsTask task = executeTask(aggregationLevel, type, threshold);

        then:
        notThrown(Throwable)
        task.allCounters.size() == expectedNumberOfCounters

        where:
        aggregationLevel | type          | threshold | expectedNumberOfCounters
        "Project"        | "Class"       | 10        | 1
        "Project"        | "Class"       | 100       | 1 // Project isn't handled by this task
        "SubProject"     | "Class"       | 31        | 1
        "Package"        | "Class"       | 0         | 5

        "SubProject"     | "Method"      | 16        | 1
        "Package"        | "Method"      | 0         | 5
        "Class"          | "Method"      | 0         | 50

        "SubProject"     | "Complexity"  | 7.5       | 1
        "Package"        | "Complexity"  | 0         | 5
        "Class"          | "Complexity"  | 0         | 50
        "Method"         | "Complexity"  | 0         | 299

        "SubProject"     | "Line"        | 11.8      | 1
        "Package"        | "Line"        | 0         | 5
        "Class"          | "Line"        | 0         | 49
        "Method"         | "Line"        | 0         | 102

        "SubProject"     | "Branch"      | 3.01      | 1
        "Package"        | "Branch"      | 0         | 5
        "Class"          | "Branch"      | 0         | 36
        "Method"         | "Branch"      | 0         | 63

        "SubProject"     | "Instruction" | 12.95     | 1
        "Package"        | "Instruction" | 0         | 5
        "Class"          | "Instruction" | 0         | 50
        "Method"         | "Instruction" | 0         | 299
    }

    @Unroll("Verify AggregationLevel(#aggregationLevel) with Type(#type) And Threshold (#threshold) Runs With Exception")
    def "failed run"(String aggregationLevel, String type, double threshold, String expectedMessage) {
        when:
        JaCoCoResultsTask task = executeTask(aggregationLevel, type, threshold);

        then:
        def e = thrown(ThresholdMissedException)
        e.message.startsWith(expectedMessage)

        where:
        aggregationLevel | type          | threshold | expectedMessage
        "SubProject"     | "Class"       | 32.5      | "The following coverage thresholds were not met: \n\tSubProject aon-buildtools (Class Level): 32.0%"
        "Package"        | "Class"       | 25        | "The following coverage thresholds were not met: \n\tPackage org/aon/esolutions/build/tools/gradle/tasks (Class Level): 7.14285714%\n\tPackage org/aon/esolutions/build/tools/gradle/exception (Class Level): 0.0%"

        "SubProject"     | "Method"      | 100       | "The following coverage thresholds were not met: \n\tSubProject aon-buildtools (Method Level): 16.05351171%"
        "Package"        | "Method"      | 10        | "The following coverage thresholds were not met: \n\tPackage org/aon/esolutions/build/tools/gradle/tasks (Method Level): 2.5974026%\n\tPackage org/aon/esolutions/build/tools/gradle/exception (Method Level): 0.0%"
        "Class"          | "Method"      | 1         | "The following coverage thresholds were not met: \n\tClass org/aon/esolutions/build/tools/util/FileUtil\$_unzipFile_closure1 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/util/FileUtil\$_unzipFile_closure1_closure2 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/util/FileUtil (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_scanCoverageReports_closure3 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoRootResultsTask\$_aggregateChildren_closure1 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoRootResultsTask\$_aggregateChildren_closure1_closure2 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_scanCoverageReports_closure2 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_closure1_closure8 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_scanReport_closure5 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_scanReport_closure4 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_scanReport_closure7 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_scanReport_closure6 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$Counter (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoRootResultsTask\$_aggregateChildren_closure1_closure2_closure3 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoRootResultsTask (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/Executable (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/NpmExecutable (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/ExecutableFinder (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/Executable\$_runGrepOnStandardOut_closure3 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/NpmExecutable\$_addDependenciesToList_closure3 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/PhantomJSExecutable (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/JavaExecutable (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$LogLevel (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$ExecutableContainer (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/JaCoCoResultsPlugin (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$_NodeJSContainer_run_closure3 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$NodeJSContainer (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$_NodeJSContainer_run_closure4 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/JaCoCoResultsPluginExtension (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$_NodeJSContainer_closure2 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$_NodeJSContainer_closure1 (Method Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/exception/ThresholdMissedException (Method Level): 0.0%"

        "SubProject"     | "Instruction" | 100       | "The following coverage thresholds were not met: \n\tSubProject aon-buildtools (Instruction Level): 12.95381106%"
        "Package"        | "Instruction" | 7         | "The following coverage thresholds were not met: \n\tPackage org/aon/esolutions/build/tools/util (Instruction Level): 5.01730104%\n\tPackage org/aon/esolutions/build/tools/gradle/tasks (Instruction Level): 0.38961039%\n\tPackage org/aon/esolutions/build/tools/gradle/exception (Instruction Level): 0.0%"
        "Class"          | "Instruction" | 1         | "The following coverage thresholds were not met: \n\tClass org/aon/esolutions/build/tools/util/FileUtil\$_unzipFile_closure1 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/util/FileUtil\$_unzipFile_closure1_closure2 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/util/FileUtil (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_scanCoverageReports_closure3 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoRootResultsTask\$_aggregateChildren_closure1 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoRootResultsTask\$_aggregateChildren_closure1_closure2 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_scanCoverageReports_closure2 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_closure1_closure8 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_scanReport_closure5 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_scanReport_closure4 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_scanReport_closure7 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$_scanReport_closure6 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask\$Counter (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoResultsTask (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoRootResultsTask\$_aggregateChildren_closure1_closure2_closure3 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/tasks/JaCoCoRootResultsTask (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/Executable (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/NpmExecutable (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/ExecutableFinder (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/Executable\$_runGrepOnStandardOut_closure3 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/NpmExecutable\$_addDependenciesToList_closure3 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/PhantomJSExecutable (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/exec/JavaExecutable (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$LogLevel (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$ExecutableContainer (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/JaCoCoResultsPlugin (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$_NodeJSContainer_run_closure3 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$NodeJSContainer (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$_NodeJSContainer_run_closure4 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/JaCoCoResultsPluginExtension (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$_NodeJSContainer_closure2 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/ExecPlugin\$_NodeJSContainer_closure1 (Instruction Level): 0.0%\n\tClass org/aon/esolutions/build/tools/gradle/exception/ThresholdMissedException (Instruction Level): 0.0%"
        "Method"         | "Instruction" | 1         | "The following coverage thresholds were not met: \n\tMethod org/aon/esolutions/build/tools/util/OSUtil.<init> (Instruction Level): 0.0%\n\tMethod org/aon/esolutions/build/tools/util/OSUtil.isMac (Instruction Level): 0.0%"
    }

    @Unroll("Verify Root AggregationLevel(#aggregationLevel) with Type(#type) And Threshold (#threshold) Runs Successfully")
    def "successful run of root"(String aggregationLevel, String type, double threshold) {
        when:
        executeRootTask(aggregationLevel, type, threshold);

        then:
        notThrown(Throwable)

        where:
        aggregationLevel | type          | threshold
        "Project"        | "Class"       | 31
        "Project"        | "Method"      | 16
        "Project"        | "Complexity"  | 7.5
        "Project"        | "Branch"      | 3
        "Project"        | "Line"        | 11
        "Project"        | "Instruction" | 12.9
    }

    @Unroll("Verify Root AggregationLevel(#aggregationLevel) with Type(#type) And Threshold (#threshold) Fails")
    def "failed run of root"(String aggregationLevel, String type, double threshold, String expectedMessage) {
        when:
        executeRootTask(aggregationLevel, type, threshold);

        then:
        def e = thrown(ThresholdMissedException)
        e.message == expectedMessage

        where:
        aggregationLevel | type          | threshold | expectedMessage
        "Project"        | "Class"       | 33        | "The coverage threshold was not met for all projects overall.  Class coverage was 32.0%"
        "Project"        | "Method"      | 17        | "The coverage threshold was not met for all projects overall.  Method coverage was 16.05351171%"
        "Project"        | "Complexity"  | 8.5       | "The coverage threshold was not met for all projects overall.  Complexity coverage was 7.50750751%"
        "Project"        | "Branch"      | 4         | "The coverage threshold was not met for all projects overall.  Branch coverage was 3.0261348%"
        "Project"        | "Line"        | 12        | "The coverage threshold was not met for all projects overall.  Line coverage was 11.85410334%"
        "Project"        | "Instruction" | 13.9      | "The coverage threshold was not met for all projects overall.  Instruction coverage was 12.95381106%"
    }

    private def executeTask(String aggregationLevel, String type, double threshold) {
        JaCoCoResultsPluginExtension extension = new JaCoCoResultsPluginExtension(null);
        extension.aggregationLevel = aggregationLevel
        extension.type = type
        extension.threshold = threshold;

        // Kick off the task
        JaCoCoResultsTask task = project.tasks.withType(JaCoCoResultsTask).iterator().next()

        // Set the sample Coverage Report (Take from point in time of this project) & Extension
        task.coverageReports = [new File(JaCoCoResultsTaskSpec.class.getResource("/jacocoTestReport.xml").getFile())]
        task.extension = extension

        task.scanCoverageReports();

        return task;
    }

    private def executeRootTask(String aggregationLevel, String type, double threshold) {
        executeTask(aggregationLevel, type, threshold);

        // Kick off the task
        JaCoCoRootResultsTask task = project.tasks.withType(JaCoCoRootResultsTask).iterator().next()
        task.aggregateChildren();
    }

}
