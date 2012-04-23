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
    FileNotFoundException fileNotFound
    
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
            fileNotFound = new FileNotFoundException( value )
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
            config.setProperty( FILE_NOT_FOUND_KEY, fileNotFound.message )
        }
        
        metaFile.withOutputStream {
            config.store( it, null )
        }
    }
    
    void success() {
        fileNotFound = null
        save()
    }
    
    void failed( FileNotFoundException e ) {
        
        fileNotFound = e;
        save()
    }
}