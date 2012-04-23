package m4e

import java.util.jar.JarFile;
import java.util.jar.Pack200;
import javax.xml.bind.GetPropertyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;

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
        
        def repo = new P2Repo( workDir: workDir, url: url )
        repo.load()
        
        System.out.withWriter {
            repo.list( it )
        }
    }
}

class Version implements Comparable<Version> {
    
    final static int BIT_WIDTH = 10
    final static int BIT_MASK = ( 1 << BIT_WIDTH ) - 1
    
    final boolean blank
    final long version
    final String qualifier
    
    Version( String pattern ) {
        if( pattern ) {
            String[] parts = pattern.split( '\\.', 4 )
            
            int major = Integer.parseInt( parts[0] )
            int minor = Integer.parseInt( parts[1] )
            int service = Integer.parseInt( parts[2] )
            
            version = ( ( ( major << BIT_WIDTH ) + minor ) << BIT_WIDTH ) + service 
            
            qualifier = parts.size() == 4 ? parts[3] : null
            blank = false
            
            assert pattern == toString()
        } else {
            blank = true
        }
    }
    
    boolean equals( Object o ) {
        if( this == o ) {
            return true
        }
        
        if( !( o instanceof Version ) ) {
            return false
        }
        
        Version other = o
        
        if( blank != other.blank ) {
            return false
        }
        if( version != other.version ) {
            return false
        }
        if( qualifier != other.qualifier ) {
            return false
        }
        
        return true
    }
    
    int hashCode() {
        if( blank ) {
            return 0
        }
        
        return 1311 * version + ( qualifier ? qualifier.hashCode() : 0 )
    }
    
    int getMajor() {
        return version >>> (2*BIT_WIDTH)
    }
    
    int getMinor() {
        return ( version >>> BIT_WIDTH ) & BIT_MASK 
    }
    
    int getService() {
        return version & BIT_MASK 
    }
    
    String toString() {
        if( blank ) {
            return ''
        }
        
        String s = ''
        if( qualifier ) {
            s = ".${qualifier}"
        }
        
        return "${major}.${minor}.${service}${s}"
    }

    String shortVersion() {
        if( blank ) {
            return ''
        }

        return "${major}.${minor}.${service}"
    }
    
    public int compareTo( Version o ) {
        
        if( blank ) {
            return o.blank ? 0 : -1
        }
        
        int d = version - o.version

        if( d == 0 ) {
            if( qualifier ) {
                if( o.qualifier ) {
                    d = qualifier.compareTo( o.qualifier )
                } else {
                    d = 1
                }
            } else {
                d = o.qualifier ? -1 : 0
            }
        }
        
        return d;
    }
    
    Version next() {
        return new Version( "${major}.${minor}.${service+1}" )
    }
}

class VersionRange {
    
    static final NULL_RANGE = new VersionRange( null )
    
    final Version lower
    final boolean includeLower
    final Version upper
    final boolean includeUpper
    
    VersionRange( String pattern, VersionCache cache = null ) {
        if( !pattern ) {
            lower = upper = null
            includeLower = includeUpper = false
            return
        }
        
        if( pattern.startsWith( '[' ) ) {
            includeLower = true
            pattern = pattern.substring( 1 )
        } else if( pattern.startsWith( '(' ) ) {
            includeLower = false
            pattern = pattern.substring( 1 )
        } else {
            includeLower = false 
        }
        
        if( pattern.endsWith( ']' ) ) {
            includeUpper = true
            pattern = pattern.substring( 0, pattern.size() - 1 )
        } else if( pattern.endsWith( ')' ) ) {
            includeUpper = false
            pattern = pattern.substring( 0, pattern.size() - 1 )
        } else {
            includeUpper = false
        }
        
        String[] parts = pattern.split( ',', 2 )
        
        if( parts.size() >= 1 ) {
            lower = newVersion( cache, parts[0] )
        } else {
            lower = null
        }
        if( parts.size() >= 2 ) {
            upper = newVersion( cache, parts[1] )
        } else {
            upper = null
        }
    }
    
