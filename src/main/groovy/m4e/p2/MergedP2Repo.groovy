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

import java.util.List;

class MergedP2Repo implements IP2Repo {
    
    URL url
    
    private List<IP2Repo> repos = []

    void add( IP2Repo repo ) {
        repos << repo
    }

    P2Bundle latest( String id, VersionRange range = VersionRange.NULL_RANGE ) {
        
        for( IP2Repo repo : repos ) {
            P2Bundle result = repo.latest( id, range )
            if( result ) {
                return result
            }
        }
        
        return null;
    }

    P2Bundle find( String id, Version version ) {
        
        for( IP2Repo repo : repos ) {
            P2Bundle result = repo.find( id, version )
            if( result ) {
                return result
            }
        }
        
        return null;
    }
 
    void list( IndentPrinter out ) {
        out.printIndent()
        out.println( "Composite P2 Repository" )
        out.incrementIndent()
        
        repos.each {
            it.list( out )
        }
        
        out.decrementIndent()
    }
    
    String toString() {
        return "Composite repo ${url} (${repos.size()})"
    }
    
    List<IP2Repo> getRepos() {
        return repos
    }
    
    int size() {
        return repos.size()
    }
    
}
