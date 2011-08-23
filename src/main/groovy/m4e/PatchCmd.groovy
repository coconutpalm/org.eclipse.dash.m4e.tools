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

import groovy.transform.ToString;
import org.codehaus.groovy.control.CompilerConfiguration;
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

	PatchSet loadPatches( String... patches ) {
		def set = new PatchSet()
		
		set.patches << new RemoveNonOptional()
        
        for( String patchName : patches ) {
            def loader = new PatchLoader( new File( patchName ).getAbsoluteFile() )
            def patch = loader.load()
            
            set.patches << patch
        }
		
		return set
	}
}

class PatchLoader {
    
    File file
    String text
    
    PatchLoader( File file ) {
        if( !file.exists() ) {
            throw new UserError( "Can't find patch ${file}" )
        }
        
        this.file = file
    }

    PatchLoader( String text ) {
        this.text = text
    }
        
    ScriptedPatchSet set
    
    PatchSet load() {
        def config = new CompilerConfiguration()
        config.setScriptBaseClass("m4e.PatchScript")
    
        def shell = new GroovyShell(this.class.classLoader, new Binding(), config)
        
        def text = this.text ? this.text : file.getText( 'utf-8' )
        text += "\n\nthis"
        def inst = shell.evaluate( text, "PatchScript" )
        
        set = inst.set
        set.source = file ? file.absolutePath : 'JUnit test'
        
        check()
        
        return set
    }
    
    void check() {
        findDuplicateReplacements()
    }
    
    void findDuplicateReplacements() {
        
        Set<String> keys = []
        
        set.patches.each {
            if( it instanceof ReplaceDependency ) {
                def key = it.pattern.key()
                
                if( !keys.add( key ) ) {
                    throw new UserError( "Found duplicate replace ${key} in patch '{set.source}'" )
                }
            }
        }
    }
}

class ScriptedPatchSet extends PatchSet {
    String source
}

abstract class PatchScript extends Script {
    ScriptedPatchSet set = new ScriptedPatchSet()
    
    String defaultProfile
    String profile
    
    void defaultProfile( String name ) {
        this.defaultProfile = name
    }
    
    void profile( String name ) {
        this.profile = name
    }
    
    void replace( String _pattern, String with ) {
        PatchDependency pattern = PatchDependency.fromString( _pattern )
        PatchDependency replacement = PatchDependency.fromString( with )
        
        def patch = new ReplaceDependency( defaultProfile: defaultProfile, profile: profile, pattern: pattern, replacement: replacement )
        set.patches << patch
    }
    
    void delete( String pattern ) {
        set.patches << new DeleteDependency( key: pattern )
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
    
    String toString() {
        return "DeleteDependency( ${key} )"
    }
}

class PatchDependency {
    String groupId
    String artifactId
    String version
    boolean optional
    String scope
    
    String key() {
        return "${groupId}:${artifactId}:${version}"
    }
    
    String toString() {
        return "PatchDependency( ${key()} )"
    }
    
    static PatchDependency fromString( String s ) {
        def parts = s.split( ':', -1 )
        if( parts.size() < 3 ) {
            throw new UserError( "Expected at least three colon-separated values: [${s}]" )
        }
        
        def options = [:]
        for( String part in parts[3..<parts.size()] ) {
            def tmp = part.split( '=', 2 )
            
            if( tmp.size() != 2 ) {
                throw new UserError( "Expected key=value in [${part}] of [${s}]" )
            }
            
            options[tmp[0]] = tmp[1]
        }
        
        def optional = options.remove( 'optional' )
        def scope = options.remove( 'scope' )
        
        if( options.size() ) {
            throw new UserError( "Unexpected options ${options} in ${s}" )
        }
        
        boolean optValue = false
        if( optional != null ) {
            optValue = 'true' == optional
        }
        
        return new PatchDependency( groupId: parts[0], artifactId: parts[1], version: parts[2], optional: optValue, scope: scope )
    }
}

class ReplaceDependency extends Patch {
    String defaultProfile
    String profile
    PatchDependency pattern
    PatchDependency replacement
    
    void apply( Pom pom ) {
        // TODO
    }
    
    String toString() {
        return "ReplaceDependency( ${pattern} -> ${replacement} )"
    }
}