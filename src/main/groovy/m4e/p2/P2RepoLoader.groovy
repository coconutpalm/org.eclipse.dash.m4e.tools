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

import groovy.util.Node;

import java.io.File;
import java.net.URL;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class P2RepoLoader {
    
    static final Logger log = LoggerFactory.getLogger( P2RepoLoader )
    
    File workDir
    private URL url
    
    void setUrl( URL url ) {
        if( !url.path.endsWith( '/' ) ) {
            url = new URL( url.toExternalForm() + '/' )
        }
        this.url = url
    }
    
    URL getUrl() {
        return url
    }

    IP2Repo load() {
        return load( new Downloader( cacheRoot: new File( workDir, 'p2' ) ) )
    }
    
    Downloader downloader
    
    IP2Repo load( Downloader downloader ) {
        
        this.downloader = downloader
        
        File contentXmlFile
        
        try {
            def p2indexFile = downloader.download( new URL( url, 'p2.index' ) )
            
            def p2index = new P2Index( p2indexFile )
            
            return loadP2Index( p2index )
        } catch( P2DownloadException e ) {
            return loadContent()
        }
    }
    
    IP2Repo loadContent() {
        File contentXmlFile
        
        try {
            def contentJarFile = downloader.download( new URL( url, 'content.jar' ) )
            
            contentXmlFile = unpackContentJar( contentJarFile )
        } catch( P2DownloadException e ) {
            
            try {
                contentXmlFile = downloader.download( new URL( url, 'content.xml' ) )
            } catch( P2DownloadException e2 ) {
                // Last frantic attempt. Example: http://download.eclipse.org/modeling/emft/mwe/updates/releases/helios/compositeContent.xml
                return loadCompositeContentXmlFile( 'compositeContent.jar', 'compositeContent.xml' )
            }
        }
        
        P2Repo repo = new P2Repo( workDir: workDir, url: url )
        repo.downloader = downloader
        
        log.debug( 'Parsing {}', contentXmlFile )
        def parser = new ContentXmlParser( repo: repo )
        parser.parseXml( contentXmlFile )
        
        return repo
    }
    
    IP2Repo loadP2Index( P2Index p2index ) {
        
        return loadCompositeContentXmlFile( p2index.contentJarName, p2index.contentXmlName )
    }
    
    IP2Repo loadCompositeContentXmlFile( contentJarName, contentXmlName ) {
        File contentXmlFile
        
        try {
            def contentJarFile = downloader.download( new URL( url, contentJarName ) )
            
            contentXmlFile = unpackContentJar( contentJarFile )
        } catch( P2DownloadException e2 ) {
            contentXmlFile = downloader.download( new URL( url, contentXmlName ) )
        }
        
        P2Repo repo = new P2Repo( workDir: workDir, url: url )
        repo.downloader = downloader
        
        log.debug( 'Parsing {}', contentXmlFile )
        def parser = new ContentXmlParser( repo: repo )
        try {
            parser.parseXml( contentXmlFile )
        } catch( CompositeRepoException e ) {
            return loadCompositeRepo( contentXmlFile )
        }
        
        return repo
    }
    
    IP2Repo loadCompositeRepo( File compositeContentXmlFile ) {
        
        def doc = new XmlParser().parse( compositeContentXmlFile )
        
        try {
            return parse( doc )
        } catch( Exception e ) {
            throw new P2Exception( "Error parsing ${compositeContentXmlFile}: ${e}", e )
        }
    }
    
    IP2Repo parse( Node doc ) {
        
        def result = new MergedP2Repo( url: url )
        
        def children = doc.get( 'children' )
        
        for( Node node : children.child ) {
            def location = node.'@location'
            
            P2RepoLoader sub = new P2RepoLoader( workDir: workDir, url: new URL( url, location ) )
            result.add( sub.load() )
        }
        
        return result
    }
    
    private File unpackContentJar( File contentJarFile ) {
        
        String name = contentJarFile.name.removeEnd( '.jar' ) + '.xml'
        File contentXmlFile = new File( contentJarFile.parentFile, name )
        
        if( contentXmlFile.exists() ) {
            if( contentXmlFile.lastModified() >= contentJarFile.lastModified() ) {
                return contentXmlFile
            }
            
            contentXmlFile.usefulDelete()
        }
        
        def jar = new JarFile( contentJarFile )
        def entry = jar.getEntry( name )
        if( ! entry ) {
            throw new P2Exception( "Missing ${name} in ${contentJarFile}" )
        }
        def input = jar.getInputStream( entry )

        input.withStream { it ->
            contentXmlFile << it
        }
        
        return contentXmlFile
    }
}

class CompositeRepoException extends RuntimeException {}
