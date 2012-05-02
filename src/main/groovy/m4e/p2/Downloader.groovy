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
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class Downloader {
    
    static final Logger log = LoggerFactory.getLogger( Downloader )
    
    File cacheRoot
    
    File urlToPath( URL url ) {
        File path = new File( cacheRoot, url.host )
        
        if( url.port != 80 && url.port != -1 ) {
            path = new File( path, "${url.port}" )
        }
        
        path = new File( path, url.path )
        return path
    }

    File download( URL url ) {
        File file = urlToPath( url )
        def meta = DownloadMetadata.load( file )
        
        if( !meta.needsRefresh ) {
            if( meta.fileNotFound ) {
                throw new P2DownloadException( meta.fileNotFound )
            }
            
            log.info( "Using cached version of ${url}" )
            return file
        }
        
        List<URL> urls = locateMirror( url )
        
        boolean success = false
        int i = 0
        while( !success && i < urls.size() ) {
            def url2 = urls[ i ++ ]
            log.info( "Downloading ${url2} to ${file}..." )
            
            URLConnection conn = url2.openConnection()
            try {
                conn.connect()
            } catch( SocketException e ) {
                log.warn( "Unable to connect to ${url2}: ${e.message}", e )
                continue
            }
            
            def value = conn.getHeaderField("Content-Length")
            long contentLength = value ? Long.parseLong( value ) : 0
            Progress p = progressFactory.newProgress( contentLength )
            log.info( 'Size: {} bytes = {} kb', p.size, p.sizeInKB )
            
            try {
                doDownload( url2, file, p )
                
                meta.success()
                return file
            } catch( FileNotFoundException e ) {
                continue
            } catch( IOException e ) {
                String message = e.message
                if( message.startsWith ( 'Server returned HTTP response code: 503' ) ) {
                    log.debug( "Mirror ${url2} failed with: ${message}" )
                    continue
                }
                
                throw e
            }
        }
        
        def e = new P2DownloadException( url, urls )
        meta.failed( e )
        throw e
    }
    
    void doDownload( URL url, File file, Progress p ) {
        
        p.update( 0 )
        
        File tmp = new File( "${file.absolutePath.removeEnd('.jar')}.tmp" )
        File dir = tmp.parentFile
        if( dir ) {
            dir.makedirs()
        }
        
        url.withInputStream { input ->
            byte[] buffer = new byte[10240]
            
            tmp.withOutputStream { output ->
                int len
                while( ( len = input.read(buffer) ) > 0 ) {
                    p.update( len )
                    
                    output.write( buffer, 0, len )
                }
            }
        }
        
        p.close()
        
        tmp.renameTo( file )
    }
    
    List<URL> locateMirror( URL url ) {
        if( 'http' != url.protocol ) {
            return [ url ]
        }
        
        if( -1 != url.port ) {
            return [ url ]
        }
        
        if( 'download.eclipse.org' != url.host ) {
            return [ url ]
        }
        
        String path = URLEncoder.encode( url.path, 'UTF-8' )
        URL mirrorList = new URL( "http://www.eclipse.org/downloads/download.php?file=${path}&protocol=http&format=xml" )
        
        def doc
        mirrorList.withInputStream {
            doc = new XmlParser().parse( it )
        }
        
        NodeList mirrors = doc.mirror
        if( !mirrors ) {
            return [ url ]
        }
        
        log.debug( "Found ${mirrors.size()} mirrors." )
        def result = []
        if( mirrors.size() > 5 ) {
            mirrors = mirrors.subList( 0, 5 )
        }
        mirrors.each {
            result << new URL( it.'@url' )
        }
        result << url
        return result
    }
    
    ProgressFactory progressFactory = new ProgressFactory()
}

