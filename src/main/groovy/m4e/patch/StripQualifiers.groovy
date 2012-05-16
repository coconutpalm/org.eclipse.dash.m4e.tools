/*******************************************************************************
 * Copyright (c) 24.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e.patch

import java.io.File;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import m4e.Dependency;
import m4e.MavenRepositoryTools;
import m4e.Pom;

/** Strip Eclipse qualifiers from versions.
 * 
 *  <p>This patcher supports versions like "1", "1.0", "[1.0,2.0)" and standard
 *  Eclipse versions (three numbers plus optional qualifier)
 */
class StripQualifiers extends Patch {
    
    static final Logger log = LoggerFactory.getLogger( StripQualifiers )
    
    // ~/.../ isn't supported by the Eclipse Groovy editor
    Pattern versionRangePattern = Pattern.compile( '^([\\[\\]()])([^,]*),([^,]*?)([\\[\\]()])$' );
    
    GlobalPatches globalPatches
    File target
    
    void apply( Pom pom ) {
        
        updateVersion( pom )
        
        pom.dependencies.each {
            String version = it.value( Dependency.VERSION )
            
            QualifierPatch p = findQualifierPatch( it.key() )
            String newVersion
            if( p ) {
                newVersion = p.version
            } else {
                newVersion = stripQualifier( version )
            }
            
            if( version != newVersion ) {
                log.debug( 'Setting version of dependency {} to {}', it.shortKey(), newVersion )
                it.value( Dependency.VERSION, newVersion )
            }
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
        
        log.debug( 'Setting POM version to {}', newVersion )
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
            
            String snapshot = "${m[0]}-SNAPSHOT"
            
            if( parts[2] != snapshot ) {
                parts[2] = m[0]
            }
        }
        int end = Math.min( parts.size()-1, 2 )
        return parts[0..end].join( '.' )
    }
    
    @Override
    public String toString() {
        return 'StripQualifiers()';
    }
}
