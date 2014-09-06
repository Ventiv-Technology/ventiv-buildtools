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
package org.ventiv.tech.build.tools.gradle

import org.ventiv.tech.build.tools.gradle.tasks.JaCoCoResultsTask
import org.ventiv.tech.build.tools.gradle.tasks.JaCoCoRootResultsTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testing.jacoco.tasks.JacocoReport
import spock.lang.Specification

/**
 *
 *
 * @author John Crygier
 */
class JaCoCoResultsPluginSpec extends Specification {

    Project project;

    def setup() {
        project = ProjectBuilder.builder().build();

        project.apply plugin: 'jacoco-results'
        project.apply plugin: 'groovy'
    }

    def cleanup() {
        project.getRootDir().deleteDir()
    }

    def "adds jacocoTestResults task"() {
        project.file("src/main/groovy").mkdirs()
        when:
        project.evaluate();

        then:
        project.tasks.withType(JaCoCoResultsTask).size() == 1
        project.tasks.withType(JaCoCoResultsTask).every { it.description.toString() == "Processes JaCoCo Test results from test and potentially fails the build based on configured thresholds" }
        project.tasks.withType(JaCoCoRootResultsTask).size() == 1
        project.tasks.withType(JaCoCoResultsTask).iterator().next().getDependsOn().size() == 2      // Report Task & Files
        project.tasks.withType(JacocoReport)*.reports.xml.enabled
    }
}
