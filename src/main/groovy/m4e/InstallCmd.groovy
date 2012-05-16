/*******************************************************************************
 * Copyright (c) 25.07.2011 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e

import groovy.xml.MarkupBuilder
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Constants;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

class InstallCmd extends AbstractCommand {

    final static String DESCRIPTION = '''archives...\n- Extract the specified archives and convert the Eclipse plug-ins inside into Maven artifacts'''
    
    void doRun( String... args ) {
        // args[1..-1] throws "IndexOutOfBoundsException: toIndex = 2" if array has only one element
        if( args.size() > 1 ) {
            for( String archive in args[1..-1] ) {
                importArchive( archive )
            }
        }
        
        log.info( "Import complete. Imported ${statistics.bundleCount} Eclipse bundles into ${statistics.pomCount} POMs. There are ${statistics.jarCount} new JARs of which ${statistics.sourceCount} have sources" )
        
        if( m2repos.size() == 1 ) {
            log.info( "The new Maven 2 repository is here: ${m2repos[0].absolutePath}" )
        } else {
            log.info( "${m2repos.size()} Maven 2 repositories were created:" )
            m2repos.each {
                log.info( "    ${it.absolutePath}" )
            }
        }
    }
    
    List<File> m2repos = []
    ImportStatistics statistics = new ImportStatistics()
    
    void importArchive( String url ) {
        
        File archive = downloadArchive( url )
        File path = unpackArchive( archive )
        
        ImportTool tool = importIntoTmpRepo( path )
        
        File m2repo = tool.m2repo
        m2repos << m2repo
    }

    ImportTool importIntoTmpRepo( File path ) {
        def tool = new ImportTool( installCmd: this )
        tool.run( path )
        return tool
    }

    def archiveExtensions = [ '.tar', '.tar.gz', '.tar.bz2', '.zip' ]

    /** Unpack an archive for import */
    File unpackArchive( File archive ) {

        if( archive.isDirectory() ) {
            log.debug( "Archive ${archive} is a directory; no need to unpack" )
            return archive
        }

        String dirName = basename( archive )

        for( String ext in archiveExtensions ) {
            dirName = dirName.removeEnd( ext )
        }

        def path = new File( workDir, dirName )
        if( path.exists() ) {
            log.debug( "Archive ${archive} is already unpacked at ${path}" )
            return path
        }

        log.info( "Unpacking ${archive}" )

        if( archive.name.endsWith( '.zip' ) ) {
            archive.unzip( path )
        } else if( archive.name.endsWithOneOf( '.tar', '.tar.gz', '.tar.bz2' ) ) {
            File log = new File( path.absolutePath + '.log' ) 
        
            archive.untar( path, log )
        } else {
            throw new RuntimeException( "Unsupported archive type ${archive}" )
        }

        log.info( 'OK' )
        return path
    }
    
    /** Download an archive via HTTP.
     *  If the value of archive is not a URL, do nothing.
     *  
     *  This function returns the name of the downloaded file.
     */
    File downloadArchive( String url ) {
        if( !url.toString().startsWith('http://') ) {
            log.debug( "Archive URL ${url} seems to be local" )
            return new File( url )
        }

        String basename = basename( archive )
        File path = new File( workDir, basename )

        if( path.exists() ) {
            log.debug('Archive ${path} has already been downloaded')
            return path
        }

        log.info( "Downloading ${url} to ${path}" )
        download( url, path )
        log.info( 'OK' )

        return path
    }

    String basename( def path ) {
        if( path instanceof File ) {
            return path.name
        }

        String result = path.toString()
        int pos = result.lastIndexOf( '/' )
        if( pos >= 0 ) {
            path = result.substring( pos + 1 )
        }

        return path
    }

    void download( String url, File path ) {
        path.withOutputStream() {
            def stream = new URL( url ).openStream()
            it << stream
            stream.close()
        }
    }

}

class FileFound extends Exception {
    File file;
}

