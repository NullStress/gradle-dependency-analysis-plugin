package lund.gradle.plugins.asm;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 13.07.14
 * Time: 11:57
 */
public class SourceSetScanner {

    private final Set<String> dependencies;

    public SourceSetScanner() {
        dependencies = new HashSet<String>();
    }

    public Set<String> analyzeJar(URL url) {
        try {
            JarInputStream in = new JarInputStream( url.openStream() );

            JarEntry entry = null;

            while ( ( entry = in.getNextJarEntry() ) != null )
            {
                String name = entry.getName();

                if ( name.endsWith( ".class" ) )
                {
                    scanFile(in);
                }
            }

            in.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        return dependencies;
    }

    public Set<String> analyze(URL url) {
        try {
//            System.out.println("analyzing " + url);
            File startDir = new File(url.toURI());
            if(!startDir.isDirectory()) {
                return dependencies;
            }
            Collection<File> files = FileUtils.listFiles(startDir, new String[]{"class"}, true);
            for(File file : files) {
//                System.out.println("File to be scanned: " + file.getName());
                scanFile(FileUtils.openInputStream(file));
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dependencies;
    }


    public Set<String> getDependencies() {
        return dependencies;
    }

    protected void scanFile(InputStream inputStream){
        try
        {
            ClassReader reader = new ClassReader(inputStream);
            ASMDependencyAnalyzer visitor = new ASMDependencyAnalyzer();

            reader.accept( visitor, 0 );

            dependencies.addAll( visitor.getClasses() );
        }
        catch ( IOException exception )
        {
            exception.printStackTrace();
        }
        catch ( IndexOutOfBoundsException e )
        {
            // some bug inside ASM causes an IOB exception. Log it and move on?
            // this happens when the class isn't valid.
//            System.out.println( "Unable to process: stream" );
        }
    }




}
