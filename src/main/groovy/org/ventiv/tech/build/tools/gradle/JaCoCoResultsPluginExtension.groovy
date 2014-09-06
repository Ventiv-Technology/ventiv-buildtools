/**
 * Copyright (c) 2014 Ventiv Technology
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
import org.gradle.api.logging.LogLevel

/**
 *
 *
 * @author John Crygier
 */
class JaCoCoResultsPluginExtension {

    private Project project;

    String aggregationLevel = CoverageLevels.SubProject.name();
    String type = CoverageTypes.Line.name();
    double threshold = 80.0;
    String logResultsLevel = "DEBUG";
    List<String> excludes = [];

    CoverageLevels coverageLevel;
    CoverageTypes coverageType;

    public JaCoCoResultsPluginExtension(Project project) {
        this.project = project;
    }

    public LogLevel getResultsLogLevel() {
        return LogLevel.valueOf(logResultsLevel);
    }

    public verifyVariables() {
        coverageLevel = CoverageLevels.getValue(aggregationLevel);
        coverageType = CoverageTypes.getValue(type);

        if (coverageLevel == null)
            throw new IllegalArgumentException("jacocoResults.aggregationLevel must be one of: " + CoverageLevels.values() + " but was $aggregationLevel");

        if (coverageType == null)
            throw new IllegalArgumentException("jacocoResults.type must be one of: " + CoverageTypes.values() + " but was $type");

        if (!coverageLevel.isCoverageTypePermitted(coverageType))
            throw new IllegalArgumentException("jacocoResults.type must be one of: " + coverageLevel.getPermittedCoverageTypes() + " if aggregationLevel is $coverageLevel");
    }

    public static enum CoverageLevels {
        Project         ([CoverageTypes.Class, CoverageTypes.Method, CoverageTypes.Complexity, CoverageTypes.Line, CoverageTypes.Branch, CoverageTypes.Instruction]),
        SubProject      ([CoverageTypes.Class, CoverageTypes.Method, CoverageTypes.Complexity, CoverageTypes.Line, CoverageTypes.Branch, CoverageTypes.Instruction]),
        Package         ([CoverageTypes.Class, CoverageTypes.Method, CoverageTypes.Complexity, CoverageTypes.Line, CoverageTypes.Branch, CoverageTypes.Instruction]),
        Class           ([CoverageTypes.Method, CoverageTypes.Complexity, CoverageTypes.Line, CoverageTypes.Branch, CoverageTypes.Instruction]),
        Method          ([CoverageTypes.Complexity, CoverageTypes.Line, CoverageTypes.Branch, CoverageTypes.Instruction]);

        List<CoverageTypes> permittedCoverageTypes
        private CoverageLevels(List<CoverageTypes> permittedCoverageTypes) {
            this.permittedCoverageTypes = permittedCoverageTypes;
        }

        boolean isCoverageTypePermitted(CoverageTypes coverageType) {
            return permittedCoverageTypes.contains(coverageType);
        }

        public static CoverageLevels getValue(String value) {
            return values().find { it.name().equalsIgnoreCase(value) }
        }
    }

    public static enum CoverageTypes {
        Class, Method, Complexity, Line, Branch, Instruction

        public static CoverageTypes getValue(String value) {
            return values().find { it.name().equalsIgnoreCase(value) }
        }
    }
}