class ImportStatistics {
    int bundleCount = 0
    int pomCount = 0
    int jarCount = 0
    int sourceCount = 0
}

class SourceSnapshotVersionMap {
    /** 
     * Filled when a binary bundle is encountered.
     * 
     * <p>Eclipse key (value of Eclipse-SourceBundle) -> Maven path of source JAR */
    Map<String, File> binaryMap = [:]
    
    /** 
     * Filled when a source bundle is encountered and the key can't be found in knownMap.
     * 
     * <p>Eclipse key (value of Eclipse-SourceBundle) -> Maven path of source JAR */
    Map<String, File> unknownMap = [:]
}

class ImportTool {

    static final Logger log = LoggerFactory.getLogger( ImportTool )
    
    static boolean DEBUG = true
    
    static final String FAILURE_FILE_NAME = 'failure'

    InstallCmd installCmd
    SourceSnapshotVersionMap versionMap = new SourceSnapshotVersionMap()
    
    File eclipseFolder
    File tmpHome
    File m2repo
    File failure
    File m2settings
    
    void run( File path ) {
        assert path != null
        assert installCmd != null
        assert installCmd.workDir != null

        eclipseFolder = locate( path, 'plugins' )
        if( !eclipseFolder ) {
            throw new RuntimeException( "Can't find plugins folder below ${path.absolutePath}" )
        }
        eclipseFolder = eclipseFolder.parentFile
        assert eclipseFolder != null

        tmpHome = new File( installCmd.workDir, path.name + '_home' )
        m2repo = new File( tmpHome, 'm2repo' )
        failure = new File( tmpHome, FAILURE_FILE_NAME )
        
        installCmd.prepareErrorLog( m2repo, 'install' )
        
        log.debug( "Importing plug-ins from ${eclipseFolder} into repo ${m2repo}" )
        clean()

        doImport()
        
        moveSourceBundles()
        
        log.info( 'OK' )
        
        failure.delete()
    }
    
    void doImport() {
        
        // TODO convert features
        
        File pluginsFolder = new File( eclipseFolder, 'plugins' )

        pluginsFolder.eachFile { it ->
            
            try {
                doImport( it )
            } catch( Exception e ) {
                installCmd.error( Error.IMPORT_ERROR, "Error processing ${it.absolutePath}: ${e}", e, [ file: it.absolutePath ] )
            }
        }
    }
    
    void doImport( File bundle ) {
        def tool = new BundleConverter( installCmd: installCmd, m2repo: m2repo, statistics: installCmd.statistics, versionMap: versionMap )
        
        if( bundle.isDirectory() ) {
            tool.importExplodedBundle( bundle )
        } else {
            tool.importBundle( bundle )
        }
        
        tool.close()
    }
    
    /** Make sure we don't have any leftovers from previous attempts. */
    void clean() {
        if( tmpHome.exists() ) {
            log.info('Cleaning up from last run...')
            tmpHome.deleteDir()
        }

        tmpHome.makedirs()

        // This file is deleted after the import succeeds        
        failure << 'Import failed'
    }

    File locate( File root, String name ) {
        try {
            root.eachDirRecurse() { File dir ->
                if( dir.name == name ) {
                    throw new FileFound( file: dir )
                }
            }
        } catch( FileFound e ) {
            return e.file
        }

        return null
    }
    
    void moveSourceBundles() {
        versionMap.unknownMap.each { eclipseKey, sourceJar ->
            File target = versionMap.binaryMap[ eclipseKey ]
            
            if( target ) {
                log.debug( "Moving ${sourceJar} to ${target}" )
                sourceJar.usefulRename( target )
                
                File parent = sourceJar.parentFile
                if( parent.list().size() == 0 ) {
                    parent.usefulDelete()
                }
            } else {
                installCmd.warn( Warning.MISSING_BINARY_BUNDLE_FOR_SOURCES, "No binary bundle for ${sourceJar.absolutePath}" )
            }
        }
    }
}

