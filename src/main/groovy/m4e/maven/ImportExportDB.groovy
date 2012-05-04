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

package m4e.maven

import java.awt.geom.Line2D;
import org.eclipse.osgi.util.ManifestElement;
import m4e.Glob
import m4e.Pom
import m4e.PomElement
import m4e.TextNode
import m4e.patch.DeleteClasses

class ImportExportDB {

    File file
    
    Map<String /*key*/, ImportExportInfo> infos = [:]
    Map<String /*package*/, List<ImportExportInfo>> exportedBy = [:].withDefault { [] }
    
    int size() {
        return infos.size()
    }
    
    void load() {
        
        if( !file.exists() ) {
            return
        }
        
        String key
        List<String> imports = []
        List<String> exports = []
        
        file.eachLine( 'UTF-8' ) { line ->
            
            String cmd = line[0..1]
            String data = line.substring( 2 )
            
            switch( cmd ) {
                case 'P ':
                    if( key ) {
                        addInfo( key, exports, imports )
                        exports.clear()
                        imports.clear()
                    }
                    
                    key = data
                    break;
                case 'E ': exports << data; break;
                case 'I ': imports << data; break;
            }
        }
        
        if( key ) {
            addInfo( key, exports, imports )
        }
    }
    
    void addInfo( String key, List<String> exports, List<String> imports ) {
        def info = new ImportExportInfo( key: key, exports: exports.toArray(), imports: imports.toArray() )
        addInfo( info )
    }
    
    void save() {
        File tmp = new File( file.absolutePath + '.tmp' )
        tmp.parentFile?.makedirs()
        
        tmp.withWriter( "UTF-8" ) { writer ->
            def keys = infos.keySet() as ArrayList
            keys.sort()
            
            keys.each { key ->
                writer << 'P ' << key << '\n'
                infos[ key ].write( writer )
            }
        }
        
        File bak = new File( file.absolutePath + '.bak' )
        bak.usefulDelete()
        
        if( file.exists() ) {
            file.usefulRename( bak )
        }
        
        tmp.usefulRename( file )
    }
    
    static TextNode EXPORT_PACKAGE_PROPERTY = new TextNode( name: Pom.EXPORT_PACKAGE_PROPERTY )
    static TextNode IMPORT_PACKAGE_PROPERTY = new TextNode( name: Pom.IMPORT_PACKAGE_PROPERTY )
    
    void add( Pom pom ) {
        def info = toInfo( pom )
        addInfo( info )
    }
    
    ImportExportInfo toInfo( Pom pom ) {
        String key = pom.key()
        def info = new ImportExportInfo( key: key )
        
        def properties = pom.element( Pom.PROPERTIES )
        if( !properties ) {
            return info
        }
        
        String version = pom.version()
        
        info.exports = split( EXPORT_PACKAGE_PROPERTY, properties, version )
        info.imports = split( IMPORT_PACKAGE_PROPERTY, properties, version )

        return info
    }

    ImportExportInfo getAt( Pom pom ) {
        String key = pom.key()
        return getAt( key )
    }
    
    ImportExportInfo getAt( String key ) {
        return infos[ key ]
    }

    void addInfo( ImportExportInfo info ) {
        infos[ info.key ] = info
        
        info.exports.each {
            exportedBy[it] << info
        }
    }
    
    List<String> artifactsThatExport( String packageName ) {
        return exportedBy[ packageName ].collect { it.key }
    }
    
    ManifestElement[] parse( TextNode node, PomElement properties ) {
        String value = properties.value( node )
        if( !value ) {
            return null
        }
        
        return ManifestElement.parseHeader( node.name, value )
    }
    
    String[] split( TextNode node, PomElement properties, String expectedVersion ) {
        def attr = parse( node, properties )
        
        return attr ? attr.collect { it.value }.toArray().sort() : Collections.emptyList()
    }
    
    void updateExports( Glob keyPattern, List<Glob> exclusions ) {
        infos.each { key, info ->
            if( ! keyPattern.matches( key ) ) {
                return
            }
            
            println "updateExports key=${key}"
            info.updateExports( exclusions )
        }
    }
}

class ImportExportInfo {
    String key
    String[] exports
    String[] imports
    String[] deletions
    
    void write( Writer writer ) {
        exports.each {
            writer << 'E ' << it << '\n'
        }
        imports.each {
            writer << 'I ' << it << '\n'
        }
    }
    
    void updateExports( List<Glob> exclusions ) {
        
        def l = []
        
        exports = exports.findAll { e ->
            for( Glob g : exclusions ) {
                if( g.matches( e ) ) {
                    l << e
                    return false
                }
            }
            
            return true
        }
        
        println "exports=${exports}"
        
        if( l ) {
            deletions = l.toArray()
        }
        
        println "deletions=${deletions}"
    }
}