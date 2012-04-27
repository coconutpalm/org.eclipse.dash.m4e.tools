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

package m4e.patch

import m4e.MavenRepositoryTools;
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

class ArtifactRenamer {

    static final Logger log = LoggerFactory.getLogger( ArtifactRenamer )
    
    File target
    String oldKey
    String newKey
    
    File newPomPath
    
    void run() {
        File oldPomPath = MavenRepositoryTools.buildPath( target, oldKey, 'pom' )
        newPomPath = MavenRepositoryTools.buildPath( target, newKey, 'pom' )
        
        log.info( "Renaming ${oldKey} to ${newKey}" )
        
        File oldFolder = oldPomPath.parentFile
        File newFolder = newPomPath.parentFile
        newFolder.makedirs()
        
        String prefix = oldKey.substringAfter( ':' ).replace( ':', '-' )
        String newPrefix = newKey.substringAfter( ':' ).replace( ':', '-' )
        
        int extraFileCount = 0
        oldFolder.eachFile { it ->
            if( it.name.startsWith( prefix ) ) {
                String newName = newPrefix + it.name.substring( prefix.size() )
                File dest = new File( newFolder, newName )
                assert it.renameTo( dest )
            } else {
                extraFileCount ++
            }
        }
        
        if( extraFileCount == 0 ) {
            assert oldFolder.delete()
        }
    }
}
