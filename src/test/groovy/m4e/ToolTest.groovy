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
import org.junit.BeforeClass;
import org.junit.Test;

class ToolTest {
    
    @BeforeClass
    static void mopSetup() {
        MopSetup.setup();
    }

    final static String HELP_OUTPUT = '''\
convert groupId:artifactId:version patches...
    - Convert everything in the directory "downloads" into one big Maven
    repository

    The first argument is used to create a POM file with a dependencyManagement
    element.
[ install | in ] archives...
    - Extract the specified archives and convert the Eclipse plug-ins inside
    into Maven artifacts
[ merge | me ] directories... destination
    - Merge several Maven repositories into one.

    For safety reasons, destination must not exist.
[ attach-sources | as | attach | sources ] directories...
    - Source for source JARs and move them in the right place for Maven 2
[ apply-patches | patch | ap ] target patches...
    - Apply the patches to the target repository. Patches can be scripts or
    directories with scripts.
[ analyze | an ] repository
    - Check a converted Maven 2 repository for various problems
[ dependency-management | dm ] repository groupId:artifactId:version
    - Create a POM with a huge dependencyManagement element that contains all
    the versions of all the artifacts in the repository
clean
    - Clean the work directory
[ p2list | p2ls ] URL
    - List the content of a P2 repository.''' 
    
    @Test
    public void testNoArguments() throws Exception {
        try {
            new Tool().run()
        } catch( UserError e ) {
            assertEquals ( 'Missing command. Valid commands are:\n' + HELP_OUTPUT, e.message )
        }
    }
    
    @Test
    public void testHelp() throws Exception {
        boolean called = false
        
        Tool tool = new Tool() {
            void print( String text ) {
                assertEquals( HELP_OUTPUT, text )
                called = true
            }
        }
        
        tool.run( 'help' )
        assertTrue( "Help wasn't printed", called )
        
        called = false
        tool.run( '--help' )
        assertTrue( "Help wasn't printed", called )
        
        called = false
        tool.run( '-h' )
        assertTrue( "Help wasn't printed", called )
    }
    
    @Test
    public void testClean() throws Exception {
        
        File tmpDir = CommonTestCode.newFile( "testClean" )
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
}