class BundleConverter {
    
    private final static Logger log = LoggerFactory.getLogger( BundleConverter )
    
    InstallCmd installCmd
    File m2repo
    
    Manifest manifest
    String groupId
    String artifactId
    String version
    
    File bundle
    
    ImportStatistics statistics
    SourceSnapshotVersionMap versionMap
    
    void importBundle( File bundleJar ) {
        this.bundle = bundleJar
        
        manifest = loadManifestFromJar( bundleJar )
        if( ! manifest ) {
            return
        }
        
        statistics.bundleCount ++
        
        log.debug( 'Importing {}', bundleJar )
        
        examineManifest()
        
        loadMavenProperties( bundleJar )
        
        if( isSourceBundle() ) {
            return
        }
        
        String key = "${groupId}:${artifactId}:${version}"
        log.info( "Importing ${key}" )
        File jarFile = MavenRepositoryTools.buildPath( m2repo, key, 'jar' )
        versionMap.binaryMap[ eclipseKey ] = MavenRepositoryTools.buildPath( m2repo, key, "jar", "sources" )
        
        String classPath = manifest.attr.getValue( 'Bundle-ClassPath' )
        if( classPath && classPath != '.' ) {
            unpackNestedJar( classPath, jarFile )
        } else {
            bundleJar.copy( jarFile )
        }
        
        statistics.jarCount ++
        
        File pomFile = MavenRepositoryTools.buildPath( m2repo, key, 'pom' )
        pomFile.withWriter( 'UTF-8' ) {
            createPom( it )
        }
    }
    
    void loadMavenProperties( File bundleJar ) {
        
        ZipEntry pomProperties
        
        for( ZipEntry entry : archive.entries() ) {
            if( entry.name.startsWith( 'META-INF/maven/' ) && entry.name.endsWith( '/pom.properties' ) ) {
                Properties p = new Properties()
                archive.withInputStream( entry ) {
                    p.load( it )
                }
                
                String pomVersion = p.getProperty( 'version' )
                if( pomVersion ) {
                    version = pomVersion
                }
                
                // Should appear only once :-/ Check?
                break
            }
        }
        
    }
    
    boolean isSourceBundle() {
        ManifestElement[] sourceBundleFor = parseAttribute( 'Eclipse-SourceBundle' )
        if( !sourceBundleFor ) {
            return false
        }
                    
        importSourceBundle( manifest, sourceBundleFor )
        return true
    }
    
    boolean isSingleton
    String licenseURL
    String eclipseKey
    
    Map licenseNameMap = [
        'http://www.apache.org/licenses/LICENSE-2.0.txt': 'Apache 2'
    ]
    
    void examineManifest() {
//        println manifest.entries
        
        log.debug( "Attributes of ${bundle}" )
        manifest.attr.each {
            log.debug( "    ${it.key}: ${it.value}" )
        }
        
        def attrs = parseAttribute( Constants.BUNDLE_SYMBOLICNAME )
        assert attrs.size() == 1
        
        artifactId = attrs[0].value
        version = manifest.attr.getValue( Constants.BUNDLE_VERSION )
        groupId = artifactIdToGroupId( artifactId )
        
        eclipseKey = "${artifactId}:${version}"
        
        isSingleton = ( 'true' == attrs[0].getDirective( Constants.SINGLETON_DIRECTIVE ) )
        
        attrs = parseAttribute( 'Bundle-License' )
        if( attrs && attrs.size() == 1 ) {
            licenseURL = attrs[0].value
        }
    }
    
    void unpackNestedJar( String nestedJarPath, File jarFile ) {
        
        if( nestedJarPath.contains( ',' ) ) {
            String msg = "Multiple nested JARs are not supported; just copying the original bundle"
            Map xml = [ jar: jarFile.absolutePath, nestedJarPath: nestedJarPath ]
            installCmd.warn( Warning.MULTIPLE_NESTED_JARS, msg, xml )
            bundle.copy( jarFile )
            return
        }
        
        def entry = archive[ nestedJarPath ]
        if( null == entry ) {
            throw new RuntimeException( "Can't find [${nestedJarPath}] in ${bundle}" )
        }
        
        jarFile.parentFile?.makedirs()
        
        archive.withInputStream( entry ) {
            jarFile << it
        }
    }
    
