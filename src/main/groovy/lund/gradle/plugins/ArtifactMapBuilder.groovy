package lund.gradle.plugins

import lund.gradle.plugins.asm.SourceSetScanner
import lund.gradle.plugins.model.Artifact
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
    Logger logger = LoggerFactory.getLogger('gradle-logger')

/**
 * Map each of the files declared on all configurations of the project to a collection of the class names they contain.
 * @param project the project we're working on
 * @return a Map of files to their classes
 * @throws IOException
 */
    Set<String> findArtifactClasses(Artifact artifact) throws IOException
    {
        File file = artifact.getAbsoluteFile()
        if (file.name.endsWith('.jar'))
        {
            def scanner  = new SourceSetScanner()
            return scanner.analyzeJar(file.toURI().toURL())
        }
        else
        {
            return null;
        }

    }

    /**
     * Determine which of the project dependencies are used.
     *
     * @param artifactClassMap a map of Files to the classes they contain
     * @param dependencyClasses all classes used directly by the project
     * @return a set of project dependencies confirmed to be used by the project
     */
    void buildUsedArtifacts(Set<Artifact> artifacts, Set<String> dependencyClasses)
    {

        dependencyClasses.each { String className ->
            logger.debug("Checking for classname: *$className*")

            artifacts.each {
                Artifact artifact ->
                    if(artifact.containedClasses.contains(className + ".class")) {
                        logger.info("match for artifact " + artifact.name)
                        artifact.setIsUsed(true)
                    }
            }

        }
    }

    /**
     * Find and analyze all class files to determine which external classes are used.
     * @param project
     * @return a Set of class names
     */
    Collection analyzeClassDependencies(Project project)
    {
        return project.sourceSets*.output.classesDir?.collect {File file ->
            logger.debug("Analyzing: " + file.name)
            def scanner  = new SourceSetScanner()
            scanner.analyze(file.toURI().toURL())
        }?.flatten()?.unique()
    }
}
