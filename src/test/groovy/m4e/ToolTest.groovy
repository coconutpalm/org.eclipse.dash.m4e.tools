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
import org.junit.Test;

class ToolTest {

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
clean 
\t- Clean the work directory'''
                 , e.message )
        }
    }
    
    File tmpDir = new File( 'tmp-test' )
    
    @Test
    public void testClean() throws Exception {
        Tool.mopFile()
        
        File workDir = new File( tmpDir, "testClean" )
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
        Tool.mopString()
        
        assert 'aaab' == 'aaabx'.removeEnd( 'x' )
        assert 'aaabx' == 'aaabx'.removeEnd( 'xx' )
        assert 'aaaby' == 'aaaby'.removeEnd( 'x' )
        assert 'aaaby' == 'aaaby'.removeEnd( null )
        assert 'aaaby' == 'aaaby'.removeEnd( '' )
    }
    
    @Test
    public void testSubstringBeforeLast() throws Exception {
        Tool.mopString()
        
        assert 'aaa' == 'aaabx'.substringBeforeLast( 'b' )
        assert 'abaa' == 'abaabx'.substringBeforeLast( 'b' )
        assert 'aaabx' == 'aaabx'.substringBeforeLast( 'xx' )
        assert 'aaaby' == 'aaaby'.substringBeforeLast( 'x' )
        assert 'aaab' == 'aaaby'.substringBeforeLast( 'y' )
        assert 'aa' == 'aaaby'.substringBeforeLast( 'a' )
        assert 'aaaby' == 'aaaby'.substringBeforeLast( null )
        assert 'aaaby' == 'aaaby'.substringBeforeLast( '' )
    }
}