    private static newVersion( VersionCache cache, String pattern ) {
        if( cache ) {
            return cache.version( pattern )
        }
        
        return new Version( pattern )
    }
    
    String toString() {
        StringBuilder buffer = new StringBuilder()
        
        if( includeLower ) {
            buffer.append( '[' )
        } else if( lower && upper ) {
            buffer.append( '(' )
        }
        
        if( lower ) {
            buffer.append( lower )
        }
        
        if( upper ) {
            buffer.append( ',' ).append( upper )
        }
        
        if( includeUpper ) {
            buffer.append( ']' )
        } else if( lower && upper ) {
            buffer.append( ')' )
        }
        
        return buffer.toString()
    }
    
    String shortVersion() {
        StringBuilder buffer = new StringBuilder()
        
        if( includeLower ) {
            buffer.append( '[' )
        } else if( lower && upper ) {
            buffer.append( '(' )
        }
        
        if( lower ) {
            buffer.append( lower.shortVersion() )
        }
        
        if( upper ) {
            buffer.append( ',' ).append( upper.shortVersion() )
        }
        
        if( includeUpper ) {
            buffer.append( ']' )
        } else if( lower && upper ) {
            buffer.append( ')' )
        }
        
        return buffer.toString()
    }
    
    boolean contains( Version version ) {
        int d1 = lower ? lower.compareTo( version ) : -1
        int d2 = upper ? version.compareTo( upper ) : -1
        
        int max1 = includeLower ? 0 : -1
        int max2 = includeUpper ? 0 : -1
        
        //println "${d1} ${d2} ${max1} ${max2}"
        
        return d1 <= max1 && d2 <= max2
    }
}

class VersionCache {
    
    Map<String, Version> versions = [:].withDefault { pattern -> new Version( pattern ) }
    Map<String, VersionRange> ranges = [:].withDefault { pattern -> new VersionRange( pattern, this ) }
    
    Version version( String pattern ) {
        return versions.get( pattern )
    }
    
    VersionRange range( String pattern ) {
        return ranges.get( pattern )
    }
}

class P2Dependency implements Comparable<P2Dependency> {
    String type
    String id
    VersionRange versionRange
    List<P2Bundle> bundles 
    
    String toString() {
        return "${getClass().simpleName}( id=${id}, version=${versionRange}, type=${type} )"
    }
    
    void list( Writer out, String indent ) {
        out << "${indent}${id} ${versionRange.shortVersion()}\n"
    }
    
    public int compareTo( P2Dependency o ) {
        int d = id.compareTo( o.id )
        
        if( d == 0 ) {
            d = versionRange.lower.compareTo( o.versionRange.lower )
        }
        
        return d
    }
    
    @Override
    public boolean equals( Object obj ) {
        if( this == obj ) {
            return true
        }
        
        if( !(obj instanceof P2Dependency) ) {
            return false
        }
        
        P2Dependency other = obj
        if( !id.equals( other.id ) ) {
            return false
        }
        if( !type.equals( other.type ) ) {
            return false
        }
        if( !versionRange.equals( other.versionRange ) ) {
            return false
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode() *  31 + type.hashCode() * 37 + versionRange.hashCode() * 97;
    }
}

abstract class P2Bundle implements Comparable<P2Bundle> {
    String id
    String name
    String description
    Version version
    List<P2Dependency> dependencies = []
    
    String toString() {
        return "${getClass().simpleName}( id=${id}, version=${version}, name=${name} )"
    }
    
    void list( Writer out, String indent ) {
        String s = ''
        if( description ) {
            s = " - ${description.trim().replaceAll('\\s+', ' ')}"
        }
        out << "${indent}${name}${s}\n"
        
        indent += '    '
        
        for( P2Dependency dep : dependencies ) {
            dep.list( out, indent )
        }
    }

    public int compareTo( P2Bundle o ) {
        
        int d = id.compareTo( o.id )
        if( d == 0 ) {
            d = version.compareTo( o.version )
        }
        
        return d;
    }
    
