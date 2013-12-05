package pluginb

import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginB implements Plugin<Project> {

    void apply(Project project) {}

    void anotherMethodThatIsntCovered() {
        println "hi"
        println "hi"
        println "hi"
        println "hi"
        println "hi"
        println "hi"
        println "hi"
        println "hi"
    }

}
