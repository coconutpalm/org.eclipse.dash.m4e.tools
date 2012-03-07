package m4e

import java.util.jar.JarFile;

class P2ListCmd extends AbstractCommand {

    final static String DESCRIPTION = '''\
URL
    - List the content of a P2 repository.'''
        
    void run( String... args ) {
        
        if( args.size() < 2 ) {
            throw new UserError( "Expected at least one argument: The URL of the p2 repository to list" )
        }
        
        def url = new URL( args[1] )
        log.info( 'Listing {}...', url )
        def path = urlToPath( url )
        path.makedirs()
        
        def contentJarFile = new File( path, 'content.jar' )
        if( !contentJarFile.exists() ) {
            downloadContentJar( url, contentJarFile )            
        }
        
        def contentXmlFile = new File( path, 'content.xml' )
        if( !contentXmlFile.exists() ) {
            def jar = new JarFile( contentJarFile )
            def entry = jar.getEntry( 'content.xml' )
            def input = jar.getInputStream( entry )
            
            input.withStream { it ->
                contentXmlFile << it
            }
        }
        
        list( contentXmlFile )
    }
    
    void list( File contentXmlFile ) {
        def doc = new XmlParser().parse( contentXmlFile )
        list( doc )
    }
    
    void list( Node doc ) {
        def units = doc.units
        
        println( "Found ${units.'@size'} items." )
        
        def features = []
        def plugins = []
        def other = []
        
        for( Node unit : units.unit ) {
            def artifacts = unit.artifacts[0]
            if( !artifacts ) {
                //println( "${unit.'@id'} ${unit.'@version'}: No artifacts node" )
                other << unit
                continue
            }
            
            if( artifacts.'@size' != '1' ) {
                println( "${unit.'@id'} ${unit.'@version'}: Unusual size: ${artifacts.'@size'}" )
                other << unit
                continue
            }
            
            def artifact = artifacts.artifact[0]
            
            def classifier = artifact.'@classifier'
            if( 'org.eclipse.update.feature' == classifier ) {
                features << unit
                continue
            } else if( 'osgi.bundle' == classifier ) {
                plugins << unit
            } else {
                println( "${unit.'@id'} ${unit.'@version'}: Unusual classifier: ${classifier}" )
                other << unit
            }
        }
        
        def sorter = { it ->
            "${it.'@id'} ${it.'@version'}"
        }
        
        features.sort( sorter )
        plugins.sort( sorter )
        other.sort( sorter )
        
        def printer = { it ->
            println("    ${it.'@id'} ${it.'@version'}" )
        }
        
        println( "Found ${features.size()} features:" )
        features.each printer
        
        println( "Found ${plugins.size()} plugins:" )
        plugins.each printer
        
        println( "Found ${other.size()} unusual units:" )
        //other.each printer
    }
    
    void downloadContentJar( URL url, File contentJarFile ) {
        def downloadUrl = new URL( "${url.toExternalForm()}/content.jar" )
        log.info( 'Downloading content.jar from {} to {}', downloadUrl, contentJarFile )
        
        HttpURLConnection conn = downloadUrl.openConnection()
        conn.connect()
        
        long contentLength = Long.parseLong(conn.getHeaderField("Content-Length"))
        Progress p = new Progress( contentLength )
        log.info( 'Size: {} bytes = {} kb', p.size, p.sizeInKB )
        
        p.update( 0 )
        
        File tmp = new File( "${contentLength}.tmp" )
        
        downloadUrl.withInputStream { input ->
            byte[] buffer = new byte[10240]
            
            tmp.withOutputStream { output ->
                int len
                while( ( len = input.read(buffer) ) > 0 ) {
                    p.update( len )
                    
                    output.write( buffer, 0, len )
                }
            }
        }
        
        System.out.println()
        
        tmp.renameTo( contentJarFile )
    }
    
    File urlToPath( URL url ) {
        def path = new File( workDir, "p2" )
        path = new File( path, url.host )
        
        if( url.port != 80 && url.port != -1 ) {
            path = new File( path, "${url.port}" )
        }
        
        path = new File( path, url.path )
        return path
    }
}

class Progress {
    long count
    long size
    long sizeInKB
    
    String format = "Downloaded %6d of %6dkb (%3d%%)\r"
    
    public Progress( long size ) {
        this.size = size
        this.sizeInKB = this.size >>> 10
    }
    
    void update( long len ) {
        count += len
        
        int p = 100 * count / size
        long progress = count >>> 10

        System.out.printf( format, progress, sizeInKB, p )
        System.out.flush()
    }
}