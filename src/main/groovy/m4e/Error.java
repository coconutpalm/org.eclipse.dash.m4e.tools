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

import java.util.HashMap;
import java.util.Map;

public enum Error {
    TWO_VERSIONS( 1 ),
    MAVEN_FAILED( 2 ),
    MISSING_MANIFEST( 3 ),
    IMPORT_ERROR( 4 );

    private final static Map<String, Error> map = new HashMap<String, Error>();
    static {
        for( Error e : Error.values() ) {
            Error old = map.put( e.code(), e );
            if( null != old ) {
                throw new IllegalStateException( "Duplicate codes: " + e + " and " + old );
            }
        }
    }

    private final String code;
    
    private Error( int id ) {
        this.code = String.format( "E%04d", id );
    }
    
    public String code() {
        return code;
    }
    
    public static Error fromCode( String code ) {
        Error result = map.get( code );
        if( null == result ) {
            throw new IllegalArgumentException( "Undefined Error " + code );
        }
        return result;
    }

    public String url() {
        return Warning.BASE_URL + code();
    }
}
