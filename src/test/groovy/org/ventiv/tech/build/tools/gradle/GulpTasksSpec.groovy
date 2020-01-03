/*
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
package org.ventiv.tech.build.tools.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.ventiv.tech.build.tools.exec.Executable
import org.ventiv.tech.build.tools.gradle.tasks.GulpTask
import spock.lang.Specification

class GulpTasksSpec extends Specification {
    Executable gradle

    Executable gradlew() {
        return new Executable(new File('gradlew'))
    }

    def setup() {
        gradle = gradlew()
    }


    def "gulp doesn't get installed if not running gulp tasks"() {
        setup:
        new File('node_modules/gulp').deleteDir()

        when:
        gradle.run([ '-b', 'gulpTest__emptyExecPluginBuildScript.gradle', 'noop' ])

        then:
        !gradle.standardOut.contains("Installing gulp")
        !new File(GulpTask.GULP_PATH).exists()
    }

    def "installGulp task is present"() {
        setup:
        Project project = ProjectBuilder.builder().build();
        project.apply plugin: 'exec'
        project.apply plugin: 'groovy'

        when:
        project.evaluate()

        then:
        project.tasks.findAll { it.name == 'installGulp' }.size() == 1

        cleanup:
        project.getRootDir().deleteDir()
    }

    def "installGulp task works"() {
        setup:
        new File('node_modules/gulp').deleteDir()


        when:
        gradle.run(['-b', 'gulpTest__emptyExecPluginBuildScript.gradle', 'installGulp'])

        then:
        gradle.standardOut.contains("Installing gulp")
        new File(GulpTask.GULP_PATH).exists()
    }

    def "gulp tasks install gulp if needed and gulp doesn't reinstall if present"() {
        setup:
        new File('node_modules/gulp').deleteDir()


        when:
        gradle.run([ '-b', 'gulpTest__emptyExecPluginBuildScript.gradle', 'gulp_build' ])

        then:
        gradle.standardOut.contains("Installing gulp")
        new File(GulpTask.GULP_PATH).exists()


        when:
        gradle = gradlew()
        gradle.run([ '-b', 'gulpTest__emptyExecPluginBuildScript.gradle', 'gulp_build' ])

        then:
        !gradle.standardOut.contains("Installing gulp")
        new File(GulpTask.GULP_PATH).exists()
    }

    def "gulp tasks run"() {
        when:
        gradle.run([ '-b', 'gulpTest__emptyExecPluginBuildScript.gradle', 'gulp_build', 'gulp_task2' ])

        then:
        gradle.standardOut.contains('running gulp')
        gradle.standardOut.contains("Starting 'task2'")
    }
}
