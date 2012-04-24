package m4e.patch

import de.pdark.decentxml.Element
import m4e.Dependency;
import m4e.Pom;
import m4e.PomUtils;

class DeleteDependency extends Patch {
    String key
    
    void apply( Pom pom ) {
        
        def toDelete = []
        
        pom.dependencies.each {
            if( key == it.key() ) {
                toDelete << it
            }
        }
        
        for( Dependency d in toDelete ) {
            d.remove()
        }
        
        if( pom.dependencies.isEmpty() ) {
            Element e = pom.xml( Pom.DEPENDENCIES )
            PomUtils.removeWithIndent( e )
        }
    }
    
    String toString() {
        return "DeleteDependency( ${key} )"
    }
}
