package m4e.p2;

import java.util.Map;


class VersionCache {
    
    Map<String, Version> versions = [:].withDefault { pattern -> new Version( pattern ) }
    Map<String, VersionRange> ranges = [:].withDefault { pattern -> new VersionRange( pattern, this ) }
    
    Version version( String pattern ) {
        return versions.get( pattern )
    }
    
    VersionRange range( String pattern ) {
        return ranges.get( pattern )
    }
}
