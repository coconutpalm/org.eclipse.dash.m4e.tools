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

class AttachSourcesCmd extends AbstractCommand {
    
    final static String DESCRIPTION = '''directories...\n- Source for source JARs and move them in the right place for Maven 2'''
    
    int count

    void run( String... args ) {
        String[] dirs = args[1..-1]
        log.debug( "Directories: ${dirs}" )
        
        for( String path in dirs ) {
            File dir = new File( path ).absoluteFile
            
            attachSources( dir )
        }
    }
    
    File target
    
    void attachSources( File dir ) {
        if( !dir.exists() ) {
            throw new UserError( "${dir} doesn't exist" )
        }
        
        if( !dir.isDirectory() ) {
            throw new UserError( "${dir} isn't a directory" )
        }
        
        log.info( "Attaching sources in ${dir}..." )
        count = 0

        target = dir
        process( dir )        
        
        log.info( "Found ${count} sources JARs" )
    }
    
    void process( File dir ) {
        dir.eachFile { File it ->
            if( it.isDirectory() ) {
                if( it.name.endsWithOneOf( '.source', '.sources' ) ) {
                    processSourceFolder( it )
                } else {
                    process( it )
                }
            }
        }
    }
    
    void processSourceFolder( File srcPath ) {
        
        File binPath = binPathFromSource( srcPath )
        if( !binPath.exists() ) {
            warn( Warning.MISSING_BINARY_BUNDLE_FOR_SOURCES, "Missing bundle ${binPath} for sources in ${srcPath}" )
            return
        }
        
        boolean canDelete = true
        
        srcPath.eachFile { it ->
            if( !processSourceVersion( srcPath, binPath, it.name ) ) {
                canDelete = false
            }
        }
        
        if( canDelete ) {
            log.debug( "Deleting {}", srcPath )
            if( !srcPath.deleteDir() ) {
                throw new RuntimeException( "Failed to delete ${srcPath}" )
            }
        } else {
            log.debug( "There were unexpected files in {} -> not deleting it", srcPath )
        }
    }
    
    File binPathFromSource( File srcPath ) {
        String bundleName = srcPath.name
        bundleName = bundleName.endsWith( '.source' ) ? bundleName.removeEnd( '.source' ) : bundleName.removeEnd( '.sources' )
        
        def parts = bundleName.split( '\\.', -1 )
        if( parts.size() > 3 ) {
            parts = parts[0..2]
        }
        
        File binPath = new File( target, parts.join( '/' ) )
        binPath = new File( binPath, bundleName )
    }
    
    boolean processSourceVersion( File srcPath, File binPath, String version ) {
        srcPath = new File( srcPath, version )
        binPath = new File( binPath, version )
        
        if( !binPath.exists() ) {
            warn( Warning.MISSING_BINARY_BUNDLE_FOR_SOURCES, "Missing bundle ${binPath} for sources in ${srcPath}" )
            return
        }
        
        boolean canDelete = true
        srcPath.eachFile { File it ->
            String name = it.name
            
            if( name.endsWith( '.pom' ) ) {
                log.debug( 'Deleting source POM {}', it )
                it.delete()
            } else if( name.endsWith( '.jar' ) ) {
                moveSource( srcPath, binPath, name )
            } else {
                warn( Warning.UNEXPECTED_FILE_IN_SOURCE_BUNDLE, "Unexpected file ${it} in source bundle ${srcPath}" )
                canDelete = false
            }
        }
        
        return canDelete
    }
    
    void moveSource( File srcPath, File binPath, String name ) {
        
        String version = srcPath.name
        String basename = srcPath.parentFile.name
        
        if( !basename.endsWith( '.source' ) && !basename.endsWith( '.sources' ) ) {
            throw new RuntimeException( "Unexpected file ${new File( srcPath, name)}" )
        }
        
        basename = basename.substringBeforeLast( '.' )
        
        String binName = "${basename}-${version}.jar"
        
        File binJar = new File( binPath, binName )
        if( !binJar.exists() ) {
            throw new RuntimeException( "Missing JAR ${binJar}" )
        }
        
        String targetName = "${basename}-${version}-sources.jar"
        File src = new File( srcPath, name )
        File target = new File( binPath, targetName )
        
        log.debug( "Moving ${src} to ${target}" )
        src.renameTo( target )
        
        count ++
    }
}
