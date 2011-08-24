/*******************************************************************************
 * Copyright (c) 24.08.2011 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/
package m4e

class MavenRepositoryTools {

    static File buildPath( File repo, String pom, String ext = null, String qualifier = null ) {
        String[] parts = pom.split( ':', -1 )
        if( parts.size() != 3 ) {
            throw new IllegalArgumentException( "Expected exactly three tokens separated by colon: ${pom}" )
        }
        
        String groupPath = parts[0].replace( '.', '/' )
        
        File dir = new File( repo, groupPath )
        dir = new File( dir, parts[1] )
        dir = new File( dir, parts[2] )
        
        if( !ext ) {
            return dir
        }
        
        String name = "${parts[1]}-${parts[2]}"
        
        if( qualifier ) {
            name += '-' + qualifier
        }
        
        name += '.' + ext
        File file = new File( dir, name )
        return file
    }
    
    static void eachPom( File repo, Closure c ) {
        repo.eachFile() { File it ->
            if( it.isDirectory() ) {
                eachPom( it, c )
            } else if( it.name.endsWith( '.pom' ) ){
                c.call( it )
            }
        }
    }
}
