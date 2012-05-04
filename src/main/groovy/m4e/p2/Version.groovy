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
    
    final static int BIT_WIDTH = 20
    final static int BIT_MASK = ( 1 << BIT_WIDTH ) - 1
    
    final boolean blank
    final long version
    int len
    final String qualifier
    
    Version( String pattern ) {
        if( pattern ) {
            def parts = pattern.split( '\\.', 4 )
            
            long major = Long.parseLong( parts[0] )
            long minor = 0
            long service = 0
            len = parts.length
            
            check( pattern, 'major', major )
            if( len > 1 ) {
                minor = Long.parseLong( parts[1] )
                check( pattern, 'minor', minor )
                
                if( len > 2 ) {
                    service = Long.parseLong( parts[2] )
                    check( pattern, 'service', service )
                    
                    if( len > 3 ) {
                        qualifier = parts[3]
                    }
                }
            }
            
            version = ( ( ( major << BIT_WIDTH ) + minor ) << BIT_WIDTH ) + service 
            
            blank = false
            
            assert pattern == toString()
        } else {
            blank = true
        }
    }
    
    void check( String version, String field, long value ) {
        if( value < 0 ) {
            throw new VersionException( version, "${field} must be > 0: ${value}" )
        }
        if( value > BIT_MASK ) {
            throw new VersionException( version, "${field} must be < ${BIT_MASK}: ${value}" )
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
        
        def buffer = prepareVersionBuffer()
        
        if( qualifier ) {
            buffer.append( '.' ).append( qualifier )
        }
        
        return buffer.toString()
    }

    String shortVersion() {
        if( blank ) {
            return ''
        }

        return prepareVersionBuffer().toString()
    }
    
    private StringBuilder prepareVersionBuffer() {
        StringBuilder buffer = new StringBuilder()
        
        buffer.append( major )
        
        if( len > 1 ) {
            buffer.append( '.' ).append( minor )
        }
        if( len > 2 ) {
            buffer.append( '.' ).append( service )
        }

        return buffer
    }
    
    public int compareTo( Version o ) {
        
        if( blank ) {
            return o.blank ? 0 : -1
        } else if( o.blank ) {
            return 1
        }
        
        int d = Long.signum( version - o.version )

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

class VersionException extends RuntimeException {
    String version
    
    VersionException( String version, String message ) {
        super( "Error parsing version '${version}': ${message}" )
        
        this.version = version
    }
}