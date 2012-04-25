package m4e.patch

import de.pdark.decentxml.Element
import java.util.regex.Pattern
import m4e.Dependency;
import m4e.Pom

class OrbitPatch extends Patch {

    GlobalPatches globalPatches
    File target
    
    private List<Pattern> exclusionPatterns = null
    
    private final static String ORBIT_GROUP_ID = 'org.eclipse.orbit'
    private final static String ORBIT_ARTIFACT_ID_PREFIX = 'orbit.'
    
    void apply( Pom pom ) {
        
        compilePatterns()
        
        if( excluded( pom.groupId() ) ) {
            return
        }
        
        Element parent = pom.xml( Pom.PARENT )
        if( parent ) {
            throw new RuntimeException( "Unable to patch ${pom.source}: <parent> element not supported" )
        }
        
        pom.value( Pom.GROUP_ID, ORBIT_GROUP_ID )
        pom.value( Pom.ARTIFACT_ID, ORBIT_ARTIFACT_ID_PREFIX + pom.artifactId() )
        
        def name = pom.element( 'name' )
        if( name ) {
            name.xml.text = name.xml.text + ' supplied by Eclipse Orbit'
        }
        
        pom.dependencies.each { dep ->
            if( excluded( dep.value( Dependency.GROUP_ID ) ) ) {
                return
            }
            
            dep.value( Dependency.GROUP_ID, ORBIT_GROUP_ID )
            String artifactId = ORBIT_ARTIFACT_ID_PREFIX + dep.value( Dependency.ARTIFACT_ID )
            dep.value( Dependency.ARTIFACT_ID, artifactId )
        }
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
