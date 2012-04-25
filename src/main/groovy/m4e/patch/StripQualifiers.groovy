package m4e.patch

import java.io.File;
import java.util.regex.Pattern;
import m4e.Dependency;
import m4e.MavenRepositoryTools;
import m4e.Pom;

/** Strip Eclipse qualifiers from versions.
 * 
 *  <p>This patcher supports versions like "1", "1.0", "[1.0,2.0)" and standard
 *  Eclipse versions (three numbers plus optional qualifier)
 */
class StripQualifiers extends Patch {
    
    // ~/.../ isn't supported by the Eclipse Groovy editor
    Pattern versionRangePattern = Pattern.compile( '^([\\[\\]()])([^,]*),([^,]*?)([\\[\\]()])$' );
    
    GlobalPatches globalPatches
    File target
    
    void apply( Pom pom ) {
        
        updateVersion( pom )
        
        pom.dependencies.each {
            String version = it.value( Dependency.VERSION )
            
            QualifierPatch p = findQualifierPatch( it.key() )
            if( p ) {
                version = p.version
            } else {
                version = stripQualifier( version )
            }
            
            it.value( Dependency.VERSION, version )
        }
    }
    
    QualifierPatch findQualifierPatch( String key ) {
        for( QualifierPatch p : globalPatches.qualifierPatches ) {
            if( p.appliesTo( key ) ) {
                return p
            }
        }
        
        return null
    }
    
    void updateVersion( Pom pom ) {
        
        String key = pom.key()
        QualifierPatch p = findQualifierPatch( key )
        if( p ) {
            updateVersion( pom, p.version )
            return
        }
        
        String oldVersion = pom.version()
        String newVersion = stripQualifier( oldVersion )
        
        if( oldVersion != newVersion ) {
            updateVersion( pom, newVersion )
        }
    }
    
    void updateVersion( Pom pom, String newVersion ) {
        def e = pom.xml( Pom.VERSION )
        if( ! e ) {
            throw new RuntimeException( 'TODO Missing version element' )
        }
        
        String oldVersion = e.text
        e.text = newVersion
    }
    
    String stripQualifier( String version ) {
        if( !version ) {
            return version
        }
        
        def m = versionRangePattern.matcher( version )
        if( !m.matches() ) {
            return stripQualifier2( version )
        }
        
        def prefix = m.group(1)
        def v1 = m.group(2)
        def v2 = m.group(3)
        def postfix = m.group(4)
        
        v1 = stripQualifier2(v1)
        v2 = stripQualifier2(v2)
        
        return "${prefix}${v1},${v2}${postfix}"
    }
    
    String stripQualifier2( String version ) {
        def parts = version.split('\\.', -1)
        if( parts.size() == 3 ) {
            def m = parts[2] =~ '^\\d+'
            parts[2] = m[0]
        }
        int end = Math.min( parts.size()-1, 2 )
        return parts[0..end].join( '.' )
    }
    
    @Override
    public String toString() {
        return 'StripQualifiers()';
    }
}