    public abstract URL downloadURL( URL baseURL );
}

class P2Plugin extends P2Bundle {
    public URL downloadURL( URL baseURL ) {
        return new URL( baseURL, "plugins/${id}_${version}.jar" )
    }
}

class P2Feature extends P2Bundle {
    public URL downloadURL( URL baseURL ) {
        String baseName = id.removeEnd( '.feature.group' )
        
        return new URL( baseURL, "features/${baseName}_${version}.jar" )
    }
}

class P2Category extends P2Bundle {
    public URL downloadURL( URL baseURL ) {
        throw new UnsupportedOperationException( "Can't download categories" )
    }
}

class P2Other {
    String id
    Version version
    String message
    
    String toString() {
        return "${id} ${version}: ${message}"
    }
}

class P2Unit {
    String id
    Version version
    String xml
    
    String toString() {
        return "${id} ${version}"
    }
}

class DependencySet {
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
                
                    tmp.renameTo( cached )
                }
            } catch( FileNotFoundException e ) {
                cached = repo.downloader.download( url )
            }
            
            String path = ( it instanceof P2Feature ) ? 'features' : 'plugins'
            File dir = new File( dest, path )
            
            cached.copy( new File( dir, fileName ) )
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

class DependencyCache {
    Map<String, P2Dependency> cache = [:]
    
    P2Dependency dependency( String id, String type, VersionRange versionRange ) {
        String key = "${id}:${type}:${versionRange}"
        
        P2Dependency result = cache.get( key )
        if( !result ) {
            result = new P2Dependency( id: id, type: type, versionRange: versionRange )
            cache[key] = result
        }
        
        return result
    }
}

class P2Index {

    String contentJarName
    String contentXmlName
    
    P2Index( File file ) {
        
        Properties properties = new Properties();
        file.withInputStream {
            properties.load( it )
        }
        
        def value = properties.getProperty( 'metadata.repository.factory.order' )
        value = value.trim().removeEnd( ',!' )
        
        contentXmlName = value
        contentJarName = value.removeEnd( '.xml' ) + '.jar'
    }
}

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
                throw meta.fileNotFound;
            }
            
            log.info( "Using cached version of ${url}" )
            return file
        }
        
        url = locateMirror( url )
        
        log.info( "Downloading ${url} to ${file}..." )
        
        URLConnection conn = url.openConnection()
        conn.connect()
        
        def value = conn.getHeaderField("Content-Length")
        long contentLength = value ? Long.parseLong( value ) : 0
        Progress p = progressFactory.newProgress( contentLength )
        log.info( 'Size: {} bytes = {} kb', p.size, p.sizeInKB )
        
        p.update( 0 )
        
        File tmp = new File( "${file.absolutePath.removeEnd('.jar')}.tmp" )
        File dir = tmp.parentFile
        if( dir ) {
            dir.makedirs()
        }
        
        try {
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
            
            meta.success()
        } catch( FileNotFoundException e ) {
            meta.failed( e )
            throw e
        }
        
        return file
    }
    
    URL locateMirror( URL url ) {
        if( 'http' != url.protocol ) {
            return url
        }
        
        if( -1 != url.port ) {
            return url
        }
        
        if( 'download.eclipse.org' != url.host ) {
            return url
        }
        
        String path = URLEncoder.encode( url.path, 'UTF-8' )
        URL mirrorList = new URL( "http://www.eclipse.org/downloads/download.php?file=${path}&protocol=http&format=xml" )
        
        def doc
        mirrorList.withInputStream {
            doc = new XmlParser().parse( it )
        }
        
        def mirrors = doc.mirror
        if( !mirrors ) {
            return url
        }
        
        // TODO switch to a different mirror when this one is down
        log.debug( "Found ${mirrors.size()} mirrors. Using ${mirrors[0].'@label'}" )
        return new URL( mirrors[0].'@url' )
    }
    
    ProgressFactory progressFactory = new ProgressFactory()
}

