package com.github.nullstress.asm;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Set;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 13.07.14
 * Time: 18:21
 */
public class SourceSetScannerTest {

    @Test
    public void sourceSetScannerShouldAddScanResultsToDependencies() throws IOException {
        SourceSetScanner sourceSetScanner = new SourceSetScanner();
        File testClass = new File("src/test/resources/test.class");
        Set<String> dependencies  = sourceSetScanner.scanFile(FileUtils.openInputStream(testClass));

        assertEquals(1, dependencies.size());
        assertTrue(dependencies.contains("javax.xml.stream.FactoryConfigurationError"));
    }

    @Test
    public void sourceSetScannerShouldResolveDirectoriesAndAnalyzeFiles() {
        SourceSetScanner sourceSetScanner = new SourceSetScanner();
        File testClassFile = new File("src\\test\\resources");
        URI testClassDir = testClassFile.toURI();
        assertEquals(1, sourceSetScanner.analyze(testClassDir).size());
    }

    @Test
    public void sourceSetScannerShouldReturnClassFilesFromJar() throws MalformedURLException {
        SourceSetScanner sourceSetScanner = new SourceSetScanner();
        File jarFile = new File("src\\test\\resources\\commons-cli-1.0.jar");
        assertEquals(20, sourceSetScanner.analyzeJar(jarFile.toURI().toURL()).size());
    }
}
