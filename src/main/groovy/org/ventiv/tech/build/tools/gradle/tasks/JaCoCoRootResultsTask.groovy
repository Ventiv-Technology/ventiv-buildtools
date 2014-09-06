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
package org.ventiv.tech.build.tools.gradle.tasks

import org.ventiv.tech.build.tools.gradle.JaCoCoResultsPluginExtension
import org.ventiv.tech.build.tools.gradle.exception.ThresholdMissedException
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 *
 *
 * @author John Crygier
 */
class JaCoCoRootResultsTask extends DefaultTask {

    @TaskAction
    void aggregateChildren() {
        // Get the extension at the root level project
        JaCoCoResultsPluginExtension extension = getProject().getTasks().withType(JaCoCoResultsTask)?.iterator()?.next()?.extension;
        extension.verifyVariables();

        // Create an 'overall' counter to aggregate all sub-projects
        JaCoCoResultsTask.Counter overallCounter = new JaCoCoResultsTask.Counter(extension.getCoverageType(), "All Projects");
        double threshold = extension?.getThreshold();

        // Loop over all projects - getting their 'allCounters'
        getProject().getAllprojects().each { Project project ->
            project.tasks.withType(JaCoCoResultsTask) { JaCoCoResultsTask task ->
                task.extension.verifyVariables();

                if (task.extension.getCoverageLevel() == JaCoCoResultsPluginExtension.CoverageLevels.Project) {
                     task.allCounters.each {
                        overallCounter.covered += it.covered
                        overallCounter.missed += it.missed
                    }
                }
            }
        }

        // Verify coverage
        if (overallCounter.getPercentCoverage() < threshold) {
            throw new ThresholdMissedException("The coverage threshold was not met for all projects overall.  ${overallCounter.getType()} coverage was ${overallCounter.getPercentCoverage()}%");
        }
    }

}
