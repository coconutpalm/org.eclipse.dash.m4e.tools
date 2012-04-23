package m4e.p2;

import java.io.Writer;
import java.net.URL;
import java.util.List;

abstract class P2Bundle implements Comparable<P2Bundle> {
    String id
    String name
    String description
    Version version
    List<P2Dependency> dependencies = []
    
    String toString() {
        return "${getClass().simpleName}( id=${id}, version=${version}, name=${name} )"
    }
    
    void list( IndentPrinter out ) {
        String s = ''
        if( description ) {
            s = " - ${description.trim().replaceAll('\\s+', ' ')}"
        }
        out.printIndent()
        out.println( "${name}${s}" )
        
        out.incrementIndent()
        
        for( P2Dependency dep : dependencies ) {
            dep.list( out )
        }
        
        out.decrementIndent()
    }

    public int compareTo( P2Bundle o ) {
        
        int d = id.compareTo( o.id )
        if( d == 0 ) {
            d = version.compareTo( o.version )
        }
        
        return d;
    }
    
    public abstract URL downloadURL( URL baseURL );
}

class P2Plugin extends P2Bundle {
    public URL downloadURL( URL baseURL ) {
        return new URL( baseURL, "plugins/${id}_${version}.jar" )
    }
}

class P2Feature extends P2Bundle {
    public URL downloadURL( URL baseURL ) {
        String baseName = id.removeEnd( '.feature.group' )
        
        return new URL( baseURL, "features/${baseName}_${version}.jar" )
    }
}

class P2Category extends P2Bundle {
    public URL downloadURL( URL baseURL ) {
        throw new UnsupportedOperationException( "Can't download categories" )
    }
}

class P2Other {
    String id
    Version version
    String message
    
    String toString() {
        return "${id} ${version}: ${message}"
    }
}

class P2Unit {
    String id
    Version version
    String xml
    
    String toString() {
        return "${id} ${version}"
    }
}
