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

class P2Dependency implements Comparable<P2Dependency> {
    String type
    String id
    VersionRange versionRange
    
    String toString() {
        return "${getClass().simpleName}( id=${id}, version=${versionRange}, type=${type} )"
    }
    
    void list( IndentPrinter out ) {
        out.printIndent()
        out.println( "${id} ${versionRange.shortVersion()}" )
    }
    
    public int compareTo( P2Dependency o ) {
        int d = id.compareTo( o.id )
        
        if( d == 0 ) {
            d = versionRange.lower.compareTo( o.versionRange.lower )
        }
        
        return d
    }
    
    @Override
    public boolean equals( Object obj ) {
        if( this == obj ) {
            return true
        }
        
        if( !(obj instanceof P2Dependency) ) {
            return false
        }
        
        P2Dependency other = obj
        if( !id.equals( other.id ) ) {
            return false
        }
        if( !type.equals( other.type ) ) {
            return false
        }
        if( !versionRange.equals( other.versionRange ) ) {
            return false
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode() *  31 + type.hashCode() * 37 + versionRange.hashCode() * 97;
    }
}
