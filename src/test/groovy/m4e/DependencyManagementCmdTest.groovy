/*******************************************************************************
 * Copyright (c) 24.04.2012 Aaron Digulla.
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
import org.slf4j.Logger;

class DependencyManagementCmdTest {

    @Test
    public void testRepo1() throws Exception {
        
        File copy = CommonTestCode.prepareRepo( new File( 'data/input/repo1' ), 'dmRepo' )
        
        DependencyManagementCmd tool = new DependencyManagementCmd()
        
        List<String> errors = []
        def log = [ error: { errors << it }, info: {} ] as Logger
        tool.log = log
        
        tool.run([ 'dm', copy.path, 'org.eclipse.dash:dependency-management:3.7.0' ])
        
        File expectedDmFile = new File( copy, 'org/eclipse/dash/dependency-management/3.7.0/dependency-management-3.7.0.pom' )
        assertTrue( "Missing file ${expectedDmFile}", expectedDmFile.exists() )
        
        String expected = new File( 'data/expected/dependency-management-3.7.0.pom' ).getText( 'utf-8' ).normalize().trim()
        String actual = expectedDmFile.getText( 'utf-8' ).normalize().trim()
        
        assertEquals( expected, actual )
        
        actual = errors.join( '\n' )
        
        // the output depends on the ordering of files in the file system
        // Do some search'n'replace to always get the same order
        actual = actual.replace( '3.5.0 and 3.6.0.', '3.6.0 and 3.5.0.' )
        
        assertEquals( '''\
The repository contains (at least) two versions of org.eclipse.core:org.eclipse.core.runtime: 3.5.0 and 3.6.0. Omitting both.
For details, see http://wiki.eclipse.org/MT4E_E0001'''
            , errors.join( '\n' ) )
        
        assertEquals( 1, tool.errorCount )
        assertEquals( 0, tool.warningCount )
    }
}
