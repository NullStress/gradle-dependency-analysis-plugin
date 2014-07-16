package lund.gradle.plugins.tasks


import lund.gradle.plugins.asm.SourceSetScanner
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.TaskAction

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 10.07.14
 * Time: 00:00
 */

class AnalyzeTask extends DefaultTask {

    SourceSetScanner dependencyAnalyzer

    AnalyzeTask() {
        this.dependencyAnalyzer = new SourceSetScanner()
    }

    @TaskAction
    void analyze() {
        if (!project.plugins.hasPlugin('java')) {
            throw new IllegalStateException("Project does not have the java plugin applied.")
        }

        analyzeClassDependencies(project).each {
            println(it)
        }


        Set<ResolvedDependency> firstLevelDeps = getFirstLevelDependencies(project, 'compile')
        Set<File> dependencyArtifacts = findModuleArtifactFiles(firstLevelDeps)

        Map<File, Set<String>> fileClassMap = buildArtifactClassMap(dependencyArtifacts)
        project.logger.info "fileClassMap = $fileClassMap"

        Set<String> dependencyClasses = analyzeClassDependencies(project)
        project.logger.info "dependencyClasses = $dependencyClasses"

        Set<File> usedArtifacts = buildUsedArtifacts(fileClassMap, dependencyClasses)
        project.logger.info "usedArtifacts = $usedArtifacts"

        Set<File> usedDeclaredArtifacts = new LinkedHashSet<File>(dependencyArtifacts)
        usedDeclaredArtifacts.retainAll(usedArtifacts)
        project.logger.info "usedDeclaredArtifacts = $usedDeclaredArtifacts"

        Set<File> usedUndeclaredArtifacts = new LinkedHashSet<File>(usedArtifacts)
        usedUndeclaredArtifacts.removeAll(dependencyArtifacts)
        project.logger.info "usedUndeclaredArtifacts = $usedUndeclaredArtifacts"

        Set<File> unusedDeclaredArtifacts = new LinkedHashSet<File>(dependencyArtifacts)
        unusedDeclaredArtifacts.removeAll(usedArtifacts)
        project.logger.info "unusedDeclaredArtifacts = $unusedDeclaredArtifacts"

        //Now work back from the files to the artifact information
        ConfigurationContainer configurations = project.configurations
        def nonTestConfigurations = configurations.findAll {!it.name.contains('test')}
        Set<ResolvedArtifact> artifacts = nonTestConfigurations*.resolvedConfiguration*.getResolvedArtifacts().unique().flatten() as Set<ResolvedArtifact>
//        ProjectDependencyAnalysis projectDependencyAnalysis = new ProjectDependencyAnalysis(
//                artifacts.findAll {ResolvedArtifact artifact -> artifact.file in usedDeclaredArtifacts}.unique {it.file} as Set,
//                artifacts.findAll {ResolvedArtifact artifact -> artifact.file in usedUndeclaredArtifacts}.unique {it.file} as Set,
//                artifacts.findAll {ResolvedArtifact artifact -> artifact.file in unusedDeclaredArtifacts}.unique {it.file} as Set)

    }

    private Set<ResolvedDependency> getFirstLevelDependencies(Project project, String configurationName)
    {
        project.configurations."$configurationName".resolvedConfiguration.firstLevelModuleDependencies
    }

    /**
     * Map each of the files declared on all configurations of the project to a collection of the class names they contain.
     * @param project the project we're working on
     * @return a Map of files to their classes
     * @throws IOException
     */
    private Map<File, Set<String>> buildArtifactClassMap(Set<File> dependencyArtifacts) throws IOException
    {
        Map<File, Set<String>> artifactClassMap = [:]

        dependencyArtifacts.each { File file ->
            if (file.name.endsWith('jar'))
            {
                artifactClassMap.put(file, dependencyAnalyzer.analyzeJar(file.toURI().toURL()))
            }
            else
            {
                project.logger.info "Skipping analysis of file for classes: $file"
            }
        }
        return artifactClassMap
    }

    private Set<File> findModuleArtifactFiles(Set<ResolvedDependency> dependencies)
    {
        dependencies*.moduleArtifacts*.collect {it.file}.unique().flatten()
    }

    /**
     * Determine which of the project dependencies are used.
     *
     * @param artifactClassMap a map of Files to the classes they contain
     * @param dependencyClasses all classes used directly by the project
     * @return a set of project dependencies confirmed to be used by the project
     */
    private Set<File> buildUsedArtifacts(Map<File, Set<String>> artifactClassMap, Set<String> dependencyClasses)
    {
        Set<File> usedArtifacts = new HashSet()

        dependencyClasses.each { String className ->
            File artifact = artifactClassMap.find {it.value.contains(className)}?.key
            if (artifact)
            {
                usedArtifacts << artifact
            }
        }
        return usedArtifacts
    }

    /**
     * Find and analyze all class files to determine which external classes are used.
     * @param project
     * @return a Set of class names
     */
    private Collection analyzeClassDependencies(Project project)
    {
        return project.sourceSets*.output.classesDir?.collect {File file ->
            println("Analyzing: " + file.name)
            dependencyAnalyzer.analyze(file.toURI().toURL())
        }?.flatten()?.unique()
    }
}
