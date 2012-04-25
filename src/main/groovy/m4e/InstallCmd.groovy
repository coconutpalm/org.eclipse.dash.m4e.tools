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
    
    void run( String... args ) {
        // args[1..-1] throws "IndexOutOfBoundsException: toIndex = 2" if array has only one element
        if( args.size() > 1 ) {
            for( String archive in args[1..-1] ) {
                importArchive( archive )
            }
        }
        
        log.info( "Import complete. Imported ${statistics.bundleCount} Eclipse bundles into ${statistics.pomCount} POMs. There are ${statistics.jarCount} new JARs of which ${statistics.sourceCount} have sources" )
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

class ImportTool {

    static final Logger log = LoggerFactory.getLogger( ImportTool )
    
    static boolean DEBUG = true
    
    static final String FAILURE_FILE_NAME = 'failure'

    InstallCmd installCmd

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
        
        log.debug( "Importing plug-ins from ${eclipseFolder} into repo ${m2repo}" )
        clean()

        doImport()
        
        log.info( 'OK' )
        
        failure.delete()
    }
    
    void doImport() {
        
        // TODO convert features
        
        File pluginsFolder = new File( eclipseFolder, 'plugins' )
        doImport( pluginsFolder )
    }
    
    void doImport( File folder ) {
        folder.eachFile { it ->
            
            def tool = new BundleConverter( installCmd: installCmd, m2repo: m2repo, statistics: installCmd.statistics )
            
            if( it.isDirectory() ) {
                tool.importExplodedBundle( it )
            } else {
                tool.importBundle( it )
            }
            
            tool.close()
        }
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
    
    void importBundle( File bundleJar ) {
        this.bundle = bundleJar
        
        manifest = loadManifestFromJar( bundleJar )
        if( ! manifest ) {
            return
        }
        
        statistics.bundleCount ++
        
        log.debug( 'Importing {}', bundleJar )
        
        examineManifest()
        
        if( isSourceBundle() ) {
            return
        }
        
        String key = "${groupId}:${artifactId}:${version}"
        log.info( "Importing ${key}" )
        File jarFile = MavenRepositoryTools.buildPath( m2repo, key, 'jar' )
        
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
    
    boolean isSourceBundle() {
        ManifestElement[] sourceBundleFor = parseAttribute( 'Eclipse-SourceBundle' )
        if( !sourceBundleFor ) {
            return false
        }
                    
        importSourceBundle( manifest, sourceBundleFor )
        return true
    }
    
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
    }
    
    void unpackNestedJar( String nestedJarPath, File jarFile ) {
        
        if( nestedJarPath.contains( ',' ) ) {
            installCmd.warn( Warning.MULTIPLE_NESTED_JARS, "Multiple nested JARs are not supported; just copying the original bundle" )
            bundle.copy( jarFile )
            return
        }
        
        def entry = archive.getEntry( nestedJarPath )
        if( null == entry ) {
            throw new RuntimeException( "Can't find [${nestedJarPath}] in ${bundle}" )
        }
        
        jarFile.parentFile?.makedirs()
        
        def stream = archive.getInputStream( entry )
        try {
            jarFile << stream
        } finally {
            stream.close()
        }
    }
    
    String artifactIdToGroupId( String artifactId ) {
        def parts = artifactId.split( '\\.', -1 )
        def n = Math.min( parts.size()-1, 2 )
        return parts[0..n].join( '.' )
    }
    
    void createPom( Writer writer ) {
        
        statistics.pomCount ++
        
        writer << '<?xml version="1.0" encoding="UTF-8"?>\n'
        writer << '<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"\n'
        writer << '    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n'
        writer << '  <modelVersion>4.0.0</modelVersion>\n'

        writer << '  <groupId>' << groupId << '</groupId>\n'
        writer << '  <artifactId>' << artifactId << '</artifactId>\n'
        writer << '  <version>' << version << '</version>\n'
        
        String name = manifest.attr.getValue( 'Bundle-Name' )
        if( name ) {
            if( name.contains( '%' ) ) {
                name = expand( name )
            }
            
            writer << '  <name>' << escape( name ) << '</name>\n'
        }
        
        def requiredBundles = parseAttribute( Constants.REQUIRE_BUNDLE )
        if( requiredBundles ) {
            addDependencies( writer, requiredBundles )
        }
        
        writer << '</project>\n'
    }
    
    void addDependencies( Writer writer, ManifestElement[] requiredBundles ) {
        
        writer << '  <dependencies>\n'
        
        requiredBundles.each {
            addDependency( writer, it )
        }
        
        writer << '  </dependencies>\n'
    }
    
    void addDependency( Writer writer, ManifestElement dep ) {
//        println dep
        
        String artifactId = dep.value
        String version = dep.getAttribute( Constants.BUNDLE_VERSION_ATTRIBUTE )
        if( !version ) {
            version = '[0,)'
        }
        
        String groupId = artifactIdToGroupId( artifactId )
        
        writer << '    <dependency>\n'
        writer << '      <groupId>' << groupId <<  '</groupId>\n'
        writer << '      <artifactId>' << artifactId <<  '</artifactId>\n'
        writer << '      <version>' << version <<  '</version>\n'
        writer << '    </dependency>\n'
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
            def entry = archive.getEntry( 'plugin.properties' )
            if( !entry ) {
                return
            }
            
            def stream = archive.getInputStream( entry )
            try {
                pluginProperties.load( stream )
            } finally {
                stream.close()
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
        
        String key = "${groupId}:${artifactId}:${version}"
        File mavenSourceJar = MavenRepositoryTools.buildPath( m2repo, key, "jar", "sources" )
        
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
            
            writeManifest( out )
            
            filterSourceBundle( out, roots )
        }
    }
    
    void filterSourceBundle( ZipOutputStream out, String roots ) {
        assert archive != null
        
        for( ZipEntry entry: archive.entries() ) {
            if( entry.getName().startsWith( 'META-INF/' ) ) {
                continue
            }
            
            if( !entry.getName().startsWith( roots ) ) {
                out.putNextEntry( new ZipEntry( entry ) )
            } else {
                String name = entry.getName().substring( roots.size() )
                ZipEntry clone = new ZipEntry( name )
                clone.setTime( entry.getTime() )
                clone.setSize( entry.getSize() )
                clone.setComment( entry.getComment() )
                clone.setExtra( entry.getExtra() )
                
                out.putNextEntry( clone )
            }
            
            def stream = archive.getInputStream( entry )
            try {
                out << stream
            } finally {
                stream.close()
            }
        }
    }
    
    void writeManifest( ZipOutputStream out ) {
        def entry = new ZipEntry( 'META-INF/MANIFEST.MF' )
        out.putNextEntry( entry )
        manifest.write( out )
    }
    
    void close() {
        if( archive ) {
            archive.close()
        }
    }
    
    ZipFile archive
    
    Manifest loadManifestFromJar( File file ) {
        archive = new ZipFile( file )
        
        def entry = archive.getEntry( 'META-INF/MANIFEST.MF' )
        if( !entry ) {
            installCmd.error( Error.MISSING_MANIFEST, "Can't find manifest in ${file.absolutePath}" )
            return null
        }
        
        def stream = archive.getInputStream( entry )
        try {
            return new Manifest( stream )
        } finally {
            stream.close()
        }
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
            }
        }
    }
    
    Manifest loadManifestFromFile( File file ) {
        if( !file.exists() ) {
            installCmd.error( Error.MISSING_MANIFEST, "Can't find manifest ${file.absolutePath}" )
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
