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

package m4e;

public enum Warning {
    MISSING_BINARY_BUNDLE_FOR_SOURCES( 1 ),
    UNEXPECTED_FILE_IN_SOURCE_BUNDLE( 2 ),
    BINARY_DIFFERENCE( 3 ),
    MULTIPLE_NESTED_JARS( 4 );
    
    public final static String BASE_URL = "http://wiki.eclipse.org/MT4E_";
    
    private final int id;
    
    private Warning( int id ) {
        this.id = id;
    }
    
    public String url() {
        return BASE_URL + String.format( "W%04d", id );
    }
}
