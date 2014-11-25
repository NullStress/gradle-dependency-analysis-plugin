package lund.gradle.plugins.tasks

import lund.gradle.plugins.ArtifactMapBuilder
import lund.gradle.plugins.ArtifactResolver
import lund.gradle.plugins.asm.SourceSetScanner
import lund.gradle.plugins.model.Artifact
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.GradleScriptException

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
    ArtifactMapBuilder artifactMapBuilder
    ArtifactResolver artifactResolver

    AnalyzeTask() {
        this.dependencyAnalyzer = new SourceSetScanner()
        this.artifactMapBuilder = new ArtifactMapBuilder()
        this.artifactResolver = new ArtifactResolver(project, artifactMapBuilder)
    }

    @TaskAction
    void analyze() {
        if (!project.plugins.hasPlugin('java')) {
            throw new IllegalStateException("Project does not have the java plugin applied.")
        }

        project.configurations.each {
            if(it.name.toLowerCase().contains("compile")) {
                project.logger.quiet("Dependencies for configuration " + it.name)
                Set<ResolvedDependency> firstLevelDeps = getFirstLevelDependencies(it)
                Set<ResolvedDependency> transitiveDeps = artifactResolver.getTransitiveDependencies(firstLevelDeps)

                Set<Artifact> dependencyArtifacts = findModuleArtifactFiles(firstLevelDeps)
                Set<Artifact> transitiveDependencyArtifacts = findModuleArtifactFiles(transitiveDeps)

                Set<Artifact> usedArtifacts = artifactResolver.resolveArtifacts(dependencyArtifacts, "Declared")
                Set<Artifact> usedTransitiveArtifacts = artifactResolver.resolveArtifacts(transitiveDependencyArtifacts, "Transitive")

                Set<Artifact> unusedArtifacts = artifactResolver.findUnusedArtifact(dependencyArtifacts, "Declared")
                Set<Artifact> unusedTransitiveArtifacts = artifactResolver.findUnusedArtifact(transitiveDependencyArtifacts, "Transitive")

                if(!unusedArtifacts.empty) {
                    throw new GradleException("The project has unused declared artifacts")
                }
            }
        }
    }

    Set<ResolvedDependency> getFirstLevelDependencies(Configuration configuration) {
        configuration.resolvedConfiguration.firstLevelModuleDependencies
    }

    Set<Artifact> findModuleArtifactFiles(Set<ResolvedDependency> dependencies) {
        Set<Artifact> artifactSet = new LinkedHashSet<>()
        dependencies*.moduleArtifacts*.each {
            String classifier = it.getClassifier() ? ":${it.getClassifier()}" : ""
            ModuleVersionIdentifier identifier = it.getModuleVersion().getId()
            artifactSet.add(new Artifact(it.getFile(), identifier.group + ":" + identifier.name + ":" + identifier.version + classifier + "." + it.getExtension()))
        }
        return artifactSet
    }

}
