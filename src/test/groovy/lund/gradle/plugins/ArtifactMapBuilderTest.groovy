package lund.gradle.plugins

import lund.gradle.plugins.asm.SourceSetScanner
import lund.gradle.plugins.model.Artifact
import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 02.08.14
 * Time: 18:11
 */
class ArtifactMapBuilderTest {

    SourceSetScanner scanner

    Set<String> dependencyClasses
    File file = new File('src/test/resources/commons-cli-1.0.jar')
    Artifact commonsArtifact = new Artifact(file, "commons-cli:commons-cli:1.0")

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {
        scanner = null
    }

    @Test
    public void findArtifactClassesShouldReturn0ForEmptyJar() {
        ArtifactMapBuilder artifactMapBuilder = new ArtifactMapBuilder()

        Artifact artifact = new Artifact(new File('/fake-1.23.jar'), "fake.org:fake:1.23")

        Set<String> result = artifactMapBuilder.findArtifactClasses(artifact)

        assertEquals(0,result.size())
    }

    @Test
    public void findArtifactClassesShouldReturn12CommonsCli() {
        ArtifactMapBuilder artifactMapBuilder = new ArtifactMapBuilder()
        Set<String> result = artifactMapBuilder.findArtifactClasses(commonsArtifact)

        assertEquals(20,result.size())
    }

    @Test
    public void buildUsedArtifactsShouldReturnArtifactOnMatch() {
        Artifact artifact = new Artifact(new File('/fake-1.23.jar'), "fake.org:fake:1.23")
        artifact.setContainedClasses(buildMatchingClassSet())
        Set<Artifact> artifacts = new HashSet<>()
        artifacts.add(artifact)

        ArtifactMapBuilder artifactMapBuilder = new ArtifactMapBuilder()
        artifactMapBuilder.buildUsedArtifacts(artifacts, buildMatchingDependencyClassesSet())
        assertTrue(artifact.isUsed)
    }

    @Test
    public void buildUsedArtifactsShouldNotReturnArtifactIfNoMatch() {
        Artifact artifact = new Artifact(new File('/fake-1.23.jar'), "fake.org:fake:1.23")
        Set<String> stringSet = new HashSet<>()
        stringSet.add("fake.class")
        artifact.setContainedClasses(stringSet)
        Set<Artifact> artifacts = new HashSet<>()
        artifacts.add(artifact)

        ArtifactMapBuilder artifactMapBuilder = new ArtifactMapBuilder()
        artifactMapBuilder.buildUsedArtifacts(artifacts, buildNotMatchingDependencyClassesSet())
        assertFalse(artifact.isUsed)
    }



    private Set<String> buildMatchingDependencyClassesSet() {
        Set<String> dependencySetThatShouldMatch = new HashSet<>()
        dependencySetThatShouldMatch.add("fake")
        return dependencySetThatShouldMatch
    }

    private Set<String> buildNotMatchingDependencyClassesSet() {
        Set<String> dependencySetThatShouldNotMatch = new HashSet<>()
        dependencySetThatShouldNotMatch.add("ShouldNotMatch")
        return dependencySetThatShouldNotMatch
    }

    private Set<String> buildMatchingClassSet() {
        Set<String> stringSet = new HashSet<>()
        stringSet.add("fake.class")
        stringSet.add("more.fake.class")
        return stringSet
    }

}
