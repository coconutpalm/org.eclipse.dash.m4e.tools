package m4e.maven

class VersionUtils {

    static List sort( List versions ) {
        return versions.sort { a, b -> compare( a, b ) }
    }
    
    static int compare( String version1, String version2 ) {
        [ version1, version2 ]*.tokenize('.')*.collect { it.isInteger() ? new Integer( it ) : it }.with { u, v ->
            [ u,v ].transpose().findResult{ x,y-> x<=>y ?: null } ?: u.size() <=> v.size()
        }
    }
}
