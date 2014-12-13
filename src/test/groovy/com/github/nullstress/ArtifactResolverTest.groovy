package com.github.nullstress

import com.github.nullstress.model.Artifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import org.gradle.api.internal.artifacts.DefaultResolvedDependency
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;



/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 22.11.14
 * Time: 23:28
 */
class ArtifactResolverTest {

    Set<Artifact> artifacts = new HashSet<>()
    ArtifactResolver artifactResolver
    Set<ResolvedDependency> transitiveDeps
    Set<ResolvedDependency> children

    @Before
    public void setup() {
        Artifact usedArtifact = new Artifact(new File(".\testtrue"), "usedArtifact")
        usedArtifact.setIsUsed(true)
        Artifact notUsedArtifact = new Artifact(new File(".\testtrue"), "unusedArtifact")
        notUsedArtifact.setIsUsed(false)

        artifacts.add(usedArtifact)
        artifacts.add(notUsedArtifact)

        ArtifactMapBuilder artifactMapBuilder = mock(ArtifactMapBuilder.class)
        when(artifactMapBuilder.findArtifactClasses(any(Artifact.class))).thenReturn({""} as Set<String>)

        ResolvedDependency resolvedDependency = new DefaultResolvedDependency(new DefaultModuleVersionIdentifier("test","test","test"), "Compile")
        ResolvedDependency resolvedDependency2 = new DefaultResolvedDependency(new DefaultModuleVersionIdentifier("test2","test2","test2"), "Compile")
        ResolvedDependency resolvedDependency3 = new DefaultResolvedDependency(new DefaultModuleVersionIdentifier("test3","test3","test3"), "Compile")
        ResolvedDependency resolvedDependency4 = new DefaultResolvedDependency(new DefaultModuleVersionIdentifier("test3","test3","test3"), "Compile")

        transitiveDeps = new HashSet<>()
        transitiveDeps.add(resolvedDependency3)

        children = new HashSet<>()
        children.add(resolvedDependency)
        children.add(resolvedDependency2)
        children.add(resolvedDependency4)

//        when(artifactMapBuilder.buildUsedArtifacts()).thenReturn()

        artifactResolver = new ArtifactResolver(ProjectBuilder.builder().build(), artifactMapBuilder)
    }

    @Test
    public void artifactResolverShouldFindUnusedArtifacts() {
        Set<Artifact> unusedArtifacts = artifactResolver.findUnusedArtifact(artifacts, "Compile")
        assertEquals(1, unusedArtifacts.size())
        Artifact artifact = unusedArtifacts.iterator()[0]
        assertEquals("unusedArtifact", artifact.name)
    }

    @Test
    public void artifactResolverShouldFindUsedArtifacts() {
        Set<Artifact> usedArtifacts = artifactResolver.resolveArtifacts(artifacts, "Compile")
        assertEquals(1, usedArtifacts.size())
        Artifact artifact = usedArtifacts.iterator()[0]
        assertEquals("usedArtifact", artifact.name)
    }

    @Test
    public void intersectionOfTwoSetsShouldBeFound() {
        Set<ResolvedDependency> intersection = artifactResolver.unionMinusIntersectionOfResolvedDependencySets(transitiveDeps, children)
        assertEquals(2, intersection.size())
    }
}
