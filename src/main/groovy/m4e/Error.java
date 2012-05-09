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

public enum Error {
    TWO_VERSIONS( 1 ),
    MAVEN_FAILED( 2 ),
    MISSING_MANIFEST( 3 );
    
    private final int id;
    
    private Error( int id ) {
        this.id = id;
    }
    
    public String code() {
        return String.format( "E%04d", id );
    }
    
    public String url() {
        return Warning.BASE_URL + code();
    }
}
