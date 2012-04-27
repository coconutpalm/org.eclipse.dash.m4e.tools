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

package m4e.p2;

class Version implements Comparable<Version> {
    
    final static int BIT_WIDTH = 10
    final static int BIT_MASK = ( 1 << BIT_WIDTH ) - 1
    
    final boolean blank
    final long version
    final String qualifier
    
    Version( String pattern ) {
        if( pattern ) {
            String[] parts = pattern.split( '\\.', 4 )
            
            int major = Integer.parseInt( parts[0] )
            int minor = Integer.parseInt( parts[1] )
            int service = Integer.parseInt( parts[2] )
            
            version = ( ( ( major << BIT_WIDTH ) + minor ) << BIT_WIDTH ) + service 
            
            qualifier = parts.size() == 4 ? parts[3] : null
            blank = false
            
            assert pattern == toString()
        } else {
            blank = true
        }
    }
    
    boolean equals( Object o ) {
        if( this == o ) {
            return true
        }
        
        if( !( o instanceof Version ) ) {
            return false
        }
        
        Version other = o
        
        if( blank != other.blank ) {
            return false
        }
        if( version != other.version ) {
            return false
        }
        if( qualifier != other.qualifier ) {
            return false
        }
        
        return true
    }
    
    int hashCode() {
        if( blank ) {
            return 0
        }
        
        return 1311 * version + ( qualifier ? qualifier.hashCode() : 0 )
    }
    
    int getMajor() {
        return version >>> (2*BIT_WIDTH)
    }
    
    int getMinor() {
        return ( version >>> BIT_WIDTH ) & BIT_MASK 
    }
    
    int getService() {
        return version & BIT_MASK 
    }
    
    String toString() {
        if( blank ) {
            return ''
        }
        
        String s = ''
        if( qualifier ) {
            s = ".${qualifier}"
        }
        
        return "${major}.${minor}.${service}${s}"
    }

    String shortVersion() {
        if( blank ) {
            return ''
        }

        return "${major}.${minor}.${service}"
    }
    
    public int compareTo( Version o ) {
        
        if( blank ) {
            return o.blank ? 0 : -1
        }
        
        int d = version - o.version

        if( d == 0 ) {
            if( qualifier ) {
                if( o.qualifier ) {
                    d = qualifier.compareTo( o.qualifier )
                } else {
                    d = 1
                }
            } else {
                d = o.qualifier ? -1 : 0
            }
        }
        
        return d;
    }
    
    Version next() {
        return new Version( "${major}.${minor}.${service+1}" )
    }
    
    Version stripQualifier() {
        return new Version( "${major}.${minor}.${service}" )
    }
}
