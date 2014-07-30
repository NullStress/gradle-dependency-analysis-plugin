package lund.gradle.plugins.tasks

import lund.gradle.plugins.asm.SourceSetScanner
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.result.ResolutionResult
import org.gradle.api.tasks.TaskAction

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 10.07.14
 * Time: 00:00
 */

class AnalyzeTask extends DefaultTask {

    SourceSetScanner dependencyAnalyzer
    Map<File, String> dependencyArtifactsAndFilesMap

    AnalyzeTask() {
        this.dependencyAnalyzer = new SourceSetScanner()
    }

    @TaskAction
    void analyze() {
        if (!project.plugins.hasPlugin('java')) {
            throw new IllegalStateException("Project does not have the java plugin applied.")
        }

        Set<Configuration> gradleConfigurations = project.getConfigurations()
        gradleConfigurations.each {
            ResolutionResult result = it.getIncoming().getResolutionResult()
            println(result.root.id.displayName)
        }

        println("FILES")
        project.configurations.each {
            println(it.name)
        }
        project.configurations.each {
            if(it.name.toLowerCase().contains("compile")) {
                project.logger.quiet("Dependencies for configuration " + it.name)
                Set<ResolvedDependency> firstLevelDeps = getFirstLevelDependencies(it, it.name.toString())
                dependencyArtifactsAndFilesMap = findModuleArtifactFiles(firstLevelDeps)

                Map<File, Set<String>> fileClassMap = buildArtifactClassMap(dependencyArtifactsAndFilesMap.keySet())
                project.logger.info "fileClassMap = $fileClassMap"
//
                Set<String> dependencyClasses = analyzeClassDependencies(project)
                project.logger.info "dependencyClasses = $dependencyClasses"
//
                Set<String> usedArtifacts = buildUsedArtifacts(fileClassMap, dependencyClasses)
                project.logger.info "usedArtifacts = $usedArtifacts"
//
                Set<String> usedDeclaredArtifacts = new LinkedHashSet<String>(dependencyArtifactsAndFilesMap.values().toSet())
                usedDeclaredArtifacts.retainAll(usedArtifacts)
                project.logger.quiet "usedDeclaredArtifacts = $usedDeclaredArtifacts"

                Set<String> usedUndeclaredArtifacts = new LinkedHashSet<String>(usedArtifacts)
                usedUndeclaredArtifacts.removeAll(dependencyArtifactsAndFilesMap.values())
                project.logger.quiet "usedUndeclaredArtifacts = $usedUndeclaredArtifacts"

                Set<String> unusedDeclaredArtifacts = new LinkedHashSet<String>(dependencyArtifactsAndFilesMap.values())
                unusedDeclaredArtifacts.removeAll(usedArtifacts)
                project.logger.quiet "unusedDeclaredArtifacts = $unusedDeclaredArtifacts"
            }

        }

    }

    Set<ResolvedDependency> getFirstLevelDependencies(Configuration configuration, String configurationName)
    {
        configuration.resolvedConfiguration.firstLevelModuleDependencies
    }

    /**
     * Map each of the files declared on all configurations of the project to a collection of the class names they contain.
     * @param project the project we're working on
     * @return a Map of files to their classes
     * @throws IOException
     */
    Map<File, Set<String>> buildArtifactClassMap(Set<File> dependencyArtifacts) throws IOException
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

    Map<File, String> findModuleArtifactFiles(Set<ResolvedDependency> dependencies)
    {
        Map<File, String> artifactAndFileMap = new HashMap<>()
        dependencies*.moduleArtifacts*.each {
            String classifier = it.getClassifier() ? ":${it.getClassifier()}" : ""
            ModuleVersionIdentifier identifier = it.getModuleVersion().getId()
            artifactAndFileMap.put(it.getFile(), identifier.group + ":" + identifier.name + ":" + identifier.version + classifier + "." + it.getExtension())
        }
        return artifactAndFileMap
    }

    /**
     * Determine which of the project dependencies are used.
     *
     * @param artifactClassMap a map of Files to the classes they contain
     * @param dependencyClasses all classes used directly by the project
     * @return a set of project dependencies confirmed to be used by the project
     */
    Set<String> buildUsedArtifacts(Map<File, Set<String>> artifactClassMap, Set<String> dependencyClasses)
    {
        Set<String> usedArtifacts = new HashSet()

        dependencyClasses.each { String className ->
            File artifact = artifactClassMap.find {it.value.contains(className)}?.key
            if (artifact)
            {
                usedArtifacts << dependencyArtifactsAndFilesMap.get(artifact)
            }
        }
        return usedArtifacts
    }

    /**
     * Find and analyze all class files to determine which external classes are used.
     * @param project
     * @return a Set of class names
     */
    Collection analyzeClassDependencies(Project project)
    {
        return project.sourceSets*.output.classesDir?.collect {File file ->
            println("Analyzing: " + file.name)
            dependencyAnalyzer.analyze(file.toURI().toURL())
        }?.flatten()?.unique()
    }
}
