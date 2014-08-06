package lund.gradle.plugins

import lund.gradle.plugins.asm.SourceSetScanner
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 02.08.14
 * Time: 18:09
 */
class ArtifactMapBuilder {

    SourceSetScanner dependencyAnalyzer
    Logger logger = LoggerFactory.getLogger('gradle-logger')
    Map<File, String> dependencyArtifactsAndFilesMap

    ArtifactMapBuilder(SourceSetScanner dependencyAnalyzer, Map<File, String> dependencyArtifactsAndFilesMap) {
        this.dependencyAnalyzer = dependencyAnalyzer
        this.dependencyArtifactsAndFilesMap = dependencyArtifactsAndFilesMap
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
            if (file.name.endsWith('.jar'))

            {
                artifactClassMap.put(file, dependencyAnalyzer.analyzeJar(file.toURI().toURL()))
            }
            else
            {
                logger.info("Skipping analysis of file for classes: $file")
            }
        }
        return artifactClassMap
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
