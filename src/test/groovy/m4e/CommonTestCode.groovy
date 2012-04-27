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
}
