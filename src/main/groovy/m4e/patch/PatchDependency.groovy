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

package m4e.patch

import m4e.UserError

/** Just a bean to store all the information about a dependency */
class PatchDependency {
    String groupId
    String artifactId
    String version
    boolean optional
    String scope
    
    boolean matches( def other ) {
        return key() == other.key()
    }
    
    String key() {
        return "${groupId}:${artifactId}:${version}"
    }
    
    String toString() {
        return "PatchDependency( ${key()} )"
    }
    
    static PatchDependency fromString( String s ) {
        def parts = s.split( ':', -1 )
        if( parts.size() < 3 ) {
            throw new UserError( "Expected at least three colon-separated values: [${s}]" )
        }
        
        def options = [:]
        for( String part in parts[3..<parts.size()] ) {
            def tmp = part.split( '=', 2 )
            
            if( tmp.size() != 2 ) {
                throw new UserError( "Expected key=value in [${part}] of [${s}]" )
            }
            
            options[tmp[0]] = tmp[1]
        }
        
        def optional = options.remove( 'optional' )
        def scope = options.remove( 'scope' )
        
        if( options.size() ) {
            throw new UserError( "Unexpected options ${options} in ${s}" )
        }
        
        boolean optValue = false
        if( optional != null ) {
            optValue = 'true' == optional
        }
        
        return new PatchDependency( groupId: parts[0], artifactId: parts[1], version: parts[2], optional: optValue, scope: scope )
    }
}
