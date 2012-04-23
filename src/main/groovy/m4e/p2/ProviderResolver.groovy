package m4e.p2

import groovy.util.Node;

import java.util.List;
import java.util.Map;

class ProviderResolver {
    P2Repo repo
    DependencyCache dependencyCache
    
    Map<String, List<P2Bundle>> providers = new HashMap().withDefault { [] }
    
    void register( P2Bundle bundle, Node unit ) {
        def provides = unit.provides
        if( !provides ) {
            return
        }
        
        provides.provided.each { provided ->
            String namespace = provided.'@namespace'
            String key = "${namespace}:${provided.'@name'}"
            
//            println "define ${key}"
            providers.get( key ).add( bundle )
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
//                println "Keep ${dep}"
                newDeps << dep
                return
            }
            
            String key = "${dep.type}:${dep.id}"
//            println key
            def bundles = providers[key]
//            println dep
//            println bundles
            if( bundles ) {
//                println "Found ${key}"
                bundles.each { bundle ->
                    def versionRange = repo.versionCache.range( "[${bundle.version},${bundle.version}]" )
                    newDeps << dependencyCache.dependency( bundle.id, 'osgi.bundle', versionRange )
                }
            } else {
                // TODO P2Dependency( id=org.eclipse.acceleo_root, version=3.1.3.v20120214-0359, type=binary )
                // TODO P2Dependency( id=org.apache.commons.csv, version=[1.0.0,2.0.0), type=java.package )
                // TODO P2Dependency( id=javax.sql, version=0.0.0, type=java.package )

                //println dep
//                println "Missing ${key}"
                newDeps << dep
            }
        }
        
        return newDeps as ArrayList
    }
}
