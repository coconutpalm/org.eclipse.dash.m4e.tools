package m4e;

import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;

class DependencyManagementCmdTest {

    @Test
    public void testRepo1() throws Exception {
        
        MopSetup.setup()
        
        File source = new File( 'data/input/repo1' )
        File copy = new File( 'tmp/dmRepo' )
        
        assert copy.deleteDir()
        source.copy( copy )
        
        DependencyManagementCmd tool = new DependencyManagementCmd()
        
        List<String> errors = []
        def log = [ error: { errors << it }, info: {} ] as Logger
        tool.log = log
        
        tool.run([ 'dm', copy.path, 'org.eclipse.dash:dependency-management:3.7.0' ])
        
        File expectedDmFile = new File( 'tmp/dmRepo/org/eclipse/dash/dependency-management/3.7.0/dependency-management-3.7.0.pom' )
        assertTrue( "Missing file ${expectedDmFile}", expectedDmFile.exists() )
        
        String expected = new File( 'data/expected/dependency-management-3.7.0.pom' ).getText( 'utf-8' ).normalize().trim()
        String actual = expectedDmFile.getText( 'utf-8' ).normalize().trim()
        
        assertEquals( expected, actual )
        
        actual = errors.join( '\n' )
        
        // the output depends on the ordering of files in the file system
        // Do some search'n'replace to always get the same order
        actual = actual.replace( '3.5.0 and 3.6.0. Omitting 3.5.0', '3.6.0 and 3.5.0. Omitting 3.6.0' )
        
        assertEquals( 'The repository contains two versions of org.eclipse.core:org.eclipse.core.runtime: 3.6.0 and 3.5.0. Omitting 3.6.0', errors.join( '\n' ) )
    }
}