interface IP2Repo {
    P2Bundle latest( String id )
    P2Bundle latest( String id, VersionRange range )
    P2Bundle find( String id, Version version )
}

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
        } catch( FileNotFoundException e ) {
            return loadContent()
        }
    }
    
    IP2Repo loadContent() {
        File contentXmlFile
        
        try {
            def contentJarFile = downloader.download( new URL( url, 'content.jar' ) )
            
            contentXmlFile = unpackContentJar( contentJarFile )
        } catch( FileNotFoundException e ) {
            
            try {
                contentXmlFile = downloader.download( new URL( url, 'content.xml' ) )
            } catch( FileNotFoundException e2 ) {
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
        } catch( FileNotFoundException e2 ) {
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
        
        def result = new MergedP2Repo()
        
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

class MergedP2Repo implements IP2Repo {
    
    private List<IP2Repo> repos = []

    void add( IP2Repo repo ) {
        repos << repo
    }

    P2Bundle latest( String id, VersionRange range = VersionRange.NULL_RANGE ) {
        
        for( IP2Repo repo : repos ) {
            P2Bundle result = repo.latest( id, range )
            if( result ) {
                return result
            }
        }
        
        return null;
    }

    P2Bundle find( String id, Version version ) {
        
        for( IP2Repo repo : repos ) {
            P2Bundle result = repo.find( id, version )
            if( result ) {
                return result
            }
        }
        
        return null;
    }    
    
}

class P2Repo implements IP2Repo {

    static final Logger log = LoggerFactory.getLogger( P2Repo )
    
    VersionCache versionCache = new VersionCache()
    
    List<P2Category> categories = []
    List<P2Feature> features = []
    List<P2Plugin> plugins = []
    List<P2Other> others = []
    List<P2Unit> units = []
    
    Map<String, List<P2Bundle>> bundlesById = [:].withDefault { [] }
    
    void list( Writer out ) {
        
        list( out, 'categories', categories )
        list( out, 'features', features )
        list( out, 'plugins', plugins )
        
        out << "${others.size()} other nodes:\n"
        for( Node item : others ) {
            out << "    ${item}\n"
        }
    }
    
    void list( Writer out, String title, List<P2Bundle> bundles ) {
        out << "${bundles.size()} ${title}:\n"
        for( P2Bundle item: bundles ) {
            item.list( out, '    ' )
        }
    }
    
    private ZERO_VERSION = versionCache.version( '0.0.0' )
    
    Map<String, P2Bundle> latestCache = [:]
    
    P2Bundle latest( String id, VersionRange range = VersionRange.NULL_RANGE ) {
        
        String key = "${id}:${range}"
        P2Bundle result = null
//        P2Bundle result = latestCache[key]
//        if( result ) {
//            return result
//        }
        
        Version min = ZERO_VERSION
        def findMax = {
            if( min.compareTo( it.version ) < 0 && range.contains( it.version ) ) {
                result = it
                min = it.version
            }
        }
        bundlesById[id].each findMax
        
//        latestCache[key] = result
        
        return result
    }
    
    P2Bundle find( String id, Version version ) {
        VersionRange range = versionCache.range( "[${version},${version}]" )
        return latest( id, range)
    }
    
    File workDir
    URL url
    Downloader downloader
}

class CompositeRepoException extends RuntimeException {}

class ContentXmlParser {
    static final Logger log = LoggerFactory.getLogger( ContentXmlParser )

    P2Repo repo
    ProviderResolver providerResolver
    VersionCache versionCache
    DependencyCache dependencyCache = new DependencyCache()
    
    void parseXml( File contentXmlFile ) {
        providerResolver = new ProviderResolver( repo: repo, dependencyCache: dependencyCache )
        versionCache = repo.versionCache
        
        def doc = new XmlParser().parse( contentXmlFile )
        parse( doc )
        
        log.info( "Parsed ${contentXmlFile}" )
        log.info( "dependencyCache has ${dependencyCache.cache.size()} elements" )
    }
    
    void parse( Node doc ) {
        
        String type = doc.'@type'
        if( type == 'org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository' ) {
            throw new CompositeRepoException()
        }
        
        if( type != 'org.eclipse.equinox.internal.p2.metadata.repository.LocalMetadataRepository' ) {
            throw new P2Exception( "Unsupported repository type ${type}" )
        }
        
        def units = doc.units
        
        log.info( "Found ${units.'@size'} items." )
        
        for( Node unit : units.unit ) {
            
            def isCategory = getProperty( unit, 'org.eclipse.equinox.p2.type.category' )
            if( 'true' == isCategory ) {
                repo.categories << parseCategory( unit )
                continue
            }

            def artifacts = unit.artifacts[0]
            parsePluginOrFeature( unit, artifacts )
        }
        
        providerResolver.resolveRequirements()
        
        repo.categories.sort()
        repo.features.sort()
        repo.plugins.sort()
        repo.others.sort { "${it.id} ${it.version}" }
    }
    
    P2Category parseCategory( Node unit ) {
        def id = unit.'@id'
        def version = versionCache.version( unit.'@version' )
        def name = getProperty( unit, 'org.eclipse.equinox.p2.name' )
        def description = getDescription( unit )
        
        def category = new P2Category( id: id, version: version, name: name, description: description )
        category.dependencies = parseDependencies( unit )
        
        return category
    }
    
    List<P2Dependency> parseDependencies( Node unit ) {
        List<P2Dependency> result = []
        
        //println "parseDependencies ${unit.'@id'} ${unit.requires.size()} ${unit.artifacts.size()}"
        if( unit.requires ) {
            for( Node required : unit.requires[0].required ) {
                
                def type = required.'@namespace'
                def id = required.'@name'
                def versionRange = versionCache.range( required.'@range' )
                
                if( id.endsWith( '.feature.jar' ) ) {
                    continue
                }
                
                result << dependencyCache.dependency( id, type, versionRange )
            }
        }

        if( unit.artifacts ) {
            for( Node artifact: unit.artifacts[0].artifact ) {
                
                def type = artifact.'@classifier'
                def id = artifact.'@id'
                def versionRange = versionCache.range( artifact.'@version' )
                
                result << dependencyCache.dependency( id, type, versionRange )
            }
        }
        
        return result
    }
    
    String getProperty( Node unit, String name ) {
        def properties = unit.properties
        if( !properties || properties.size() == 0 ) {
            return null
        }
        
        properties = properties[0]
        return properties.property.find { name == it.'@name' }?.'@value'
    }
    
    void parsePluginOrFeature( Node unit, Node artifacts ) {

        String id = unit.'@id'
        //def updateFeaturePlugin = getProperty( unit, 'org.eclipse.update.feature.plugin' )
        if( id.endsWith( '.feature.jar' ) ) {
            // Ignore feature JARs
            return
        }
        
        String classifier = ''
        if( artifacts ) {
            classifier = artifacts.artifact[0].'@classifier'
        }

        def isTypeGroup = getProperty( unit, 'org.eclipse.equinox.p2.type.group' )
        //println "${unit.'@id'} ${classifier} ${isTypeGroup}"
        if( 'org.eclipse.update.feature' == classifier || 'true' == isTypeGroup || 'false' == isTypeGroup ) {
            parseFeature( unit )
            return
        }
        
        def typeFragment = getProperty( unit, 'org.eclipse.equinox.p2.type.fragment' )
        if( 'osgi.bundle' == classifier || 'binary' == classifier || 'true' == typeFragment ) {
            parsePlugin( unit )
            return
        }
        
        if( !artifacts ) {
            
            def requires = unit.requires
            if( id.startsWith( 'tooling' ) || id.startsWith( 'epp.package.' ) || !requires ) {
                repo.units << parseUnit( unit )
                return
            }
            
            println "${id} ${requires}"
        }
        
        repo.others << new P2Other( id: id, version: versionCache.version( unit.'@version' ), message: "Unable to determine type" )
    }
    
    P2Unit parseUnit( Node unit ) {
        StringWriter buffer = new StringWriter( 10240 )
        def ip = new IndentPrinter( buffer, '    ' )
        def printer = new XmlNodePrinter( ip )
        printer.print( unit )
        String xml = buffer.toString()
        String id = unit.'@id'
        
        def result = new P2Unit( id: id, version: versionCache.version( unit.'@version' ), xml: xml )
        return result
    }
    
    P2Feature parseFeature( Node unit ) {
        def id = unit.'@id'
        def version = versionCache.version( unit.'@version' )
        def name = getName( unit )
        def description = getDescription( unit )
        if( name == description ) {
            description = null
        }

        def result = new P2Feature( id: id, version: version, name: name, description: description )
        result.dependencies = parseDependencies( unit )
        
        repo.features << result
        providerResolver.register( result, unit )
        repo.bundlesById.get( id ) << result

        return result
    }
    
    String getName( Node unit ) {
        
        String name = getProperty( unit, 'org.eclipse.equinox.p2.name' )
        if( name && name.startsWith( '%' ) ) {
            String key = 'df_LT.' + name.substring( 1 )
            name = getProperty( unit, key )
        }
        
        if( name ) {
            return name
        }
        
        return unit.'@id'
    }
    
    String getDescription( Node unit ) {
        def description = getProperty( unit, 'org.eclipse.equinox.p2.description' )
        if( !description ) {
            return null
        }
        
        if( description.startsWith( '%' ) ) {
            String key = 'df_LT.' + description.substring( 1 )
            description = getProperty( unit, key )
        }
        
        return description
    }
    
    P2Plugin parsePlugin( Node unit ) {
        def id = unit.'@id'
        def version = versionCache.version( unit.'@version' )
        def name = getName( unit )
        def description = getDescription( unit )
        if( name == description ) {
            description = null
        }
        
        def result = new P2Plugin( id: id, version: version, name: name, description: description )
        result.dependencies = parseDependencies( unit )
        
        repo.plugins << result
        providerResolver.register( result, unit )
        repo.bundlesById.get( id ) << result
        
        return result
    }
    
}

class ProviderResolver {
    P2Repo repo
    DependencyCache dependencyCache
    
    Map<String, List<P2Bundle>> providers = new HashMap().withDefault { [] }
    
    void register( P2Bundle feature, Node unit ) {
        def provides = unit.provides
        if( !provides ) {
            return
        }
        
        provides.provided.each { provided ->
            String key = "${provided.'@namespace'}:${provided.'@name'}"
            
            providers.get( key ).add( feature )
        }
    }
    
    void resolveRequirements() {
        
        repo.features.each {
//            println it
            def newDeps = resolveRequirements( it.dependencies )
            it.dependencies = newDeps
        }
        
        repo.plugins.each {
//            println it
            def newDeps = resolveRequirements( it.dependencies )
            it.dependencies = newDeps
        }
    }
    
    List<P2Dependency> resolveRequirements( List<P2Dependency> dependencies ) {
        
        Set<P2Dependency> newDeps = new LinkedHashSet<P2Dependency>();
        
        dependencies.each { dep ->
            if( dep.type == 'osgi.bundle' || dep.type == 'org.eclipse.equinox.p2.iu' ) {
                newDeps << dep
                return
            }
            
            String key = "${dep.type}:${dep.id}"
            def bundles = providers[key]
//            println dep
//            println bundles
            if( bundles ) {
                bundles.each { bundle ->
                    def versionRange = repo.versionCache.range( "[${bundle.version},${bundle.version}]" )
                    newDeps << dependencyCache.dependency( bundle.id, 'osgi.bundle', versionRange )
                }
            } else {
                // TODO P2Dependency( id=org.eclipse.acceleo_root, version=3.1.3.v20120214-0359, type=binary )
                // TODO P2Dependency( id=org.apache.commons.csv, version=[1.0.0,2.0.0), type=java.package )
                // TODO P2Dependency( id=javax.sql, version=0.0.0, type=java.package )

                //println dep
                newDeps << dep
            }
        }
        
        return newDeps as ArrayList
    }
}

class ProgressFactory {
    
    Progress newProgress( long contentLength ) {
        return new Progress( contentLength )
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
        
        int p = size > 0 ? ( 100 * count / size ) : 0
        long progress = count >>> 10
        
        printProgress( progress, p )
    }
    
    void printProgress( long progress, int p ) {
        System.out.printf( format, progress, sizeInKB, p )
        System.out.flush()
    }
    
    void close() {
        System.out.println()
    }
}

class P2Exception extends RuntimeException {
    P2Exception( String message ) {
        super( message )
    }
    
    P2Exception( String message, Throwable cause ) {
        super( message, cause )
    }
}