    String artifactIdToGroupId( String artifactId ) {
        def parts = artifactId.split( '\\.', -1 )
        def n = Math.min( parts.size()-1, 2 )
        return parts[0..n].join( '.' )
    }
    
    String indent = '  '
    
    void createPom( Writer writer ) {
        
        statistics.pomCount ++
        
        writer << '<?xml version="1.0" encoding="UTF-8"?>\n'
        writer << '<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"\n'
        writer << indent*2 << 'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n'
        writer << indent << '<modelVersion>4.0.0</modelVersion>\n'

        writer << indent << '<groupId>' << groupId << '</groupId>\n'
        writer << indent << '<artifactId>' << artifactId << '</artifactId>\n'
        writer << indent << '<version>' << version << '</version>\n'
        
        String name = manifest.attr.getValue( Constants.BUNDLE_NAME )
        if( name ) {
            if( name.contains( '%' ) ) {
                name = expand( name )
            }
            
            writer << indent << '<name>' << escape( name ) << '</name>\n'
        }
        
        String description = manifest.attr.getValue( Constants.BUNDLE_DESCRIPTION )
        if( description ) {
            if( description.contains( '%' ) ) {
                description = expand( description )
            }
        } else {
            description = ''
        }
        
        writer << indent << '<description>' << escape( description ) << ( description ? "\n    " : "" ) << 'Converted with MT4E ' << Tool.VERSION << '</description>\n'
        
        String url = manifest.attr.getValue( Constants.BUNDLE_DOCURL )
        if( url ) {
            writer << indent << '<url>' << escape( url ) << '</url>\n'
        }
        
        if( licenseURL ) {
            writer << indent << '<licenses>\n'
            writer << indent*2 << '<license>\n'
            
            String licName = licenseNameMap[ licenseURL ]
            if( licName ) {
                writer << indent*3 << '<name>' << escape( licName ) << '</name>\n'
            }
            
            writer << indent*3 << '<url>' << escape( licenseURL ) << '</url>\n'
            
            writer << indent*2 << '</license>\n'
            writer << indent << '</licenses>\n'
        }
        
        addProperties( writer )
        
        def requiredBundles = parseAttribute( Constants.REQUIRE_BUNDLE )
//        println "requiredBundles=${requiredBundles}"
        if( requiredBundles ) {
            addDependencies( writer, requiredBundles )
        }
        
        writer << '</project>\n'
    }
    
    void addProperties( Writer writer, ManifestElement[] requiredBundles ) {

        def imports = parseAttribute( Constants.IMPORT_PACKAGE )
        def exports = parseAttribute( Constants.EXPORT_PACKAGE )
//        println "imports=${imports}"
//        println "exports=${exports}"
        if( !imports && !exports && !isSingleton ) {
            return
        }

        writer << indent << '<properties>\n'
        
        if( imports ) {
            String value = imports.join( ',' )
            addProperty( writer, Pom.IMPORT_PACKAGE_PROPERTY, value )
        }
        
        if( exports ) {
            String value = exports.join( ',' )
            addProperty( writer, Pom.EXPORT_PACKAGE_PROPERTY, value )
        }

        if( isSingleton ) {
            addProperty( writer, Pom.IS_SINGLETON_PROPERTY, 'true' )
        }
        
        writer << indent << '</properties>\n'
    }
    
    void addProperty( Writer writer, String name, String value ) {
        writer << indent*2 << '<' << name << '>' << escape( value ) << '</' << name << '>\n'
    }
    
