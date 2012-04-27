/*******************************************************************************
 * Copyright (c) 25.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

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
        
        if( !excluded( pom.key() ) ) {
            patchPom( pom )
        }
        
        pom.dependencies.each { dep ->
            if( excluded( dep.key() ) ) {
                return
            }
            
            dep.value( Dependency.GROUP_ID, ORBIT_GROUP_ID )
            String artifactId = ORBIT_ARTIFACT_ID_PREFIX + dep.value( Dependency.ARTIFACT_ID )
            dep.value( Dependency.ARTIFACT_ID, artifactId )
        }
    }
    
    void patchPom( Pom pom ) {
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
