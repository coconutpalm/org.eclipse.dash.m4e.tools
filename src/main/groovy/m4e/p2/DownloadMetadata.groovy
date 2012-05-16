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
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class DownloadMetadata {

    static final Logger log = LoggerFactory.getLogger( DownloadMetadata )
    
    static DownloadMetadata load( File file ) {
        File metaFile = new File( file.absolutePath + ".meta" )
        
        def result = new DownloadMetadata( file: file, metaFile: metaFile )
        result.load()
        
        return result
    }
    
    File file
    File metaFile
    long timeout = 24*60*60*1000
    boolean needsRefresh
    URL fileNotFound
    
    static final String LAST_UPDATE_KEY = 'lastUpdate'
    static final String FILE_NOT_FOUND_KEY = 'fileNotFound'
    
    void load() {
        if( !metaFile.exists() ) {
            needsRefresh = true
            log.debug( "${metaFile} not found -> needs refresh" )
            return
        }
        
        def config = new Properties()
        metaFile.withInputStream {
            config.load( it )
        }
        
        long lastUpdate
        try {
            lastUpdate = Long.parseLong( config.getProperty( LAST_UPDATE_KEY ) )
        } catch( Exception e ) {
            lastUpdate = 0
        }
        needsRefresh = ( lastUpdate + timeout < System.currentTimeMillis() )
        
        String value = config.getProperty( FILE_NOT_FOUND_KEY )
        if( value ) {
            fileNotFound = new URL( value )
        }
        
        if( !file.exists() && !fileNotFound ) {
            needsRefresh = true
        }
        
        log.debug( "Loaded meta ${needsRefresh}" )
    }
    
    void save() {
        needsRefresh = false
        
        def config = new Properties()
        config.setProperty( LAST_UPDATE_KEY, "${System.currentTimeMillis()}" )
        
        if( fileNotFound ) {
            config.setProperty( FILE_NOT_FOUND_KEY, fileNotFound.toExternalForm() )
        }
        
        metaFile?.parentFile.makedirs()
        
        metaFile.withOutputStream {
            config.store( it, null )
        }
    }
    
    void success() {
        fileNotFound = null
        save()
    }
    
    void failed( P2DownloadException e ) {
        fileNotFound = e.url;
        save()
    }
}
