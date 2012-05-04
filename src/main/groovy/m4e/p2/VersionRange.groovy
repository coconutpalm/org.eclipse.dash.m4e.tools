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

class VersionRange {
    
    static final NULL_RANGE = new VersionRange( null )
    
    final Version lower
    final boolean includeLower
    final Version upper
    final boolean includeUpper
    
    VersionRange( String pattern, VersionCache cache = null ) {
        if( !pattern ) {
            lower = upper = null
            includeLower = includeUpper = false
            return
        }
        
        if( pattern.startsWith( '[' ) ) {
            includeLower = true
            pattern = pattern.substring( 1 )
        } else if( pattern.startsWith( '(' ) ) {
            includeLower = false
            pattern = pattern.substring( 1 )
        } else {
            includeLower = false
        }
        
        if( pattern.endsWith( ']' ) ) {
            includeUpper = true
            pattern = pattern.substring( 0, pattern.size() - 1 )
        } else if( pattern.endsWith( ')' ) ) {
            includeUpper = false
            pattern = pattern.substring( 0, pattern.size() - 1 )
        } else {
            includeUpper = false
        }
        
        String[] parts = pattern.split( ',', 2 )
        
        if( parts.size() >= 1 ) {
            lower = newVersion( cache, parts[0] )
        } else {
            lower = null
        }
        if( parts.size() >= 2 ) {
            upper = newVersion( cache, parts[1] )
        } else {
            upper = null
        }
    }
    
    private VersionRange( Version lower, boolean includeLower, Version upper, boolean includeUpper ) {
        this.lower = lower
        this.includeLower = includeLower
        this.upper = upper
        this.includeUpper = includeUpper
    }
    
    public VersionRange stripQualifier() {
        return new VersionRange( lower?.stripQualifier(), includeLower, upper?.stripQualifier(), includeUpper )
    }
    
    private static newVersion( VersionCache cache, String pattern ) {
        if( cache ) {
            return cache.version( pattern )
        }
        
        return new Version( pattern )
    }
    
    String toString() {
        StringBuilder buffer = new StringBuilder()
        
        if( includeLower ) {
            buffer.append( '[' )
        } else if( lower && upper ) {
            buffer.append( '(' )
        }
        
        if( lower ) {
            buffer.append( lower )
        }
        
        if( upper ) {
            buffer.append( ',' ).append( upper )
        }
        
        if( includeUpper ) {
            buffer.append( ']' )
        } else if( lower && upper ) {
            buffer.append( ')' )
        }
        
        return buffer.toString()
    }
    
    String shortVersion() {
        StringBuilder buffer = new StringBuilder()
        
        if( includeLower ) {
            buffer.append( '[' )
        } else if( lower && upper ) {
            buffer.append( '(' )
        }
        
        if( lower ) {
            buffer.append( lower.shortVersion() )
        }
        
        if( upper ) {
            buffer.append( ',' ).append( upper.shortVersion() )
        }
        
        if( includeUpper ) {
            buffer.append( ']' )
        } else if( lower && upper ) {
            buffer.append( ')' )
        }
        
        return buffer.toString()
    }
    
    boolean contains( Version version ) {
        int d1 = lower ? lower.compareTo( version ) : -1
        int d2 = upper ? version.compareTo( upper ) : -1
        
        int max1 = includeLower ? 0 : -1
        int max2 = includeUpper ? 0 : -1
        
        //println "${d1} ${d2} ${max1} ${max2}"
        
        return d1 <= max1 && d2 <= max2
    }
    
    
}
