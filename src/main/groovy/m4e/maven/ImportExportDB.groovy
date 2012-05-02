package m4e.maven

import java.awt.geom.Line2D;
import org.eclipse.osgi.util.ManifestElement;
import m4e.Pom
import m4e.PomElement
import m4e.TextNode

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
            infos.each { key, info ->
                writer << 'P ' << key << '\n'
                info.write( writer )
            }
        }
        
        File bak = new File( file.absolutePath + '.bak' )
        bak.usefulDelete()
        
        if( file.exists() ) {
            file.usefulRename( bak )
        }
        
        tmp.usefulRename( file )
    }
    
    private def EXPORT_PACKAGE_PROPERTY = new TextNode( name: Pom.EXPORT_PACKAGE_PROPERTY )
    private def IMPORT_PACKAGE_PROPERTY = new TextNode( name: Pom.IMPORT_PACKAGE_PROPERTY )
    
    void updatePom( Pom pom ) {
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
    
    void addInfo( ImportExportInfo info ) {
        infos[ info.key ] = info
        
        info.exports.each {
            exportedBy[it] << info
        }
    }
    
    List<String> artifactsThatExport( String packageName ) {
        return exportedBy[ packageName ].collect { it.key }
    }
    
    String[] split( TextNode node, PomElement properties, String expectedVersion ) {
        String value = properties.value( node )
        if( !value ) {
            return Collections.emptyList()
        }
        
        def attr = ManifestElement.parseHeader( node.name, value )
        
        return attr.collect { it.value }.toArray()
    }
}

class ImportExportInfo {
    String key
    String[] exports
    String[] imports
    
    void write( Writer writer ) {
        exports.each {
            writer << 'E ' << it << '\n'
        }
        imports.each {
            writer << 'I ' << it << '\n'
        }
    }
}