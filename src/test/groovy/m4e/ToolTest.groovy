/*******************************************************************************
 * Copyright (c) 25.07.2011 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e;

import static org.junit.Assert.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;

class ToolTest {
    
    @BeforeClass
    static void mopSetup() {
        MopSetup.setup();
    }

    @Test
    public void testNoArguments() throws Exception {
        try {
            new Tool().run()
        } catch( UserError e ) {
            assertEquals (
                 '''\
Missing command. Valid commands are:
[ install | in ] archives...
\t- Extract the specified archives and convert the Eclipse plug-ins inside into Maven artifacts
[ merge | me ] directories... destination
\t- Merge several Maven repositories into one.

\tFor safety reasons, destination must not exist.
[ attach-sources | as | attach | sources ] directories...
\t- Source for source JARs and move them in the right place for Maven 2
convert groupId:artifactId:version
\t- Convert everything in the directory "downloads" into one big Maven repository

\tThe argument is used to create a POM file with a dependencyManagement element.
clean 
\t- Clean the work directory'''
                 , e.message )
        }
    }
    
    File tmpDir = new File( 'tmp-test' )
    
    @Test
    public void testClean() throws Exception {
        
        tmpDir = new File( tmpDir, "testClean" )
        assert tmpDir.deleteDir(), "Can't delete ${tmpDir}"
        
        File workDir = new File( tmpDir, "work" )
        workDir.makedirs()
        
        File mustSurvive = new File( tmpDir, 'mustSurvive' )
        File mustBeDeleted = new File( workDir, 'mustBeDeleted' )
        
        mustSurvive << 'xxx'
        mustBeDeleted << 'xxx'
        
        new Tool( workDir: workDir ).run( 'clean' )
        
        assert mustSurvive.exists()
        assert !mustBeDeleted.exists()
        assert !workDir.exists()
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
