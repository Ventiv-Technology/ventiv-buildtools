package org.aon.esolutions.build.tools.gradle

import org.gradle.api.Project

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

    CoverageLevels coverageLevel;
    CoverageTypes coverageType;

    public JaCoCoResultsPluginExtension(Project project) {
        this.project = project;
    }

    public verifyVariables() {
        coverageLevel = CoverageLevels.getValue(aggregationLevel);
        coverageType = CoverageTypes.getValue(type);

        if (coverageLevel == null)
            throw new IllegalArgumentException("jacocoResults.aggregationLevel must be one of: " + CoverageLevels.values() + " but was $coverageLevel");

        if (coverageType == null)
            throw new IllegalArgumentException("jacocoResults.type must be one of: " + CoverageTypes.values() + " but was $coverageType");

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
