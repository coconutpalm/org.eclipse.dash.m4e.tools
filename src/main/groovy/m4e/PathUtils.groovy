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

package m4e

class PathUtils {
    
    static {
        MopSetup.setup()
    }
    
    static String normalize( def path ) {
        if( path instanceof File ) {
            path = path.toString()
        }

        return path.replace( '\\', '/' )
    }
    
    static String basename( def path ) {
        String result = normalize( path )
        int pos = result.lastIndexOf( '/' )
        if( pos >= 0 ) {
            path = result.substring( pos + 1 )
        }

        return path
    }
    
    static String dirname( def path ) {
        String result = normalize( path )
        return result.substringBeforeLast( '/' )
    }
    
}
