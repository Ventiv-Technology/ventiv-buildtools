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

import org.ventiv.tech.build.tools.gradle.JaCoCoResultsPluginExtension.CoverageLevels
import org.ventiv.tech.build.tools.gradle.JaCoCoResultsPluginExtension.CoverageTypes
import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 *
 * @author John Crygier
 */
class JaCoCoResultsPluginExtensionSpec extends Specification {

    @Unroll("Verify AggregationLevel(#aggregationLevel) with Type(#type) Does not Throw Exception")
    def "verify positive testing"(String aggregationLevel, String type, CoverageLevels expectedLevel, CoverageTypes expectedType) {
        when:
        JaCoCoResultsPluginExtension extension = new JaCoCoResultsPluginExtension(null);
        extension.aggregationLevel = aggregationLevel
        extension.type = type
        extension.verifyVariables()

        then:
        notThrown(Throwable)
        extension.coverageLevel == expectedLevel
        extension.coverageType == expectedType

        where:
        aggregationLevel | type          | expectedLevel             | expectedType
        "Project"        | "Class"       | CoverageLevels.Project    | CoverageTypes.Class
        "SubProject"     | "Class"       | CoverageLevels.SubProject | CoverageTypes.Class
        "Package"        | "Class"       | CoverageLevels.Package    | CoverageTypes.Class

        "Project"        | "Method"      | CoverageLevels.Project    | CoverageTypes.Method
        "SubProject"     | "Method"      | CoverageLevels.SubProject | CoverageTypes.Method
        "Package"        | "Method"      | CoverageLevels.Package    | CoverageTypes.Method
        "Class"          | "Method"      | CoverageLevels.Class      | CoverageTypes.Method

        "Project"        | "Complexity"  | CoverageLevels.Project    | CoverageTypes.Complexity
        "SubProject"     | "Complexity"  | CoverageLevels.SubProject | CoverageTypes.Complexity
        "Package"        | "Complexity"  | CoverageLevels.Package    | CoverageTypes.Complexity
        "Class"          | "Complexity"  | CoverageLevels.Class      | CoverageTypes.Complexity
        "Method"         | "Complexity"  | CoverageLevels.Method     | CoverageTypes.Complexity

        "Project"        | "Line"        | CoverageLevels.Project    | CoverageTypes.Line
        "SubProject"     | "Line"        | CoverageLevels.SubProject | CoverageTypes.Line
        "Package"        | "Line"        | CoverageLevels.Package    | CoverageTypes.Line
        "Class"          | "Line"        | CoverageLevels.Class      | CoverageTypes.Line
        "Method"         | "Line"        | CoverageLevels.Method     | CoverageTypes.Line

        "Project"        | "Branch"      | CoverageLevels.Project    | CoverageTypes.Branch
        "SubProject"     | "Branch"      | CoverageLevels.SubProject | CoverageTypes.Branch
        "Package"        | "Branch"      | CoverageLevels.Package    | CoverageTypes.Branch
        "Class"          | "Branch"      | CoverageLevels.Class      | CoverageTypes.Branch
        "Method"         | "Branch"      | CoverageLevels.Method     | CoverageTypes.Branch

        "Project"        | "Instruction" | CoverageLevels.Project    | CoverageTypes.Instruction
        "SubProject"     | "Instruction" | CoverageLevels.SubProject | CoverageTypes.Instruction
        "Package"        | "Instruction" | CoverageLevels.Package    | CoverageTypes.Instruction
        "Class"          | "Instruction" | CoverageLevels.Class      | CoverageTypes.Instruction
        "Method"         | "Instruction" | CoverageLevels.Method     | CoverageTypes.Instruction

        // Verify case insensitive
        "MeThOd" | "InStruCtion" | CoverageLevels.Method | CoverageTypes.Instruction

    }

    @Unroll("Verify AggregationLevel(#aggregationLevel) with Type(#type) Throws #expectedException")
    def "verify negative testing"(String aggregationLevel, String type, Class<Throwable> expectedException, String expectedMessage) {
        when:
        JaCoCoResultsPluginExtension extension = new JaCoCoResultsPluginExtension(null);
        extension.aggregationLevel = aggregationLevel
        extension.type = type
        extension.verifyVariables()

        then:
        def e = thrown(expectedException)
        e.message == expectedMessage

        where:
        aggregationLevel | type       | expectedException        | expectedMessage
        "Class"          | "Class"    | IllegalArgumentException | "jacocoResults.type must be one of: [Method, Complexity, Line, Branch, Instruction] if aggregationLevel is Class"
        "Method"         | "Class"    | IllegalArgumentException | "jacocoResults.type must be one of: [Complexity, Line, Branch, Instruction] if aggregationLevel is Method"
        "Method"         | "Method"   | IllegalArgumentException | "jacocoResults.type must be one of: [Complexity, Line, Branch, Instruction] if aggregationLevel is Method"
        "Invalid"        | "Method"   | IllegalArgumentException | "jacocoResults.aggregationLevel must be one of: [Project, SubProject, Package, Class, Method] but was Invalid"
        "Method"         | "Invalid2" | IllegalArgumentException | "jacocoResults.type must be one of: [Class, Method, Complexity, Line, Branch, Instruction] but was Invalid2"
    }
}
