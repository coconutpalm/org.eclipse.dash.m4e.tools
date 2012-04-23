package m4e.p2

import groovy.util.Node;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        
        log.debug( "Parsed ${contentXmlFile}" )
        log.debug( "dependencyCache has ${dependencyCache.cache.size()} elements" )
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
        
        log.info( "Found ${units.unit.size()} items." )
        
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
        
        String xml = xmlToString( unit )
        repo.others << new P2Other( id: id, version: versionCache.version( unit.'@version' ), message: "Unable to determine type", xml: xml )
    }
    
    P2Unit parseUnit( Node unit ) {
        String xml = xmlToString( unit )
        String id = unit.'@id'
        
        def result = new P2Unit( id: id, version: versionCache.version( unit.'@version' ), xml: xml )
        return result
    }
    
    String xmlToString( Node node ) {
        StringWriter buffer = new StringWriter( 10240 )
        def ip = new IndentPrinter( buffer, '    ' )
        def printer = new XmlNodePrinter( ip )
        printer.print( node )
        String xml = buffer.toString()
        return xml
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
        boolean source = false
        if( unit.provides ) {
            for( Node provided : unit.provides[0].provided ) {
                String namespace = provided.'@namespace'
                if( 'org.eclipse.equinox.p2.eclipse.type' == namespace ) {
                    source = 'source' == provided.'@name'
                    break
                }
            }
        }
        
        def result = new P2Plugin( id: id, version: version, name: name, description: description, source: source )
        result.dependencies = parseDependencies( unit )
        
        repo.plugins << result
        providerResolver.register( result, unit )
        repo.bundlesById.get( id ) << result
        
        return result
    }
    
}

