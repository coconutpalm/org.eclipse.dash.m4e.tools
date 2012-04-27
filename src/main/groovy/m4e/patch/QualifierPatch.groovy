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

import java.util.regex.Pattern;

class QualifierPatch {
    /** Apply this patch to these POMs */
    Pattern pattern
    /** The new version string for matching POMs */
    String version
    
    QualifierPatch( String pattern, String version ) {
        this.pattern = compile( pattern )
        this.version = version
    }
    
    Pattern compile( String text ) {
        return Pattern.compile( text.replace( '.', '\\.' ).replace( '*', '[^:]*' ) )
    }
    
    boolean appliesTo( String key ) {
        if( pattern.matcher( key ).matches() ) {
            return true
        }
        
        return false
    }
}
