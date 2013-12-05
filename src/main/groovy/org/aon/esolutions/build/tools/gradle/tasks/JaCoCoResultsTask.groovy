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
package org.aon.esolutions.build.tools.gradle.tasks

import groovy.transform.ToString
import org.aon.esolutions.build.tools.gradle.JaCoCoResultsPluginExtension
import org.aon.esolutions.build.tools.gradle.JaCoCoResultsPluginExtension.CoverageLevels
import org.aon.esolutions.build.tools.gradle.JaCoCoResultsPluginExtension.CoverageTypes
import org.aon.esolutions.build.tools.gradle.exception.ThresholdMissedException
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

/**
 *
 *
 * @author John Crygier
 */
class JaCoCoResultsTask extends DefaultTask {

    @InputFiles
    List<File> coverageReports;

    JaCoCoResultsPluginExtension extension;
    XmlSlurper xmlSlurper = new XmlSlurper(false, false);

    List<Counter> allCounters = [];

    JaCoCoResultsTask() {
        onlyIf { coverageReports?.every { it.exists() } }

        xmlSlurper.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        xmlSlurper.setFeature("http://xml.org/sax/features/namespaces", false)
    }

    @TaskAction
    void scanCoverageReports() {
        extension.verifyVariables();
        coverageReports.each this.&scanReport

        // If we're at a SubProject or lower level check and throw exception
        if (CoverageLevels.Project != extension.getCoverageLevel()) {
            def allViolatedCounters = allCounters.findAll { it.getPercentCoverage() < extension.getThreshold() && !it.isExcluded(extension.getExcludes()) }
            if (allViolatedCounters) {
                String message = allViolatedCounters.collect {
                    extension.getCoverageLevel().name() + " " + it.getName() +" (" + it.getType()  + " Level): " + it.getPercentCoverage() + "%"
                }.join("\n\t");

                throw new ThresholdMissedException("The following coverage thresholds were not met: \n\t" + message);
            }
        }
    }

    void scanReport(File coverageReportFile) {
        if (coverageReportFile && coverageReportFile.exists()) {
            def coverageReport = xmlSlurper.parseText(coverageReportFile.text);

            if ([CoverageLevels.Project, CoverageLevels.SubProject].contains(extension.getCoverageLevel())) {
                // Add Counter for Project / SubProject
                allCounters.addAll coverageReport.collect(this.&transformNodeToCounter).findAll { it != null }
            } else if (CoverageLevels.Package == extension.getCoverageLevel()) {
                // Add Counters for Package
                allCounters.addAll coverageReport.package.collect(this.&transformNodeToCounter).findAll { it != null }
            } else if (CoverageLevels.Class == extension.getCoverageLevel()) {
                // Add Counters for Class
                allCounters.addAll coverageReport.package."class".collect(this.&transformNodeToCounter).findAll { it != null }
            } else if (CoverageLevels.Method == extension.getCoverageLevel()) {
                // Add Counters for Method
                allCounters.addAll coverageReport.package."class".collect {
                    String namePrefix = it.attributes()['name'] + "."

                    return it.method.collect {
                        this.transformNodeToCounter(it, namePrefix)
                    }.findAll { it != null }
                }.flatten()


            }
        }
    }

    boolean isCounterApplicable(def counterNode) {
        if (counterNode instanceof groovy.util.slurpersupport.Node)
            return counterNode.attributes()['type'] == extension.getCoverageType().name().toUpperCase()
        else
            return counterNode.@type == extension.getCoverageType().name().toUpperCase()
    }

    def transformNodeToCounter(def parentNode, String namePrefix = "") {
        def counter = parentNode.childNodes().find(this.&isCounterApplicable)
        if (counter)
            return new Counter(counter, extension, namePrefix + parentNode.@name.text())
        else
            return null;
    }

    @ToString
    public static final class Counter {
        int covered = 0;
        int missed = 0;
        CoverageTypes type;
        String name;

        public Counter(CoverageTypes type, String name) {
            this.type = type;
            this.name = name;
        }

        public Counter(def counterReportNode, JaCoCoResultsPluginExtension extension, String name) {
            if (counterReportNode instanceof groovy.util.slurpersupport.Node) {
                this.covered = counterReportNode.attributes()['covered'].toInteger()
                this.missed = counterReportNode.attributes()['missed'].toInteger()
            } else {
                this.covered = counterReportNode.@covered.toInteger()
                this.missed = counterReportNode.@missed.toInteger()
            }
            this.type = extension.getCoverageType()
            this.name = name
        }

        public double getPercentCoverage() {
            if (covered + missed == 0)
                return 100;

            return (covered / (covered + missed)) * 100;
        }

        public boolean isExcluded(List<String> excludes) {
            return excludes.find {
                this.getName() =~ it
            } != null;
        }
    }

}
