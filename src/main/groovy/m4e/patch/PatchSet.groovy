package m4e.patch

import java.util.List;
import m4e.Pom;

/** A patch that delegates to a list of other patches */
class PatchSet extends Patch {
    List<Patch> patches = []
    
    void apply( Pom pom ) {
        for( def patch in patches ) {
            patch.apply( pom )
        }
    }
}
