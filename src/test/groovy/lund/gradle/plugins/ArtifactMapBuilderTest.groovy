package lund.gradle.plugins

import lund.gradle.plugins.asm.SourceSetScanner
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

    @Before
    public void setup() {
        buildSourceSetScannerMock()
    }

    @Test
    public void buildArtifactClassMapShouldOnlyAcceptJarFiles() {
        ArtifactMapBuilder artifactMapBuilder = new ArtifactMapBuilder(scanner,null)

        Set<File> files = new HashSet<>()
        files.add(new File('/fake.jar'))
        files.add(new File('/notajar'))
        Map result = artifactMapBuilder.buildArtifactClassMap(files)

        assertEquals(1,result.size())
    }

    @Test
    public void buildUsedArtifactsShouldReturnArtifactOnMatch() {
        buildArtifactClassMap()
        ArtifactMapBuilder artifactMapBuilder = new ArtifactMapBuilder(scanner,buildDependencyArtifactsAndFilesMap())
        Set<String> result = artifactMapBuilder.buildUsedArtifacts(buildArtifactClassMap(),buildMatchingDependencyClassesSet())
        assertEquals(1, result.size())
        assertEquals("fake.org:fake:1.23", result.toArray()[0])
    }

    @Test
    public void buildUsedArtifactsShouldNotReturnArtifactIfNoMatch() {
        buildArtifactClassMap()
        ArtifactMapBuilder artifactMapBuilder = new ArtifactMapBuilder(scanner,buildDependencyArtifactsAndFilesMap())
        Set<String> result = artifactMapBuilder.buildUsedArtifacts(buildArtifactClassMap(),buildNotMatchingDependencyClassesSet())
        assertEquals(0, result.size())
    }

    private Map<File, String> buildDependencyArtifactsAndFilesMap() {
        Map<File, String> dependencyArtifactsAndFilesMap = new HashMap<>()
        dependencyArtifactsAndFilesMap.put(new File('/fake-1.23.jar'), "fake.org:fake:1.23")
        return dependencyArtifactsAndFilesMap
    }

    private Set<String> buildMatchingDependencyClassesSet() {
        Set<String> dependencySetThatShouldMatch = new HashSet<>()
        dependencySetThatShouldMatch.add("fake.class")
        return dependencySetThatShouldMatch
    }

    private Set<String> buildNotMatchingDependencyClassesSet() {
        Set<String> dependencySetThatShouldNotMatch = new HashSet<>()
        dependencySetThatShouldNotMatch.add("ShouldNotMatch.class")
        return dependencySetThatShouldNotMatch
    }

    private Map<File, Set<String>> buildArtifactClassMap () {
        Map<File, Set<String>> artifactClassMap = new HashMap()
        File testFile = new File('/fake-1.23.jar')
        Set<String> stringSet = new HashSet<>()
        stringSet.add("fake.class")
        stringSet.add("more.fake.class")
        artifactClassMap.put(testFile, stringSet)
        return artifactClassMap
    }

    private void buildSourceSetScannerMock() {
        scanner = mock(SourceSetScanner.class)
        Set<String> mockedStringResponse = new HashSet<>()
        mockedStringResponse.add("Mocked.class")
        when(scanner.analyzeJar(any() as URL)).thenReturn(mockedStringResponse)
    }
}
