package org.aon.esolutions.build.tools.gradle

import org.aon.esolutions.build.tools.gradle.tasks.JaCoCoResultsTask
import org.aon.esolutions.build.tools.gradle.tasks.JaCoCoRootResultsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 *
 *
 * @author John Crygier
 */
class JaCoCoResultsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply(JacocoPlugin);
        JaCoCoResultsPluginExtension extension = project.extensions.create("jacocoResults", JaCoCoResultsPluginExtension, project)

        // Create the root level task
        TaskCollection<JaCoCoRootResultsTask> existingTasks = project.getRootProject().tasks.withType(JaCoCoRootResultsTask)
        if (existingTasks.size() == 0) {
            JaCoCoRootResultsTask rootResultsTask = project.getRootProject().tasks.create("jacocoResultsAggregation", JaCoCoRootResultsTask)
            project.gradle.startParameter.taskNames << "jacocoResultsAggregation"       // Lie to the command line - pretend like they asked for this task (last)
        }

        project.plugins.withType(JavaPlugin) {
            project.tasks.withType(Test) { task ->
                if (task.name == JavaPlugin.TEST_TASK_NAME) {
                    JaCoCoResultsTask resultsTask = project.tasks.create("jacoco${task.name.capitalize()}Results", JaCoCoResultsTask)
                    resultsTask.description = "Processes JaCoCo Test results from ${task.name} and potentially fails the build based on configured thresholds"
                    resultsTask.extension = extension;

                    TaskCollection<JacocoReport> reportTasks = project.tasks.withType(JacocoReport)
                    resultsTask.dependsOn reportTasks

                    // Enable and get the XML report
                    resultsTask.coverageReports = reportTasks.collect { JacocoReport reportTask ->
                        reportTask.reports.xml.enabled = true;
                        new File(reportTask.reports.xml.destination.toString())
                    }
                }
            }
        }
    }
}
