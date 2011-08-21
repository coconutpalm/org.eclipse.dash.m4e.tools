package m4e

import de.pdark.decentxml.Element;
import de.pdark.decentxml.Node;
import de.pdark.decentxml.XMLUtils;

class PatchCmd extends AbstractCommand {
    
    void run( String... args ) {
        if( args.size() == 1 ) {
            throw new UserError( 'Missing path to repository to patch' )
        }
		
		def target = new File( args[1] ).absoluteFile
		if( !target.exists() ) {
			throw new UserError( "Directory ${target} doesn't exist" )
		}
		
		def patches = args[2..-1]
		log.debug( "Patches: ${patches}" )
		log.debug( "Target: ${target}" )

		def set = loadPatches( patches )
    }

	PatchSet loadPatches() {
		def set = new PatchSet()
		
		set.patches << new RemoveNonOptional()
		
		return set
	}
}

abstract class Patch {
	abstract void apply( Pom pom )
}

class PatchSet extends Patch {
	List<Patch> patches = []
	
	void apply( Pom pom ) {
		for( def patch in patches ) {
			patch.apply( pom )
		}
	}
}

/** Remove <code>&lt;optional&gt;false&lt;optional&gt;</code> elements from the POM */
class RemoveNonOptional extends Patch {
	void apply( Pom pom ) {
		for( Dependency d in pom.dependencies ) {
			Element optional = d.xml_optional
			if( !optional ) {
				continue;
			}
			
			if( "true".equals( optional.trimmedText ) ) {
				continue;
			}
			
			int index = optional.parentElement.nodeIndexOf( optional )
			if( index > 0 ) {
				index --
				Node previous = optional.parentElement.getNode( index )
				if( XMLUtils.isText( previous ) ) {
					optional.parentElement.removeNode( index )
				}
			}
			optional.remove()
		}
	}
}