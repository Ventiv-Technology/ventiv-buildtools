package org.aon.esolutions.build.tools.gradle.tasks

import groovy.transform.ToString
import org.aon.esolutions.build.tools.gradle.JaCoCoResultsPluginExtension
import org.aon.esolutions.build.tools.gradle.JaCoCoResultsPluginExtension.CoverageLevels
import org.aon.esolutions.build.tools.gradle.JaCoCoResultsPluginExtension.CoverageTypes
import org.codehaus.groovy.runtime.NullObject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildActionFailureException

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
        println "Scanning $coverageReports";
        extension.verifyVariables();
        coverageReports.each this.&scanReport

        println allCounters;
    }

    void scanReport(File coverageReportFile) {
        if (coverageReportFile && coverageReportFile.exists()) {
            def coverageReport = xmlSlurper.parseText(coverageReportFile.text);
            def coverageFindClosure = this.&isCounterApplicable

            if ([CoverageLevels.Project, CoverageLevels.SubProject].contains(extension.getCoverageLevel())) {
                // Add Counter for Project / SubProject
                def counter = coverageReport.counter.find this.&isCounterApplicable
                allCounters << new Counter(counter, extension, "Project ${getProject().getName()}")
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
            return (covered / (covered + missed)) * 100;
        }
    }

}
