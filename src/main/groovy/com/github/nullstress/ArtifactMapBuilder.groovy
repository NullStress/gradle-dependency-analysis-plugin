package com.github.nullstress

import com.github.nullstress.asm.SourceSetScanner
import com.github.nullstress.model.Artifact
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

    Set<String> findArtifactClasses(Artifact artifact) throws IOException {
        File file = artifact.getAbsoluteFile()
        println "Artifact absolute path: ${file}"
        if (file.name.endsWith('.jar')) {
            def scanner  = new SourceSetScanner()
            return scanner.analyzeJar(file.toURI().toURL())
        } else {
            return null;
        }
    }

    void buildUsedArtifacts(Set<Artifact> artifacts, Set<String> dependencyClasses) {
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

    Collection analyzeClassDependencies(Project project) {
        return project.sourceSets*.output.classesDir?.collect {File file ->
            logger.debug("Analyzing: " + file.name)
            def scanner  = new SourceSetScanner()
            scanner.analyze(file.toURI())
        }?.flatten()?.unique()
    }
}
