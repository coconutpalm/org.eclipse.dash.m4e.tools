package m4e.p2

import java.io.File;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class P2Repo implements IP2Repo {
    
    static final Logger log = LoggerFactory.getLogger( P2Repo )
    
    VersionCache versionCache = new VersionCache()
    
    List<P2Category> categories = []
    List<P2Feature> features = []
    List<P2Plugin> plugins = []
    List<P2Other> others = []
    List<P2Unit> units = []
    
    Map<String, List<P2Bundle>> bundlesById = [:].withDefault { [] }
    
    void list( IndentPrinter out ) {

        out.printIndent()
        out.println( "P2 Repository ${url}" )

        list( out, 'categories', categories )
        list( out, 'features', features )
        list( out, 'plugins', plugins )
        
        out.printIndent()
        out.println( "${others.size()} other nodes:" )
        out.incrementIndent()
        for( Node item : others ) {
            out.printIndent()
            out.println( "${item}" )
        }
        
        out.decrementIndent()
    }
    
    void list( IndentPrinter out, String title, List<P2Bundle> bundles ) {

        out.printIndent()
        out.println( "${bundles.size()} ${title}:" )
        out.incrementIndent()
        
        bundles.each { it.list( out ) }
        
        out.decrementIndent()
    }
    
    private ZERO_VERSION = versionCache.version( '0.0.0' )
    
    Map<String, P2Bundle> latestCache = [:]
    
    P2Bundle latest( String id, VersionRange range = VersionRange.NULL_RANGE ) {
        
        String key = "${id}:${range}"
        P2Bundle result = null
//        P2Bundle result = latestCache[key]
//        if( result ) {
//            return result
//        }
        
        Version min = ZERO_VERSION
        def findMax = {
            if( min.compareTo( it.version ) < 0 && range.contains( it.version ) ) {
                result = it
                min = it.version
            }
        }
        bundlesById[id].each findMax
        
//        latestCache[key] = result
        
        return result
    }
    
    P2Bundle find( String id, Version version ) {
        VersionRange range = versionCache.range( "[${version},${version}]" )
        return latest( id, range)
    }
    
    File workDir
    URL url
    Downloader downloader
}
