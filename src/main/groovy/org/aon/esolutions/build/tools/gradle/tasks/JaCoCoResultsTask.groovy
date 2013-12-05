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
            def allViolatedCounters = allCounters.findAll { it.getPercentCoverage() < extension.getThreshold() }
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
                allCounters.addAll coverageReport.package."class".method.collect(this.&transformNodeToCounter).findAll { it != null }
            }
        }
    }

    boolean isCounterApplicable(def counterNode) {
        if (counterNode instanceof groovy.util.slurpersupport.Node)
            return counterNode.attributes()['type'] == extension.getCoverageType().name().toUpperCase()
        else
            return counterNode.@type == extension.getCoverageType().name().toUpperCase()
    }

    def transformNodeToCounter(def parentNode) {
        def counter = parentNode.childNodes().find(this.&isCounterApplicable)
        if (counter)
            return new Counter(counter, extension, parentNode.@name.text())
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
    }

}
