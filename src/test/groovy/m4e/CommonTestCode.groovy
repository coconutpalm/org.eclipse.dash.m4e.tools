/*******************************************************************************
 * Copyright (c) 25.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e

import groovy.io.FileType;

class CommonTestCode {

    static {
        MopSetup.setup()
    }
    
    static final File testDir = new File( 'tmp-test' )
    
    static File prepareRepo( File template, String repoName ) {
        
        File copy = newFile( repoName )
        assert copy.deleteDir()
        
        template.copy( copy )
        
        return copy
    }
    
    static File newFile( String path ) {
        return new File( testDir, path )
    }
    
    static String listFiles( File root ) {
        List paths = []
        
        root.eachFileRecurse( FileType.FILES ) { it ->
            String path = it.pathRelativeTo( root )
            paths << path
        }
        
        paths.sort()
        
        return paths.join( '\n' )
    }
    
    static void fileEquals( String expected, File file ) {
        String text = file.getText( CommonConstants.UTF_8 ).trim().replace( '\r\n', '\n' )
        
        org.junit.Assert.assertEquals( "Unexpected content in ${file.absolutePath}", expected, text )
    }
}
