package org.aon.esolutions.build.tools.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 *
 *
 * @author John Crygier
 */
class JaCoCoRootResultsTask extends DefaultTask {

    @TaskAction
    void aggregateChildren() {
        println "Aggregating"
    }

}
