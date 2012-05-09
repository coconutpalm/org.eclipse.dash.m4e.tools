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

public enum Warning {
    MISSING_BINARY_BUNDLE_FOR_SOURCES( 1 ),
    UNEXPECTED_FILE_IN_SOURCE_BUNDLE( 2 ),
    BINARY_DIFFERENCE( 3 ),
    MULTIPLE_NESTED_JARS( 4 ),
    UNABLE_TO_MERGE_MT4E_FILE( 5 );
    
    public final static String BASE_URL = "http://wiki.eclipse.org/MT4E_";
    
    private final static Map<String, Warning> map = new HashMap<String, Warning>();
    static {
        for( Warning e : Warning.values() ) {
            Warning old = map.put( e.code(), e );
            if( null != old ) {
                throw new IllegalStateException( "Duplicate codes: " + e + " and " + old );
            }
        }
    }
    
    private final String code;
    
    private Warning( int id ) {
        this.code = String.format( "W%04d", id );
    }
    
    public String code() {
        return code;
    }
    
    public static Warning fromCode( String code ) {
        Warning result = map.get( code );
        if( null == result ) {
            throw new IllegalArgumentException( "Undefined warning " + code );
        }
        return result;
    }
    
    public String url() {
        return BASE_URL + code();
    }
}
