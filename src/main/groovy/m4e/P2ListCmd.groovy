package m4e

import java.util.jar.JarFile;
import javax.xml.bind.GetPropertyAction;

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
            unpackContentJar(contentJarFile, contentXmlFile)
        }
        
        list( contentXmlFile )
    }

	private File unpackContentJar(File contentJarFile, File contentXmlFile) {
		def jar = new JarFile( contentJarFile )
		def entry = jar.getEntry( 'content.xml' )
		def input = jar.getInputStream( entry )

		input.withStream { it ->
			contentXmlFile << it
		}
	}
    
    void list( File contentXmlFile ) {
        def repo = new P2Repo()
        
        repo.parseXml( contentXmlFile )
        
        System.out.withWriter {
            repo.dumpTo( it )
        }
    }
    
    void downloadContentJar( URL url, File contentJarFile ) {
        
        String uri = url.toExternalForm()
        if( !uri.endsWith( '/' ) ) {
            uri += '/'
        }
        def downloadUrl = new URL( "${uri}content.jar" )
        log.info( 'Downloading content.jar from {} to {}', downloadUrl, contentJarFile )
        
        URLConnection conn = downloadUrl.openConnection()
        conn.connect()
        
        def value = conn.getHeaderField("Content-Length")
        long contentLength = value ? Long.parseLong( value ) : 0
        Progress p = newProgress( contentLength )
        log.info( 'Size: {} bytes = {} kb', p.size, p.sizeInKB )
        
        p.update( 0 )
        
        File tmp = new File( "${contentJarFile.absolutePath.removeEnd('.jar')}.tmp" )
        File dir = tmp.parentFile
        if( dir ) {
            dir.makedirs()
        }
        
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
        
        p.close()
        
        tmp.renameTo( contentJarFile )
    }
    
    Progress newProgress( long contentLength ) {
        return new Progress( contentLength )
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
        
        int d = major - o.major
        if( d == 0 ) {
            d = minor - o.minor
        }
        if( d == 0 ) {
            d = service - o.service
        }
        if( d == 0 ) {
            d = qualifier.compareTo( o.qualifier )
        }
        
        return d;
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
}

class P2Dependency {
    String type
    String id
    VersionRange versionRange
    
    String toString() {
        return "${getClass().simpleName}( id=${id}, version=${versionRange}, type=${type} )"
    }
    
    void dumpTo( Writer out, String indent ) {
        out << "${indent}${id} ${versionRange.shortVersion()}\n"
    }
}

class P2Bundle implements Comparable<P2Bundle> {
    String id
    String name
    String description
    Version version
    List<P2Dependency> dependencies = []
    
    String toString() {
        return "${getClass().simpleName}( id=${id}, version=${version}, name=${name} )"
    }
    
    void dumpTo( Writer out, String indent ) {
        String s = ''
        if( description ) {
            s = " - ${description.trim().replaceAll('\\s+', ' ')}"
        }
        out << "${indent}${name}${s}\n"
        
        indent += '    '
        
        for( P2Dependency dep : dependencies ) {
            dep.dumpTo( out, indent )
        }
    }

    public int compareTo( P2Bundle o ) {
        
        int d = id.compareTo( o.id )
        if( d == 0 ) {
            d = version.compareTo( o.version )
        }
        
        return d;
    }
}

class P2Plugin extends P2Bundle {}
class P2Feature extends P2Bundle {}
class P2Category extends P2Bundle {}

class P2Other {
    String id
    Version version
    String message
    
    String toString() {
        return "${id} ${version}: ${message}"
    }
}

class P2Repo {
    List<P2Category> categories = []
    List<P2Feature> features = []
    List<P2Plugin> plugins = []
    List<P2Other> others = []
    
    void dumpTo( Writer out ) {
        
        dumpTo( out, 'categories', categories )
        dumpTo( out, 'features', features )
        dumpTo( out, 'plugins', plugins )
        
        out << "${others.size()} other nodes:\n"
        for( Node item : others ) {
            out << "    ${item}\n"
        }
    }
    
    void dumpTo( Writer out, String title, List<P2Bundle> bundles ) {
        out << "${bundles.size()} ${title}:\n"
        for( P2Bundle item: bundles ) {
            item.dumpTo( out, '    ' )
        }
    }
    
    void parseXml( File contentXmlFile ) {
        def doc = new XmlParser().parse( contentXmlFile )
        parse( doc )
    }
    
    void parse( Node doc ) {
        def units = doc.units
        
        println( "Found ${units.'@size'} items." )
        
        for( Node unit : units.unit ) {
            
            def isCategory = getProperty( unit, 'org.eclipse.equinox.p2.type.category' )
            if( 'true' == isCategory ) {
                categories << parseCategory( unit )
                continue
            }

            def artifacts = unit.artifacts[0]
            parsePluginOrFeature( unit, artifacts )
        }
        
        categories.sort()
        features.sort()
        plugins.sort()
        others.sort { "${it.node.'@id'} ${it.node.'@version'}" }
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
                
                result << new P2Dependency( type: type, id: id, versionRange: versionRange )
            }
        }

        if( unit.artifacts ) {
            for( Node artifact: unit.artifacts[0].artifact ) {
                
                def type = artifact.'@classifier'
                    def id = artifact.'@name'
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
    
    void parsePluginOrFeature( Node unit, Node artifacts ) {

        //def updateFeaturePlugin = getProperty( unit, 'org.eclipse.update.feature.plugin' )
        if( unit.'@id'.endsWith( '.feature.jar' ) ) {
            // Ignore feature JARs
            return
        }
        
        String classifier = '' 
        if( artifacts ) {
            classifier = artifacts.artifact[0].'@classifier'
        }

        def isTypeGroup = getProperty( unit, 'org.eclipse.equinox.p2.type.group' )
        //println "${unit.'@id'} ${classifier} ${isTypeGroup}"
        if( 'org.eclipse.update.feature' == classifier || 'true' == isTypeGroup ) {
            features << parseFeature( unit )
            return
        }
        
        if( 'osgi.bundle' == classifier ) {
            plugins << parsePlugin( unit )
            return
        }
        
        others << new P2Other( id: unit.'@id', version: new Version( unit.'@version' ), message: "Unusual classifier: ${classifier}" )
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