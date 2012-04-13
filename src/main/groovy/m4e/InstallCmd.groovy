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

import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

class InstallCmd extends AbstractCommand {

    final static String DESCRIPTION = '''archives...\n- Extract the specified archives and convert the Eclipse plug-ins inside into Maven artifacts'''
    
    static final String MVN_VERSION = '3.0.4'
    def m3archive = "apache-maven-${MVN_VERSION}-bin.zip"
    def m3home
    def m3exe
    def templateRepo

    void run( String... args ) {
        setup()
        
        // args[1..-1] throws "IndexOutOfBoundsException: toIndex = 2" if array has only one element
        if( args.size() > 1 ) {
            for( String archive in args[1..-1] ) {
                importArchive( archive )
            }
        }
        
        log.info( 'Import complete' )
    }
    
    void setup() {
        m3home = new File( workDir, "apache-maven-${MVN_VERSION}" )
        m3exe = new File( new File( m3home, 'bin' ), 'mvn' )

        downloadMaven3()
        unpackMaven3()
        loadNecessaryPlugins()
    }
    
    List<File> m2repos = []
    
    void importArchive( String url ) {
        
        File archive = downloadArchive( url )
        File path = unpackArchive( archive )
        
        ImportTool tool = importIntoTmpRepo( path )
        
        File m2repo = tool.m2repo
        
        log.info( 'Deleting non-Eclipse artifacts...' )
        deleteCommonFiles( m2repo, templateRepo )
        log.info( 'OK' )
        
        deleteMavenFiles( m2repo )
        
        m2repos << m2repo
    }
    
    boolean deleteCommonFiles( File path1, File path2 ) {
        List names = []
        path1.eachFile { it -> names << it.name }
        names.sort()
        
        Set toDelete = [] as Set
        path2.eachFile { it -> toDelete << it.name }
        
        boolean isEmpty = true
        
        for( String name in names ) {
            if( !toDelete.contains( name ) ) {
                isEmpty = false
                continue
            }
            
            File path = new File( path1, name )
            if( path.isDirectory() ) {
                boolean empty = deleteCommonFiles( path, new File( path2, name ) )
                
                if( empty ) {
                    log.trace( 'Deleting empty directory {}', path )
                    path.delete()
                } else {
                    isEmpty = false
                }
            } else {
                log.trace( 'Deleting file {}', path )
                path.delete()
            }
        }
        
        return isEmpty
    }

    Set mavenFiles = [ 'maven-metadata-local.xml', '_maven.repositories' ] as Set
    
    void deleteMavenFiles( File path ) {
        path.eachFile() { it ->
            if( it.isDirectory() ) {
                deleteMavenFiles( it )
            } else if( mavenFiles.contains( it.name ) ){
                it.delete()
            }
        }
    }
    
    /** 
     * We want to avoid downloading the Maven plug-ins all the time.
     * 
     * Therefore, we create a template repository which contains the
     * necessary plug-ins, so we can copy them later.
     */
    void loadNecessaryPlugins() {
        def primingArchive = new File( workDir, 'priming.zip' )
        extractPrimingArchive( primingArchive )
        
        File priming_home = new File( workDir, 'priming_home' )
        templateRepo = new File( priming_home, 'm2repo' )
        File failure = new File( priming_home, ImportTool.FAILURE_FILE_NAME )

        if( failure.exists() ) {
            log.info( 'Last import failed, trying again' )
        } else if( templateRepo.exists() ) {
            log.debug( 'Priming repo already exists' )
            return
        }

        log.info( 'Caching necessary plug-ins for Maven 3' )

        File archive = downloadArchive( primingArchive.absolutePath )
        File path = unpackArchive( archive )

        importIntoTmpRepo( path )
        
        // Try again if the code below fails
        failure << 'Cleanup failed'
        
        log.info( 'Preparing priming repo' )
        
        File orgDir = new File( templateRepo, 'org' )
        File eclipseDir = new File( orgDir, 'eclipse' )
        
        // Save one JAR which the Maven Eclipse Plugin needs
        File backupDir = new File( templateRepo.parentFile, 'backup' )
        backupDir = new File( backupDir, 'resources' )
        
        File source = new File( new File( eclipseDir, 'core' ), 'resources' )
        if( !backupDir.exists() ) {
            source.copy( backupDir )
        }
        
        // Delete everything
        eclipseDir.deleteDir()
        
        // Restore what we saved above
        backupDir.copy( source )
        
        log.info('OK')
        
        failure.delete()
    }

    void extractPrimingArchive( File primingArchive ) {
        if( primingArchive.exists () ) {
            return
        }
        
        def resource = 'priming.zip'
        def url = getClass().getResource( resource )
        if( !url ) {
            throw new IOException( "Can't find resource [${resource}] relative to ${getClass().name}" )
        }
        
        url.withInputStream { stream ->
            primingArchive << stream
        }
    }
    
