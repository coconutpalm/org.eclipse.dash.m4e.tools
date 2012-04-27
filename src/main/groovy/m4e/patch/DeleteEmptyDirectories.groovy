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

class DeleteEmptyDirectories {

    int counter = 0
    
    int delete( File root ) {
        
        int count = 0
        
        root.eachFile { it ->
            if( it.isDirectory() ) {
                count += delete( it )
            } else {
                count ++
            }
        }
        
        if( 0 == count ) {
            assert root.delete()
            counter ++
        }
        
        return count
    }
}
