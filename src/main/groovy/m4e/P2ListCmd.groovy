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
    boolean blank
    int major
    int minor
    int service
    String qualifier
    
    Version( String pattern ) {
        if( pattern ) {
            String[] parts = pattern.split( '\\.', 4 )
            
            major = Integer.parseInt( parts[0] )
            minor = Integer.parseInt( parts[1] )
            service = Integer.parseInt( parts[2] )
            qualifier = parts.size() == 4 ? parts[3] : null
            blank = false
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
        if( major != other.major ) {
            return false
        }
        if( minor != other.minor ) {
            return false
        }
        if( service != other.service ) {
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
        
        return 1311 * (major+1) * (minor+1) * (service+1) + ( qualifier ? qualifier.hashCode() : 0 )
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
        
        int d = major - o.major
        if( d == 0 ) {
            d = minor - o.minor
        }
        if( d == 0 ) {
            d = service - o.service
        }
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
    Version lower
    boolean includeLower
    Version upper
    boolean includeUpper
    
    VersionRange( String pattern ) {
        if( !pattern ) {
            return
        }
        
        if( pattern.startsWith( '[' ) ) {
            includeLower = true
            pattern = pattern.substring( 1 )
        } else if( pattern.startsWith( '(' ) ) {
            includeLower = false
            pattern = pattern.substring( 1 )
        }
        
        if( pattern.endsWith( ']' ) ) {
            includeUpper = true
            pattern = pattern.substring( 0, pattern.size() - 1 )
        } else if( pattern.endsWith( ')' ) ) {
            includeUpper = false
            pattern = pattern.substring( 0, pattern.size() - 1 )
        }
        
        String[] parts = pattern.split( ',', 2 )
        
        if( parts.size() >= 1 ) {
            lower = new Version( parts[0] )
        }
        if( parts.size() >= 2 ) {
            upper = new Version( parts[1] )
        }
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

class P2Dependency implements Comparable<P2Dependency> {
    String type
    String id
    VersionRange versionRange
    
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
    P2Repo repo
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
            File cached = new File( repo.workDir, 'p2/cache/' + fileName )
            File tmp = new File( repo.workDir, 'p2/cache/download.tmp' )
            
            if( !cached.exists() ) {
                File packedFile = new File( repo.workDir, 'p2/cache/' + fileName + ".pack.gz" )
                
                URL packed = new URL( "${url.toExternalForm()}.pack.gz" )
                try {
                    repo.download( packed, packedFile )
                    
                    unpack( packedFile, tmp )
                } catch( FileNotFoundException e ) {
                    repo.download( url, tmp )
                }
                
                tmp.renameTo( cached )
            }
            
            String path = ( it instanceof P2Feature ) ? 'features' : 'plugins'
            File dir = new File( dest, path )
            
            cached.copy( new File( dir, fileName ) )
        }
    }
    
    void unpack( File packedFile, File unpackedFile ) {
        
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

class P2Repo {
    
    List<P2Category> categories = []
    List<P2Feature> features = []
    List<P2Plugin> plugins = []
    List<P2Other> others = []
    List<P2Unit> units = []
    
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
    
    P2Bundle latest( String id, VersionRange range = new VersionRange( null ) ) {
        Version min = new Version( '0.0.0' )
        P2Bundle result = null
        def findMax = {
            if( id == it.id && min.compareTo( it.version ) < 0 && range.contains( it.version ) ) {
                result = it
                min = it.version
            }
        }
        features.each findMax
        
        if( result ) {
            return result
        }
        
        min = new Version( '0.0.0' )
        plugins.each findMax
        
        return result
    }
    
    P2Bundle find( String id, Version version ) {
        VersionRange range = new VersionRange( "[${version},${version}]" )
        return latest( id, range)
    }
    
    void parseXml( File contentXmlFile ) {
        def doc = new XmlParser().parse( contentXmlFile )
        parse( doc )
    }
    
    void parse( Node doc ) {
        def units = doc.units
        
        log.info( "Found ${units.'@size'} items." )
        
        ProviderResolver providerResolver = new ProviderResolver( repo: this )
        
        for( Node unit : units.unit ) {
            
            def isCategory = getProperty( unit, 'org.eclipse.equinox.p2.type.category' )
            if( 'true' == isCategory ) {
                categories << parseCategory( unit )
                continue
            }

            def artifacts = unit.artifacts[0]
            parsePluginOrFeature( unit, artifacts, providerResolver )
        }
        
        providerResolver.resolveRequirements()
        
        categories.sort()
        features.sort()
        plugins.sort()
        others.sort { "${it.id} ${it.version}" }
    }
    
    P2Category parseCategory( Node unit ) {
        def id = unit.'@id'
        def version = new Version( unit.'@version' )
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
                def versionRange = new VersionRange( required.'@range' )
                
                if( id.endsWith( '.feature.jar' ) ) {
                    continue
                }
                
                result << new P2Dependency( type: type, id: id, versionRange: versionRange )
            }
        }

        if( unit.artifacts ) {
            for( Node artifact: unit.artifacts[0].artifact ) {
                
                def type = artifact.'@classifier'
                def id = artifact.'@id'
                def versionRange = new VersionRange( artifact.'@version' )
                
                result << new P2Dependency( type: type, id: id, versionRange: versionRange )
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
    
    void parsePluginOrFeature( Node unit, Node artifacts, ProviderResolver providerResolver ) {

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
            def feature = parseFeature( unit )
            features << feature
            providerResolver.register( feature, unit )
            
            return
        }
        
        def typeFragment = getProperty( unit, 'org.eclipse.equinox.p2.type.fragment' )
        if( 'osgi.bundle' == classifier || 'binary' == classifier || 'true' == typeFragment ) {
            def plugin = parsePlugin( unit )
            plugins << plugin
            providerResolver.register( plugin, unit )
            return
        }
        
        if( !artifacts ) {
            
            def requires = unit.requires
            if( id.startsWith( 'tooling' ) || id.startsWith( 'epp.package.' ) || !requires ) {
                units << parseUnit( unit )
                return
            }
            
            println "${id} ${requires}"
        }
        
        others << new P2Other( id: id, version: new Version( unit.'@version' ), message: "Unable to determine type" )
    }
    
    P2Unit parseUnit( Node unit ) {
        StringWriter buffer = new StringWriter( 10240 )
        def ip = new IndentPrinter( buffer, '    ' )
        def printer = new XmlNodePrinter( ip )
        printer.print( unit )
        String xml = buffer.toString()
        String id = unit.'@id'
        
        def result = new P2Unit( id: id, version: new Version( unit.'@version' ), xml: xml )
        return result
    }
    
    P2Feature parseFeature( Node unit ) {
        def id = unit.'@id'
        def version = new Version( unit.'@version' )
        def name = getName( unit )
        def description = getDescription( unit )
        if( name == description ) {
            description = null
        }

        def result = new P2Feature( id: id, version: version, name: name, description: description )
        result.dependencies = parseDependencies( unit )
        
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
        def version = new Version( unit.'@version' )
        def name = getName( unit )
        def description = getDescription( unit )
        if( name == description ) {
            description = null
        }
        
        def result = new P2Plugin( id: id, version: version, name: name, description: description )
        result.dependencies = parseDependencies( unit )
        
        return result
    }
    
    File workDir
    URL url
    static final Logger log = LoggerFactory.getLogger( P2Repo )
    
    void setUrl( URL url ) {
        if( !url.path.endsWith( '/' ) ) {
            url = new URL( url.toExternalForm() + '/' )
        }
        this.url = url
    }
    
    void load() {
        def path = urlToPath( url )
        path.makedirs()
        
        def contentJarFile = new File( path, 'content.jar' )
        if( !contentJarFile.exists() ) {
            downloadContentJar( contentJarFile )
        }
        
        def contentXmlFile = new File( path, 'content.xml' )
        if( !contentXmlFile.exists() ) {
            unpackContentJar(contentJarFile, contentXmlFile)
        }
        
        parseXml( contentXmlFile )
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
    
    void downloadContentJar( File contentJarFile ) {
        
        def downloadUrl = new URL( "${url.toExternalForm()}content.jar" )
        log.info( 'Downloading content.jar from {} to {}', downloadUrl, contentJarFile )
        
        download( downloadUrl, contentJarFile )
    }
    
    void download( URL url, File file ) {
        if( file.exists() ) {
            log.info( "Using cached version of ${url}" )
            return
        }
        
        log.info( "Downloading ${url}..." )
        
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
    
    private File unpackContentJar(File contentJarFile, File contentXmlFile) {
        def jar = new JarFile( contentJarFile )
        def entry = jar.getEntry( 'content.xml' )
        def input = jar.getInputStream( entry )

        input.withStream { it ->
            contentXmlFile << it
        }
    }

    ProgressFactory progressFactory = new ProgressFactory()
}

class ProviderResolver {
    P2Repo repo
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
                    def versionRange = new VersionRange( "[${bundle.version},${bundle.version}]" )
                    newDeps << new P2Dependency( type: 'osgi.bundle', id: bundle.id, versionRange: versionRange )
                }
            } else {
                println dep
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
}