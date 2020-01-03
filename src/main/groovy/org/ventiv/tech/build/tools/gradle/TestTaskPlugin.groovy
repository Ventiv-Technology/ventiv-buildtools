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

import groovy.util.logging.Slf4j
import org.ventiv.tech.build.tools.test.GradleTestTask
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.tasks.testing.Test

/**
 *
 *
 * @author John Crygier
 */
@Slf4j
class TestTaskPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.withType(JavaPlugin) {
            project.tasks.withType(Test) { Test task ->
                if (task.name == JavaPlugin.TEST_TASK_NAME) {
                    Set<File> allTestSources = getAllTestSources(project);
                    allTestSources.each { File aTestSource ->
                        // Use Groovy to compile this source file (up to the CONVERSION phase - so we don't resolve classes we don't have)
                        GroovyCodeSource groovySource = new GroovyCodeSource(aTestSource, CompilerConfiguration.DEFAULT.getSourceEncoding())
                        CompilationUnit cu = new CompilationUnit(CompilerConfiguration.DEFAULT, groovySource.getCodeSource(), new GroovyClassLoader());
                        cu.addSource(groovySource.getName(), groovySource.getScriptText());
                        cu.compile(Phases.CONVERSION);

                        // Now, look to see if the class in this file has the annotation we're looking for
                        List<AnnotationNode> annotationNodes = cu.getFirstClassNode().getAnnotations();
                        if (annotationNodes) {
                            AnnotationNode gradleTestTaskAnnotation = annotationNodes.find { it.getClassNode().getNameWithoutPackage() == GradleTestTask.class.getSimpleName() }
                            if (gradleTestTaskAnnotation) {
                                String className = cu.getFirstClassNode().getName().replaceAll("\\.", "/");
                                String newTestTaskName = gradleTestTaskAnnotation.getMember("value").getValue()
                                task.exclude(className + '*');

                                // Get the new task
                                Test realTestTask = getTestTaskForName(project, newTestTaskName);
                                realTestTask.include(className + '*')

                                // Change the Reports directories
                                DirectoryReport existingHtmlReport = realTestTask.getReports().getHtml()
                                existingHtmlReport.setDestination(new File(existingHtmlReport.getDestination(), "../tests$newTestTaskName"));

                                DirectoryReport existingXmlReport = realTestTask.getReports().getJunitXml()
                                existingXmlReport.setDestination(new File(existingXmlReport.getDestination(), "../${realTestTask.getName()}-results"));
                            }
                        }
                    }
                }
            }
        }
    }

    private Set<File> getAllTestSources(Project project) {
        Set<File> answer = [];

        Task compileTestJavaTask = project.tasks.getByName(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME);
        if (compileTestJavaTask?.getInputs()?.getSourceFiles()?.getFiles()) {
            answer.addAll(compileTestJavaTask?.getInputs()?.getSourceFiles()?.getFiles());
        }

        Task compileTestGroovyTask = project.tasks.getByName('compileTestGroovy');
        if (compileTestGroovyTask?.getInputs()?.getSourceFiles()?.getFiles()) {
            answer.addAll(compileTestGroovyTask?.getInputs()?.getSourceFiles()?.getFiles());
        }

        return answer;
    }

    private Test getTestTaskForName(Project project, String testName) {
        String fullTaskName = "test$testName";
        Test testTask = project.tasks.findByName(fullTaskName) as Test
        if (testTask == null) {
            testTask = project.tasks.create(fullTaskName, Test);
            testTask.description = "Runs the unit tests that are annotated with @${GradleTestTask.class.getName()}(\"$testName\")";
            testTask.group = "Verification"
        }

        return testTask;
    }

}
