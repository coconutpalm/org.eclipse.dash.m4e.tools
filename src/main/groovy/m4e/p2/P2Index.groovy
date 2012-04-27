/*******************************************************************************
 * Copyright (c) 23.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e.p2

import java.io.File;


class P2Index {

    String contentJarName
    String contentXmlName
    
    P2Index( File file ) {
        
        Properties properties = new Properties();
        file.withInputStream {
            properties.load( it )
        }
        
        def value = properties.getProperty( 'metadata.repository.factory.order' )
        value = value.trim().removeEnd( ',!' )
        
        contentXmlName = value
        contentJarName = value.removeEnd( '.xml' ) + '.jar'
    }
}
