package m4e.p2

import java.util.Map;

class DependencyCache {
    Map<String, P2Dependency> cache = [:]
    
    P2Dependency dependency( String id, String type, VersionRange versionRange ) {
        String key = "${id}:${type}:${versionRange}"
        
        P2Dependency result = cache.get( key )
        if( !result ) {
            result = new P2Dependency( id: id, type: type, versionRange: versionRange )
            cache[key] = result
        }
        
        return result
    }
}
