package org.aon.esolutions.build.tools.gradle.exception

import org.aon.esolutions.build.tools.gradle.tasks.JaCoCoResultsTask

/**
 *
 *
 * @author John Crygier
 */
class ThresholdMissedException extends RuntimeException {

    public ThresholdMissedException(String message) {
        super(message);
    }

}
