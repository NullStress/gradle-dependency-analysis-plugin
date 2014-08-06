package lund.gradle.plugins.asm;

import lund.gradle.plugins.asm.SourceSetScanner;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
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
        sourceSetScanner.scanFile(FileUtils.openInputStream(testClass));

        Set<String> dependencies = sourceSetScanner.getDependencies();

        assertEquals(1, dependencies.size());
        assertTrue(dependencies.contains("javax.xml.stream.FactoryConfigurationError"));
   }
}
