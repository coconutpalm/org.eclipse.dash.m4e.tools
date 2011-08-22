/*******************************************************************************
 * Copyright (c) 21.08.2011 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/
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
}