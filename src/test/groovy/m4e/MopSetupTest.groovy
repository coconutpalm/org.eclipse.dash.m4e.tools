package m4e;

import static org.junit.Assert.*;
import java.io.File;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;

class MopSetupTest {

    File tmpDir = new File( 'tmp-test' )
    
    @BeforeClass
    static void mopSetup() {
        MopSetup.setup();
    }

    @Test
    public void testRemoveEnd() throws Exception {
        assert 'aaab' == 'aaabx'.removeEnd( 'x' )
        assert 'aaabx' == 'aaabx'.removeEnd( 'xx' )
        assert 'aaaby' == 'aaaby'.removeEnd( 'x' )
        assert 'aaaby' == 'aaaby'.removeEnd( null )
        assert 'aaaby' == 'aaaby'.removeEnd( '' )
    }
    
    @Test
    public void testSubstringBeforeLast() throws Exception {
        assert 'aaa' == 'aaabx'.substringBeforeLast( 'b' )
        assert 'abaa' == 'abaabx'.substringBeforeLast( 'b' )
        assert 'aaabx' == 'aaabx'.substringBeforeLast( 'xx' )
        assert 'aaaby' == 'aaaby'.substringBeforeLast( 'x' )
        assert 'aaab' == 'aaaby'.substringBeforeLast( 'y' )
        assert 'aa' == 'aaaby'.substringBeforeLast( 'a' )
        assert 'aaaby' == 'aaaby'.substringBeforeLast( null )
        assert 'aaaby' == 'aaaby'.substringBeforeLast( '' )
    }
    
    @Test
    public void testSubstringBefore() throws Exception {
        assert 'aaa' == 'aaabx'.substringBefore( 'b' )
        assert 'a' == 'abaabx'.substringBefore( 'b' )
        assert 'aaabx' == 'aaabx'.substringBefore( 'xx' )
        assert 'aaaby' == 'aaaby'.substringBefore( 'x' )
        assert 'aaab' == 'aaaby'.substringBefore( 'y' )
        assert '' == 'aaaby'.substringBefore( 'a' )
        assert 'aaaby' == 'aaaby'.substringBefore( null )
        assert 'aaaby' == 'aaaby'.substringBefore( '' )
    }
    
    @Test
    public void testUnzip() throws Exception {
        File workDir = new File( tmpDir, "testUnzip" )
        assert workDir.deleteDir(), "Can't delete ${workDir}"
        workDir.makedirs()

        new File( "data/priming.zip" ).unzip( workDir )
        
        File expected = new File( workDir, "priming/eclipse/plugins/org.eclipse.core.boot_3.1.200.v20100505.jar" )
        assert expected.exists(), "File ${expected} wasn't created"
    }
    
    @Test
    public void testUnzipIllegalPaths() throws Exception {
        File workDir = new File( tmpDir, "testUnzipIllegalPaths" )
        assert workDir.deleteDir(), "Can't delete ${workDir}"
        workDir.makedirs()

        File archive = new File( workDir, 'illegal.zip' )
        JarOutputStream stream = new JarOutputStream( archive.newOutputStream() )
        
        JarEntry entry = new JarEntry( "../x" )
        stream.putNextEntry( entry )
        
        stream.write( 'xxx'.getBytes( 'UTF-8' ) )
        
        stream.close()
        
        try {
            archive.unzip( workDir )
        } catch( RuntimeException e ) {
            assertEquals( 
                "ZIP archive contains odd entry '../x' which would create the file ${workDir.absoluteFile}${File.separator}../x" as String
                , e.message )
        }
    }
}