    void addDependencies( Writer writer, ManifestElement[] requiredBundles ) {
        
        writer << indent << '<dependencies>\n'
        
        requiredBundles.each {
            addDependency( writer, it )
        }
        
        writer << indent << '</dependencies>\n'
    }
    
    void addDependency( Writer writer, ManifestElement dep ) {
//        println dep
        
        String artifactId = dep.value
        String version = dep.getAttribute( Constants.BUNDLE_VERSION_ATTRIBUTE )
        if( !version ) {
            version = '[0,)'
        } else if ( !version.startsWith( '[' ) && !version.startsWith( '(' ) ) {
            version = "[${version},)"
        }
        
        String groupId = artifactIdToGroupId( artifactId )
        
        writer << indent*2 << '<dependency>\n'
        writer << indent*3 << '<groupId>' << groupId <<  '</groupId>\n'
        writer << indent*3 << '<artifactId>' << artifactId <<  '</artifactId>\n'
        writer << indent*3 << '<version>' << version <<  '</version>\n'
        
        String resolution = dep.getDirective( Constants.RESOLUTION_DIRECTIVE )
        if( Constants.RESOLUTION_OPTIONAL == resolution ) {
            writer << indent*3 << '<optional>true</optional>\n'
        }
        
        writer << indent*2 << '</dependency>\n'
    }
    
    Properties pluginProperties
    
    String expand( String variable ) {
        loadPluginProperties()
        
        if( ! variable.startsWith( '%' ) ) {
            throw new RuntimeException( 'Expected "%" as the first character in ' + variable )
        }
        
        String value = pluginProperties.getProperty( variable.substring( 1 ) )
        return value ? value : variable
    }
    
    void loadPluginProperties() {
        
        if( null != pluginProperties ) {
            return
        }
        
        pluginProperties = new Properties()
        
        if( archive ) {
            def entry = archive[ 'plugin.properties' ]
            if( entry ) {
                archive.withInputStream( entry ) {
                    pluginProperties.load( it )
                }
            }
            
            entry = archive[ 'OSGI-INF/l10n/bundle.properties' ]
            if( entry ) {
                archive.withInputStream( entry ) {
                    pluginProperties.load( it )
                }
            }
            
        } else {
            File file = new File( bundle, 'plugin.properties' )
            if( file.exists() ) {
                file.withInputStream { stream ->
                    pluginProperties.load( stream )
                }
            }
        }
    }
    
    String escape( String text ) {
        return text.replace( '&', '&amp;' ).replace( '<', '&lt;' ).replace( '>', '&gt;' )
    }
    
    void importSourceBundle( Manifest manifest, ManifestElement[] sourceBundleFor ) {
        
        statistics.sourceCount ++
        
        if( sourceBundleFor.size() != 1 ) {
            throw new RuntimeException( "Expected exactly one element in ${sourceBundleFor}" )
        }
        def attr = sourceBundleFor[0]
        
        log.debug( "Found source bundle for ${attr.value}" )
        
        artifactId = attr.value
        version = attr.getAttribute( Constants.VERSION_ATTRIBUTE )
        groupId = artifactIdToGroupId( artifactId )
        
        String eclipseKey = "${artifactId}:${version}"
        File mavenSourceJar = versionMap.binaryMap[ eclipseKey ]
        
        if( mavenSourceJar ) {
            log.debug( "Found ${eclipseKey}: ${mavenSourceJar}" )
        } else {
            String key = "${groupId}:${artifactId}:${version}"
            mavenSourceJar = MavenRepositoryTools.buildPath( m2repo, key, "jar", "sources" )
            
            versionMap.unknownMap[ eclipseKey ] = mavenSourceJar
            log.debug( "${eclipseKey} is unknown" )
        }
        
        String roots = attr.getDirective( 'roots' )
        if( !roots ) {
            assert bundle.isFile()
            
            bundle.copy( mavenSourceJar )
            return
        }
        
        mavenSourceJar.parentFile?.makedirs()

        manifest.entries.clear()
        
        roots += '/'
        mavenSourceJar.withOutputStream { it ->
            def out = new ZipOutputStream( it )
            
            filterSourceBundle( out, roots )
            
            out.close()
        }
    }
    
