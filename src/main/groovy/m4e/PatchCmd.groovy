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

import java.util.regex.Pattern;
import groovy.transform.ToString;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.pdark.decentxml.Element;
import de.pdark.decentxml.Node;
import de.pdark.decentxml.XMLUtils;

class PatchCmd extends AbstractCommand {
    
    static final String DESCRIPTION = '''\
target patches...
- Apply the patches to the target repository. Patches can be scripts or directories with scripts.
'''
    
    File target
    PatchSet set
    
    void run( String... args ) {
        if( args.size() == 1 ) {
            throw new UserError( 'Missing path to repository to patch' )
        }
		
		target = new File( args[1] ).absoluteFile
		if( !target.exists() ) {
			throw new UserError( "Directory ${target} doesn't exist" )
		}
		
		String[] patches = args[2..<args.size()]
		log.debug( "Patches: ${patches}" )
		log.debug( "Target: ${target}" )
        
        log.info( "Applying patches to ${target}..." )

		loadPatches( patches )
        
        MavenRepositoryTools.eachPom(target ) {
            patchPom( it )
        }
        
        log.info( "Patched ${count} POMs")
    }
    
    int count
    
    void patchPom( File file ) {
        def pom = Pom.load( file )
        def orig = pom.toString()
        
        set.apply( pom )
        
        def result = pom.toString()
        if( result != orig ) {
            new XmlFormatter( pom: pom ).format()
            
            log.debug( "Patched ${file}" )
            pom.save( file )
            
            count ++
        }
    }

	void loadPatches( String... patches ) {
		set = new PatchSet()
		
		set.patches << new RemoveNonOptional()
		set.patches << new StripQualifiers()
        
        for( String patchName : patches ) {
            def loader = new PatchLoader( new File( patchName ).getAbsoluteFile() )
            def patch = loader.load()
            
            set.patches << patch
        }
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
    ReplaceDependencies replacer
    
    PatchSet load() {
        def config = new CompilerConfiguration()
        config.setScriptBaseClass("m4e.PatchScript")
    
        def shell = new GroovyShell(this.class.classLoader, new Binding(), config)
        
        def text = this.text ? this.text : file.getText( 'utf-8' )
        text += "\n\nthis"
        PatchScript inst = shell.evaluate( text, "PatchScript" )
        
        set = inst.set
        set.source = file ? file.absolutePath : 'JUnit test'
        
        replacer = inst.replacer
        
        if( replacer.replacements ) {
            set.patches << replacer
        }
        
        check()
        
        return set
    }
    
    void check() {
        findDuplicateReplacements()
    }
    
    void findDuplicateReplacements() {
        
        Set<String> keys = []
        
        replacer.replacements.each {
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

    ReplaceDependencies replacer = new ReplaceDependencies()
    
    void defaultProfile( String name ) {
        replacer.defaultProfile = name
    }
    
    void profile( String name ) {
        replacer.profile = name
    }
    
    void replace( String _pattern, String with ) {
        
        PatchDependency pattern = PatchDependency.fromString( _pattern )
        PatchDependency replacement = PatchDependency.fromString( with )
        
        def rd = new ReplaceDependency( pattern: pattern, replacement: replacement )
        replacer.replacements << rd
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
    
    boolean matches( def other ) {
        return key() == other.key()
    }
    
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

class ReplaceDependency {
    PatchDependency pattern
    PatchDependency replacement
    
    @Override
    public String toString() {
        return "ReplaceDependency( ${pattern} -> ${replacement} )"
    }

}

class ProfileTool {
    Pom pom
    String defaultProfileName
    String profileName
    
    Profile defaultProfile
    Profile profile
    
    void replaceDependency( Dependency dependency, PatchDependency replacement ) {
        if( !defaultProfile ) {
            createProfiles()
        }
        
        defaultProfile.addDependency( dependency )
        
        def dep = createDependency( replacement )
        profile.addDependency( dep )
    }
    
    void createProfiles() {
        defaultProfile = pom.profile( defaultProfileName )
        defaultProfile.activeByDefault( true )
        
        profile = pom.profile( profileName )
    }
    
    Dependency createDependency( PatchDependency replacement ) {
        def xml = new Element( 'dependency' )
        
        for( String field in ['groupId', 'artifactId', 'version', 'optional', 'scope'] ) {
            def value = replacement.getProperty( field )
            if( !value ) {
                continue
            }
            
            value = value.toString()
            
            PomUtils.getOrCreate( xml, field ).text = value
        }
        
        return new Dependency( xml: xml, pom: pom )
    }
}

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

/** Strip Eclipse qualifiers from versions */
class StripQualifiers extends Patch {
    
    // ~/.../ isn't supported by the Eclipse Groovy editor
    Pattern versionRangePattern = Pattern.compile( '^([\\[\\]()])([^,]*),([^,]*?)([\\[\\]()])$' );
    
    void apply( Pom pom ) {
        pom.dependencies.each {
            String version = it.value( Dependency.VERSION )
            version = stripQualifier( version )
            it.value( Dependency.VERSION, version )
        }
    }

    String stripQualifier( String version ) {
        if( !version ) {
            return version
        }
        
        def m = versionRangePattern.matcher( version )
        if( !m.matches() ) {
            return stripQualifier2( version )
        }
        
        def prefix = m.group(1)
        def v1 = m.group(2)
        def v2 = m.group(3)
        def postfix = m.group(4)
        
        v1 = stripQualifier2(v1)
        v2 = stripQualifier2(v2)
        
        return "${prefix}${v1},${v2}${postfix}"
    }
    
    String stripQualifier2( String version ) {
        def parts = version.split('\\.', -1)
        int end = Math.min( parts.size()-1, 2 )
        return parts[0..end].join( '.' )
    }
    
    @Override
    public String toString() {
        return 'StripQualifiers()';
    }
}
