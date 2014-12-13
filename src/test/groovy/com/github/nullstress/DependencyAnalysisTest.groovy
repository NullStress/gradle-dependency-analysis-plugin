package com.github.nullstress

import com.github.nullstress.asm.SourceSetScanner
import com.github.nullstress.tasks.AnalyzeTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import static org.junit.Assert.*

class DependencyAnalysisTest {

    @Test
    public void dependencyAnalysisPluginAddsAnalyzeTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'java'
        project.apply plugin: 'dependencyAnalysis'

        assertTrue(project.tasks.analyze instanceof AnalyzeTask)
    }

    @Test
    public void dependencyAnalysisPluginShouldLetJavaPluginBeAddedAfterDAPlugin() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'dependencyAnalysis'
        project.apply plugin: 'java'

        assertTrue(project.tasks.analyze instanceof AnalyzeTask)
    }

    @Test
    public void analyzeTaskShouldWork() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'dependencyAnalysis'
        project.apply plugin: 'java'

        Task analyze = project.tasks.analyze as AnalyzeTask
        assertTrue(analyze.dependencyAnalyzer instanceof SourceSetScanner)
    }

    @Test
    public void shouldWorkWithScalaPluginApplied() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'dependencyAnalysis'
        project.apply plugin: 'scala'

        assertTrue(project.tasks.analyze instanceof AnalyzeTask)
    }
}