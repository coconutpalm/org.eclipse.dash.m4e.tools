package m4e

class MavenRepositoryTools {

    static File buildPath( File repo, String pom, String ext = null, String qualifier = null ) {
        String[] parts = pom.split( ':', -1 )
        if( parts.size() != 3 ) {
            throw new IllegalArgumentException( "Expected exactly three tokens separated by colon: ${pom}" )
        }
        
        String groupPath = parts[0].replace( '.', '/' )
        
        File dir = new File( repo, groupPath )
        dir = new File( dir, parts[1] )
        dir = new File( dir, parts[2] )
        
        if( !ext ) {
            return dir
        }
        
        String name = "${parts[1]}-${parts[2]}"
        
        if( qualifier ) {
            name += '-' + qualifier
        }
        
        name += '.' + ext
        File file = new File( dir, name )
        return file
    }
}
