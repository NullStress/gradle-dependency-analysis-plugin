package lund.gradle.plugins

import lund.gradle.plugins.model.Artifact
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 22.11.14
 * Time: 22:52
 */
class ArtifactResolver {

    Project project
    ArtifactMapBuilder artifactMapBuilder

    ArtifactResolver(Project project, ArtifactMapBuilder artifactMapBuilder) {
        this.project = project
        this.artifactMapBuilder = artifactMapBuilder
    }

    Set<Artifact> findUnusedArtifact(Set<Artifact> artifacts, String typeOfArtifactSet) {
        Set<Artifact> unusedArtifacts = artifacts.findAll {
            Artifact artifact ->
                !artifact.isUsed
        }
        project.logger.quiet "unused$typeOfArtifactSet Artifacts"
        unusedArtifacts.each {project.logger.quiet(it.name)}
        return unusedArtifacts
    }

    Set<Artifact> resolveArtifacts(Set<Artifact>  dependencyArtifacts, String typeOfArtifactSet) {
        dependencyArtifacts.each {
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
        project.logger.quiet "used$typeOfArtifactSet Artifacts"
        usedArtifacts.each {project.logger.quiet(it.name)}

        return usedArtifacts
    }

    Set<ResolvedDependency> getTransitiveDependencies(Set<ResolvedDependency> firstLevelModuleDependencies) {
        Set<ResolvedDependency> transitiveDeps = new HashSet<>()
        firstLevelModuleDependencies.each {
            ResolvedDependency resolvedDependency ->
                transitiveDeps.addAll(unionMinusIntersectionOfResolvedDependencySets(transitiveDeps, resolvedDependency.children))
        } as Set<ResolvedDependency>
        return transitiveDeps
    }

    Set<ResolvedDependency> unionMinusIntersectionOfResolvedDependencySets(Set<ResolvedDependency> transitiveCollection, Set<ResolvedDependency> children) {
        Set<ResolvedDependency> toBeAdded = new HashSet<>()
        Set<String> names =  new HashSet<>()
        transitiveCollection.each {
            names.add(it.name)
        }
        children.each {
            project.logger.quiet("names = $names")
            if(!names.contains(it.name)) {
                project.logger.quiet("adding " + it.name)
                toBeAdded.add(it)
                names.add(it.name)
            }
        }
        return toBeAdded
    }

}
