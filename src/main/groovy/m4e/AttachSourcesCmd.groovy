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
    
    int count

    void run( String... args ) {
        String[] dirs = args[1..-1]
        log.debug( "Directories: ${dirs}" )
        
        for( String path in dirs ) {
            File dir = new File( path ).absoluteFile
            
            attachSources( dir )
        }
    }
    
    void attachSources( File dir ) {
        if( !dir.exists() ) {
            throw new UserError( "${dir} doesn't exist" )
        }
        
        if( !dir.isDirectory() ) {
            throw new UserError( "${dir} isn't a directory" )
        }
        
        log.info( "Attaching sources in ${dir}..." )
        count = 0

        process( dir )        
        
        log.info( "Found ${count} sources JARs" )
    }
    
    void process( File dir ) {
        dir.eachFile { File it ->
            if( it.isDirectory() ) {
                if( it.name.endsWith( '.source' ) ) {
                    processSourceFolder( it )
                } else {
                    process( it )
                }
            }
        }
    }
    
    void processSourceFolder( File srcPath ) {
        File binPath = new File( srcPath.absolutePath.removeEnd( '.source' ) )
        
        if( !binPath.exists() ) {
            log.warn( "Missing ${binPath}" )
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
    
    boolean processSourceVersion( File srcPath, File binPath, String version ) {
        srcPath = new File( srcPath, version )
        binPath = new File( binPath, version )
        
        if( !binPath.exists() ) {
            log.warn( "Missing ${binPath}" )
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
                log.warn( "Unexpected file {}", it )
                canDelete = false
            }
        }
        
        return canDelete
    }
    
    void moveSource( File srcPath, File binPath, String name ) {
        // name = org.eclipse.core.runtime.source-3.6.0.jar
        int pos1 = name.lastIndexOf( '-' )
        int pos2 = name.lastIndexOf( '.' )
        
        String version = name[pos1+1..<pos2]
        String basename = name[0..<pos1]
        
        if( !basename.endsWith( '.source' ) ) {
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
