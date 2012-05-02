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

import java.util.List;
import java.util.Map;
import m4e.Pom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Replace dependencies in the POM with other dependencies */
class ReplaceDependencies extends Patch {
    
    final Logger log = LoggerFactory.getLogger( getClass() )

    GlobalPatches globalPatches
    List<ReplaceDependency> replacements = []
    Map<String, ReplaceDependency> replMap
    
    void init() {
        replMap = [:]
        replacements.each { replMap[it.pattern.key()] = it }
        
//        println replMap.collect { it.toString() }.join('\n')
    }
    
    void apply( Pom pom ) {
        if( !replMap ) {
            init()
        }
        
        def tool = new ProfileTool( pom: pom, defaultProfileName: globalPatches.defaultProfile, profileName: globalPatches.profile )
//        println tool.defaultProfileName
        
        pom.dependencies.each {
             String key = it.key()
             
             def replacer = replMap[key]
             if( replacer ) {
                 log.debug( 'Found {} in {}', key, pom.source )
                 
                 tool.replaceDependency( it, replacer.replacement )
             }
        }
    }
    
    String toString() {
        return "ReplaceDependencies( defaultProfile=${globalPatches.defaultProfile}, profile=${globalPatches.profile}, replacements=${replacements.size()} )"
    }
}
