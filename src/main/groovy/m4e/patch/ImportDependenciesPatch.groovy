/*******************************************************************************
 * Copyright (c) 02.05.2012 Aaron Digulla.
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
import m4e.Dependency;
import m4e.Pom
import m4e.PomElement
import m4e.PomUtils;
import m4e.maven.ImportExportDB
import m4e.maven.ImportExportInfo
import org.eclipse.osgi.util.ManifestElement;
import org.slf4j.Logger 
import org.slf4j.LoggerFactory

class ImportDependenciesPatch extends Patch {
    
    Logger log = LoggerFactory.getLogger( ImportDependenciesPatch ) 
    
    ImportExportDB db

    @Override
    void apply( Pom pom ) {
        
        if( ! db ) {
            return
        }
        
        ImportExportInfo info = db[ pom ]
        if( !info ) {
            return
        }
        
        Set<String> additionalDependencies = new LinkedHashSet()
        Map<String, String> shortToKey = [:]
        
        info.imports.each {
            def l = db.artifactsThatExport( it )
            if( l.size() == 0 ) {
                // TODO 
                log.error( "I know no artifact that exports {}", it )
                return
            }

            if ( l.size() > 1 ) {
                // TODO 
                log.error( "Expected only one artifact that exports {} but got ${l}", it )
                return
            }
            
            String key = l[0]
            additionalDependencies << key
            shortToKey[ key.substringBeforeLast( ':' ) ] = key
        }
        
        pom.dependencies.each { dep ->
            String key = shortToKey[ dep.shortKey() ]
            additionalDependencies.remove( key )
        }
        
        def dependencies = PomUtils.getOrCreate( pom.xml, Pom.DEPENDENCIES.name )
        
        additionalDependencies.each {
            addDependency( pom, dependencies, it )
        }
        
        updateProperties( pom, info )
    }
    
    void updateProperties( Pom pom, ImportExportInfo info ) {
//        println "updateProperties ${pom.key()}"
//        println info.deletions
        
        if( !info.deletions ) {
            return
        }
        
        def properties = pom.element( Pom.PROPERTIES )
        def exportProperty = properties.value( ImportExportDB.EXPORT_PACKAGE_PROPERTY )
        if( !exportProperty ) {
            return
        }
        
        def attr = ManifestElement.parseHeader( Pom.EXPORT_PACKAGE_PROPERTY, exportProperty ) as List
        def filterSet = info.deletions as Set
        
        def newValue = attr.findAll { println it.value ; !filterSet.contains( it.value ) }.join( ',' )
        
        properties.value( ImportExportDB.EXPORT_PACKAGE_PROPERTY, newValue )
    }

    void addDependency( Pom pom, Element dependencies, String key ) {
        def xml = new Element( 'dependency' )
        
        def parts = key.split( ':' )
        
        PomUtils.getOrCreate( xml, Dependency.GROUP_ID.name ).text = parts[0]
        PomUtils.getOrCreate( xml, Dependency.ARTIFACT_ID.name ).text = parts[1]
        PomUtils.getOrCreate( xml, Dependency.VERSION.name ).text = "[${parts[2]},)"
        
        dependencies.addNode( xml )
    }
}
