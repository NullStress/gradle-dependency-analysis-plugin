package lund.gradle.plugins.tasks

import lund.gradle.plugins.ArtifactMapBuilder
import lund.gradle.plugins.asm.SourceSetScanner
import lund.gradle.plugins.model.Artifact
import org.gradle.api.DefaultTask

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionIdentifier
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
    Set<Artifact> dependencyArtifacts
    Set<Artifact> transitiveArtifacts
    ArtifactMapBuilder artifactMapBuilder

    AnalyzeTask() {
        this.dependencyAnalyzer = new SourceSetScanner()

    }

    @TaskAction
    void analyze() {
        if (!project.plugins.hasPlugin('java')) {
            throw new IllegalStateException("Project does not have the java plugin applied.")
        }

        project.configurations.each {
            if(it.name.toLowerCase().contains("compile")) {
                artifactMapBuilder = new ArtifactMapBuilder()
                project.logger.quiet("Dependencies for configuration " + it.name)
                Set<ResolvedDependency> firstLevelDeps = getFirstLevelDependencies(it)
                Set<ResolvedDependency> transitiveDeps = getTransitiveDependencies(firstLevelDeps)
                dependencyArtifacts = findModuleArtifactFiles(firstLevelDeps)
                transitiveArtifacts = findModuleArtifactFiles(transitiveDeps)
                dependencyArtifacts.each {
                    logger.debug("declared artifact: " + it.name)
                }
                transitiveArtifacts.each {
                    logger.debug("transitive artifact: " + it.name)
                }
                dependencyArtifacts.each {
                    Artifact artifact ->
                        artifact.setContainedClasses(artifactMapBuilder.findArtifactClasses(artifact))
                }
                transitiveArtifacts.each {
                    Artifact artifact ->
                        artifact.setContainedClasses(artifactMapBuilder.findArtifactClasses(artifact))
                }


                Set<String> dependencyClasses = artifactMapBuilder.analyzeClassDependencies(project)
                project.logger.info "dependencyClasses = $dependencyClasses"

                artifactMapBuilder.buildUsedArtifacts(dependencyArtifacts, dependencyClasses)
                Set<Artifact> usedArtifacts = dependencyArtifacts.findAll {
                    Artifact artifact ->
                        artifact.isUsed
                }
                project.logger.quiet "usedArtifacts"
                usedArtifacts.each {project.logger.quiet(it.name)}

                Set<Artifact> unusedArtifacts = dependencyArtifacts.findAll {
                    Artifact artifact ->
                        !artifact.isUsed
                }
                project.logger.quiet "unusedArtifacts"
                unusedArtifacts.each {project.logger.quiet(it.name)}

                artifactMapBuilder.buildUsedArtifacts(transitiveArtifacts, dependencyClasses)
                Set<Artifact> usedTransitiveArtifacts = transitiveArtifacts.findAll {
                    Artifact artifact ->
                        artifact.isUsed
                }
                project.logger.quiet "usedUndeclaredArtifacts"
                usedTransitiveArtifacts.each {project.logger.quiet(it.name)}

                Set<Artifact> unusedTransitiveArtifacts = transitiveArtifacts.findAll {
                    Artifact artifact ->
                        !artifact.isUsed
                }
                project.logger.quiet "unusedUndeclaredArtifacts"
                unusedTransitiveArtifacts.each {project.logger.quiet(it.name)}
            }

        }

    }

    Set<ResolvedDependency> getFirstLevelDependencies(Configuration configuration)
    {
        configuration.resolvedConfiguration.firstLevelModuleDependencies
    }

    Set<ResolvedDependency> getTransitiveDependencies(Set<ResolvedDependency> firstLevelModuleDependencies)
    {
        Set<ResolvedDependency> transitiveDeps = new HashSet<>()
        firstLevelModuleDependencies.each {
            ResolvedDependency resolvedDependency ->
                transitiveDeps.addAll(intersectionOfResolvedDependencySets(transitiveDeps, resolvedDependency.children))
        } as Set<ResolvedDependency>
        return transitiveDeps
    }

    Set<ResolvedDependency> intersectionOfResolvedDependencySets(Set<ResolvedDependency> transitiveCollection ,Set<ResolvedDependency> children) {
        Set<ResolvedDependency> toBeAdded = new HashSet<>()
        Set<String> names =  new HashSet<>()
        transitiveCollection.each{
            names.add(it.name)
        }
        children.each {
            logger.debug("names = $names")
            if(!names.contains(it.name)){
                logger.debug("adding " + it.name)
                toBeAdded.add(it)
                names.add(it.name)
            }
        }
        return toBeAdded
    }

    Set<Artifact> findModuleArtifactFiles(Set<ResolvedDependency> dependencies)
    {
        Set<Artifact> artifactSet = new LinkedHashSet<>()
        dependencies*.moduleArtifacts*.each {
            String classifier = it.getClassifier() ? ":${it.getClassifier()}" : ""
            ModuleVersionIdentifier identifier = it.getModuleVersion().getId()
            artifactSet.add(new Artifact(it.getFile(), identifier.group + ":" + identifier.name + ":" + identifier.version + classifier + "." + it.getExtension()))
        }
        return artifactSet
    }

}
