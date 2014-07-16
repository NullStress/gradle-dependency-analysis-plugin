package lund.gradle.plugins

import lund.gradle.plugins.tasks.AnalyzeTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 10.07.14
 * Time: 00:18
 */
class DependencyAnalysisPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.tasks.create("analyze", AnalyzeTask)

    }
}