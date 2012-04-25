package m4e.patch

import de.pdark.decentxml.Element
import java.util.regex.Pattern
import m4e.Pom

class OrbitPatch extends Patch {

    GlobalPatches globalPatches
    File target
    
    private List<Pattern> exclusionPatterns = null
    
    void apply( Pom pom ) {
        
        compilePatterns()
        
        if( excluded( pom.groupId() ) ) {
            return
        }
        
        Element parent = pom.xml( Pom.PARENT )
        if( parent ) {
            throw new RuntimeException( "Unable to patch ${pom.source}: <parent> element not supported" )
        }
        
        String oldKey = pom.key()
        
        pom.value( Pom.GROUP_ID, 'org.eclipse.orbit' )
        pom.value( Pom.ARTIFACT_ID, 'orbit.' + pom.artifactId() )
        
        String newKey = pom.key()
    }
    
    boolean excluded( String groupId ) {
        for( Pattern p : exclusionPatterns ) {
            if( p.matcher( groupId ).matches() ) {
                return true
            }
        }
        
        return false
    }
    
    void compilePatterns() {
        if( exclusionPatterns != null ) {
            return
        }
        
        def l = []
        for( String text : globalPatches.orbitExclusions ) {
            text = text.replace( '.', '\\.' ).replace( '*', '.*' )
            l << Pattern.compile( text )
        }
        
        exclusionPatterns = l
    }
}
