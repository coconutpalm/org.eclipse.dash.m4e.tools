package m4e.p2

import java.util.List;

class MergedP2Repo implements IP2Repo {
    
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
        return "Composite repo (${repos.size()})"
    }
    
    List<IP2Repo> getRepos() {
        return repos
    }
    
    int size() {
        return repos.size()
    }
    
}
