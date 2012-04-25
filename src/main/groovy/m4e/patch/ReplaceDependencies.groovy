package m4e.patch

import java.util.List;
import java.util.Map;
import m4e.Pom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Replace dependencies in the POM with other dependencies */
class ReplaceDependencies extends Patch {
    
    final Logger log = LoggerFactory.getLogger( getClass() )
    
    String defaultProfile
    String profile
    List<ReplaceDependency> replacements = []
    Map<String, ReplaceDependency> replMap
    
    void init() {
        replMap = [:]
        replacements.each { replMap[it.pattern.key()] = it }
        
//        println replMap.collect { it.toString() }.join('\n')
    }
    
    void apply( Pom pom ) {
        if( !replMap ) {
            init()
        }
        
        def tool = new ProfileTool( pom: pom, defaultProfileName: defaultProfile, profileName: profile )
//        println tool.defaultProfileName
        
        pom.dependencies.each {
             String key = it.key()
             
             def replacer = replMap[key]
             if( replacer ) {
                 log.debug( 'Found {} in {}', key, pom.source )
                 
                 tool.replaceDependency( it, replacer.replacement )
             }
        }
    }
    
    String toString() {
        return "ReplaceDependencies( defaultProfile=${defaultProfile}, profile=${profile}, replacements=${replacements.size()} )"
    }
}