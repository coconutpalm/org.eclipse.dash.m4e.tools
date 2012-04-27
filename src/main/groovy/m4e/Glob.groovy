/*******************************************************************************
 * Copyright (c) 27.07.2011 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e

import java.util.regex.Pattern

class Glob {

    private String pattern
    private Pattern compiled
    
    Glob( String pattern, String manyRegexp = '.*' ) {
        
        this.pattern = pattern
        
        def parts = pattern.split( '\\*', -1 )
        if( parts.size() > 1 ) {
            pattern = parts.collect { Pattern.quote( it ) }.join( manyRegexp )
            compiled = Pattern.compile( pattern )
        }
    }
    
    boolean matches( String text ) {
        if( compiled ) {
            return compiled.matcher( text ).matches()
        }
        
        return pattern == text
    }
    
    String toString() {
        return pattern
    }
}