    void filterSourceBundle( ZipOutputStream out, String roots ) {
        assert archive != null
        
        archive.eachEntry { ZipEntry entry ->
            String name = entry.name
            
            if( 'META-INF/MANIFEST.MF' == name ) {
                writeManifest( out )
                return
            }
            
            if( name.startsWith( roots ) ) {
                name = name.substring( roots.size() )
            }
            
            ZipEntry clone = new ZipEntry( name )
            clone.time = entry.time
            clone.comment = entry.comment
            clone.extra = entry.extra
            
            out.putNextEntry( clone )
            
            archive.withInputStream( entry ) {
                out << it
            }
            
            out.closeEntry()
        }
    }
    
    void writeManifest( ZipOutputStream out ) {
        def entry = new ZipEntry( 'META-INF/MANIFEST.MF' )
        out.putNextEntry( entry )
        manifest.write( out )
        out.closeEntry()
    }
    
    void close() {
        if( archive ) {
            archive.close()
        }
    }
    
    ZipFile archive
    
    Manifest loadManifestFromJar( File file ) {
        archive = new ZipFile( file )
        
        def entry = archive[ 'META-INF/MANIFEST.MF' ]
        if( !entry ) {
            String msg = "Can't find manifest in ${file.absolutePath}"
            installCmd.error( Error.MISSING_MANIFEST, msg, [ jar: file.absolutePath ] )
            return null
        }
        
        def m
        archive.withInputStream( entry ) {
            m = new Manifest( it )
        }
        return m
    }

    void importExplodedBundle( File bundleFolder ) {
        manifest = loadManifestFromFile( new File( bundleFolder, 'META-INF/MANIFEST.MF' ) )
        if( !manifest ) {
            return
        }
        
        statistics.bundleCount ++
        
        log.debug( 'Importing {}', bundleFolder )
        bundle = bundleFolder
        
        examineManifest()
        
        if( isSourceBundle() ) {
            return
        }
        
        String key = "${groupId}:${artifactId}:${version}"
        log.info( "Importing ${key}" )
        File jarFile = MavenRepositoryTools.buildPath( m2repo, key, 'jar' )
        
        String classPath = manifest.attr.getValue( 'Bundle-ClassPath' )
        if( classPath ) {
            File nestedJar = new File( bundleFolder, classPath )
            
            nestedJar.copy( jarFile )
        } else {
            packBundle( bundleFolder, jarFile )
        }
        
        statistics.jarCount ++
        
        File pomFile = MavenRepositoryTools.buildPath( m2repo, key, 'pom' )
        pomFile.withWriter( 'UTF-8' ) {
            createPom( it )
        }
    }
    
    void packBundle( File bundleFolder, File jarFile ) {
        
        jarFile.parentFile?.makedirs()
        
        jarFile.withOutputStream {
            def out = new ZipOutputStream( it )
        
            bundleFolder.eachFileRecurse { File file ->
                String name = file.pathRelativeTo( bundleFolder )
                
                def entry = new ZipEntry( name )
                entry.setTime( file.lastModified() )
                
                out.putNextEntry( entry )
                
                out << file
                
                out.closeEntry()
            }
            
            out.close()
        }
    }
    
    Manifest loadManifestFromFile( File file ) {
        if( !file.exists() ) {
            String msg = "Can't find manifest ${file.absolutePath}"
            installCmd.error( Error.MISSING_MANIFEST, msg, [ jar: file.absolutePath ] )
            return null
        }
        
        def m
        file.withInputStream {
            m = new Manifest( it )
        }
        
        return m
    }
    
    ManifestElement[] parseAttribute( String name ) {
        String text = manifest.attr.getValue( name )
        if( !text ) {
            return null
        }
        
        return ManifestElement.parseHeader( name, text )
    }
}
