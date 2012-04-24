package m4e.patch

import de.pdark.decentxml.Element
import m4e.Pom;
import m4e.PomUtils;
import m4e.Dependency;

/** Remove <code>&lt;optional&gt;false&lt;optional&gt;</code> elements from the POM */
class RemoveNonOptional extends Patch {
    void apply( Pom pom ) {
        for( def d in pom.list( Pom.DEPENDENCIES ) ) {
            Element optional = d.xml( Dependency.OPTIONAL )
            if( !optional ) {
                continue;
            }
            
            if( "true".equals( optional.trimmedText ) ) {
                continue;
            }
            
            PomUtils.removeWithIndent( optional )
        }
    }
}

