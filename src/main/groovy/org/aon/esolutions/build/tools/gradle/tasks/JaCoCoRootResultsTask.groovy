package org.aon.esolutions.build.tools.gradle.tasks

import org.aon.esolutions.build.tools.gradle.JaCoCoResultsPluginExtension
import org.aon.esolutions.build.tools.gradle.exception.ThresholdMissedException
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