    ImportTool importIntoTmpRepo( File path ) {
        def tool = new ImportTool( templateRepo: templateRepo, m3exe: m3exe, m3home: m3home )
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

    /** Unpack the downloaded Maven 3 archive */
    void unpackMaven3() {
        def archivePath = new File( workDir, m3archive )
        def unpackedPath = "apache-maven-${MVN_VERSION}"

        def path = new File( workDir, unpackedPath )
        if( path.exists() ) {
            log.debug( 'Maven 3 was already unpacked at {}', path )
            return
        }

        log.info('Unpacking Maven 3 archive')
        archivePath.unzip( workDir )
        log.info('OK')
    }

    /** Download Maven 3 if necessary */
    void downloadMaven3() {
        def path = new File( workDir, m3archive )
        if( path.exists() ) {
            log.debug( "Maven ${MVN_VERSION} was already downloaded at ${path}" )
            return
        }

        // TODO Find closer mirror
        def downloadUrl = 'http://mirror.switch.ch/mirror/apache/dist/maven/binaries/' + m3archive

        log.info( 'Downloading Maven 3...' )
        download( downloadUrl, path )
        log.info( 'OK' )
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

class ImportTool {

    static final Logger log = LoggerFactory.getLogger( ImportTool )
    
    static boolean DEBUG = true
    
    static final String FAILURE_FILE_NAME = 'failure'

    File templateRepo
    File m3exe
    File m3home

    File eclipseFolder
    File tmpHome
    File m2repo
    File failure
    File m2settings
    File logFile

    void run( File path ) {
        assert path != null
        assert templateRepo != null
        assert m3exe != null
        assert m3home != null

        eclipseFolder = locate( path, 'plugins' )
        if( !eclipseFolder ) {
            throw new RuntimeException( "Can't find plugins folder below ${path.absolutePath}" )
        }
        eclipseFolder = eclipseFolder.parentFile
        assert eclipseFolder != null

        tmpHome = new File( path.absolutePath + '_home' )
        m2repo = new File(tmpHome, 'm2repo')
        logFile = new File(tmpHome, 'm2repo.log')
        failure = new File(tmpHome, FAILURE_FILE_NAME)
        
        log.debug( "Importing plug-ins from ${eclipseFolder} into repo ${m2repo}" )
        clean()

        log.info('Analysing Eclipse plugins...')
        doImport()
        log.info('OK')
        
        failure.delete()
    }
    
    void doImport() {
        def cmd = [
            '/bin/sh',
            m3exe.toString(),
            'eclipse:make-artifacts',
            '-DstripQualifier=true',
            "-DeclipseDir=${eclipseFolder}",
            "-Dmaven.repo.local=${m2repo}",
        ]
        
        if( DEBUG ) {
            cmd << '-X'
        }
        
        String javaHome = System.getProperty( 'java.home' )
        Process p = cmd.execute( [ "M2_HOME=${m3home.absolutePath}", "JAVA_HOME=${javaHome}" ], null )
        
        ProcessGroovyMethods.consumeProcessErrorStream( p, System.err )
        
        logFile.withPrintWriter() { it ->
            processOutput( p.inputStream, it )
        }
        
        int rc = p.waitFor()
        if( rc != 0 ) {
            log.error( "Invoking Maven with ${cmd} failed with ${rc}. See logfile ${logFile} for details." )
            throw new RuntimeException( "Importing the plug-ins from ${eclipseFolder} failed with error code ${rc}. See logfile ${logFile} for details." )
        }
    }
    
    void processOutput( InputStream stream, PrintWriter logFile ) {
        def reader = new BufferedReader( new InputStreamReader( stream, 'UTF-8' ) )
        
        def min
        def max = null
        
        int consoleWidth = ConsoleUtils.consoleWidth();
        String spaces = ' ' * consoleWidth
        int prefixLength = m2repo.toString().size() + 1
        
        for( String line in reader ) {
            logFile.println( line )
            
            if( line.startsWith( '[INFO] Processing ' ) ) {
                def parts = line.split(' ')
                if( parts[2] != 'file' ) {
                    min = parts[2]
                    max = parts[4].trim()
                }
            } else if( line.startsWith( '[INFO] Installing ') && line.endsWith( '.jar' ) ) {
                def parts = line.split(' ')
                def path = parts[-1]
//                println path
                path = path[prefixLength..-1]
                
                File file = new File( path )
                File dir = file.parentFile
                
                def version = dir.name
                
                dir = dir.parentFile
                
                def artifactId = dir.name
                
                dir = dir.parentFile
                def groupId = dir.path
                groupId = groupId.replaceAll( '[/\\\\]', "." )
                
                def msg1 = "Installing ${min} of ${max} "
                def msg2 = "${groupId}:${artifactId}:${version}"
                
                if( msg1.size() + msg2.size() > consoleWidth - 1 ) {
                    def rest = consoleWidth - 1 - msg1.size()
                    msg2 = msg2[-rest..-1]
                }
                
                def msg = msg1 + msg2 + spaces
                msg = msg[0..consoleWidth-2] + '\r'
                
                System.out.print(msg)
                System.out.flush()
            }
        }
        
        if( max ) {
            System.out.println()
        }
    }

    /** Make sure we don't have any leftovers from previous attempts. */
    void clean() {
        if( tmpHome.exists() ) {
            log.info('Cleaning up from last run...')
            tmpHome.deleteDir()
        }

        tmpHome.makedirs()

        if( templateRepo.exists() ) {
            log.info('Copying template...')
            templateRepo.copy( m2repo )
        }
        
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