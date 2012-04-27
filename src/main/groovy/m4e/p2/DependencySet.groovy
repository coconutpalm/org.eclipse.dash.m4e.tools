/*******************************************************************************
 * Copyright (c) 23.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e.p2

import java.io.File;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger
import org.slf4j.LoggerFactory;

class DependencySet {
    
    private static Logger log = LoggerFactory.getLogger( DependencySet )
    
    IP2Repo repo
    List<P2Bundle> bundles = []
    Map<String, P2Bundle> bundleById = [:]
    List<P2Dependency> unknown = []
    Set<String> unknownIds = new HashSet<String>()
    
    boolean add( P2Bundle bundle ) {
        //println "Adding ${bundle}"
        if( bundleById.containsKey( bundle.id ) ) {
            return false
        }
        
        bundles << bundle
        bundleById[bundle.id] = bundle
    }
    
    void addAll( P2Bundle bundle ) {
        if( !add( bundle ) ) {
            return
        }
        
        bundle.dependencies.each {
            def id = it.id
//            println "${id} ${it.type}"
            
            if( 'org.eclipse.update.feature' == it.type ) {
                return
            }
            
            if( bundleById.containsKey( id ) ) {
                return
            }
            
            def dep = repo.latest( id, it.versionRange )
            
            if( dep ) {
                addAll( dep )
            } else {
                def latest = repo.latest( id )
                
                if( latest ) {
                    throw new P2Exception( "Unable to find dependency ${id} for ${bundle.id} with version range ${it.versionRange}. Latest version is ${latest.version}" )
                }
                
                if( unknownIds.add( id ) ) {
                    unknown << it
                }
            }
        }
    }
    
    int size() {
        return bundles.size()
    }
    
    void download( File dest ) {
        bundles.each {
            URL url = it.downloadURL( repo.url )
            String fileName = url.path.substringAfterLast( "/" )
            if( !fileName ) {
                fileName = url.path
            }
            
            File cacheDir = new File( repo.workDir, 'p2/cache' )
            File cached = new File( cacheDir, fileName )
            cacheDir.makedirs()
            
            URL packed = new URL( "${url.toExternalForm()}.pack.gz" )
            try {
                File packedFile = repo.downloader.download( packed )
                
                if( !cached.exists() || packedFile.lastModified() > cached.lastModified() ) {
                    File tmp = new File( cacheDir, 'download.tmp' )
                    unpack( packedFile, tmp )
                
                    assert tmp.renameTo( cached )
                }
            } catch( FileNotFoundException e ) {
                cached = repo.downloader.download( url )
            }
            
            String path = ( it instanceof P2Feature ) ? 'features' : 'plugins'
            File dir = new File( dest, path )
            File result = new File( dir, fileName )
            log.info( "Downloaded to ${result}" )
            
            cached.copy( result )
        }
    }
    
    void unpack( File packedFile, File unpackedFile ) {
        
        if( unpackedFile.exists() ) {
            unpackedFile.usefulDelete()
        }
        
        unpackedFile.withOutputStream { out ->
            out = new JarOutputStream( out )
            
            packedFile.withInputStream { stream ->
                stream = new GZIPInputStream( stream )
                
                def unpacker = Pack200.newUnpacker()
                unpacker.unpack( stream, out )
            }
        }
    }
    
    void resolveDependencies( String id, Version version = null ) {
        P2Bundle root
        if( version ) {
            root = repo.find( id, version )
            
            if( !root ) {
                P2Bundle latest = repo.latest( id )
                
                if( latest ) {
                    throw new P2Exception( "Unable to find bundle ${id} with version ${version}, the latest version is ${latest.version}" )
                }
            }
        } else {
            root = repo.latest( id )
        }
        
        if( !root ) {
            throw new P2Exception( "Unable to find bundle ${id}" )
        }
        
        addAll( root )
    }
}
