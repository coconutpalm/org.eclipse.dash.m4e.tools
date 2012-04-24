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
        
        deleteArtifacts()
        
        MavenRepositoryTools.eachPom( target ) {
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
            
            file = new File( pom.source )
            log.debug( "Patched ${file}" )
            save( pom, file )
            
            count ++
        }
    }
    
    protected void save( Pom pom, File file ) {
        pom.save( file )
    }
    
    GlobalPatches globalPatches = new GlobalPatches()
    
	void loadPatches( String... patches ) {
		set = new PatchSet()
		
		set.patches << new RemoveNonOptional()
		set.patches << new StripQualifiers( globalPatches: globalPatches, target: target )
        
        for( String patchName : patches ) {
            def loader = new PatchLoader( new File( patchName ).getAbsoluteFile(), globalPatches )
            def patch = loader.load()
            
            set.patches << patch
        }
	}
    
    void deleteArtifacts() {
        for( String pattern in artifactsToDelete ) {
            deleteRecursively( pattern )
        }
    }
    
    void deleteRecursively( String pattern ) {
        File path = MavenRepositoryTools.buildPath( target, pattern )
            
        if( !path.exists() ) {
            return
        }
        
        log.info( 'Deleting artifact {}', pattern )
        count ++
        
        log.debug( 'Deleting {}', path )
        assert path.deleteDir()
        
        // Delete empty parent folders
        File parent = path.parentFile
        while( parent && parent != target ) {
            if( parent.list().size() == 0 ) {
                log.debug( 'Deleting {} because it\' emtpy', parent )
                assert parent.delete()
            }
            
            parent = parent.parentFile
        }
    }
}

class QualifierPatch {
    /** Apply this patch to these POMs */
    Pattern pattern
    /** The new version string for matching POMs */
    String version
    
    QualifierPatch( String pattern, String version ) {
        this.pattern = compile( pattern )
        this.version = version
    }
    
    Pattern compile( String text ) {
        return Pattern.compile( text.replace( '.', '\\.' ).replace( '*', '[^:]*' ) )
    }
    
    boolean appliesTo( String key ) {
        if( pattern.matcher( key ).matches() ) {
            return true
        }
        
        return false
    }
}

class GlobalPatches {
    List<String> artifactsToDelete = []
    List<QualifierPatch> qualifierPatches = []
    
    void merge( GlobalPatches other ) {
        artifactsToDelete.addAll( other.artifactsToDelete )
        qualifierPatches.addAll( other.qualifierPatches )
    }
}

class PatchLoader {
    
    File file
    String text
    GlobalPatches globalPatches
    
    PatchLoader( File file, GlobalPatches globalPatches ) {
        if( !file.exists() ) {
            throw new UserError( "Can't find patch ${file}" )
        }
        
        this.file = file
        this.globalPatches = globalPatches
    }

    /** For unit tests */
    PatchLoader( String text ) {
        this.text = text
    }
    
    ScriptedPatchSet patchSet
    ReplaceDependencies replacer
    
    PatchSet load() {
        def config = new CompilerConfiguration()
        config.setScriptBaseClass("m4e.PatchScript")
    
        def shell = new GroovyShell(this.class.classLoader, new Binding(), config)
        
        def text = this.text ? this.text : file.getText( 'utf-8' )
        text += "\n\nthis"
        String source = file ? file.absolutePath : 'JUnit test'
        PatchScript inst = shell.evaluate( text, source )
        
        patchSet = inst.patchSet
        patchSet.source = source
        globalPatches.merge( inst.globalPatches )
        
        replacer = inst.replacer
        
        if( replacer.replacements ) {
            patchSet.patches << replacer
        }
        
        check()
        
        return patchSet
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
    ScriptedPatchSet patchSet = new ScriptedPatchSet()

    ReplaceDependencies replacer = new ReplaceDependencies()
    
    GlobalPatches globalPatches = new GlobalPatches()
    
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
    
    void deleteDependency( String pattern ) {
        patchSet.patches << new DeleteDependency( key: pattern )
    }
    
    void deleteArtifact( String pattern ) {
        globalPatches.artifactsToDelete << pattern
    }
    
    /** Give some bundles a special version */
    void mapQualifier( String pattern, String version ) {
        globalPatches.qualifierPatches << new QualifierPatch( pattern, version )
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

/** Strip Eclipse qualifiers from versions.
 * 
 *  <p>This patcher supports versions like "1", "1.0", "[1.0,2.0)" and standard
 *  Eclipse versions (three numbers plus optional qualifier)
 */
class StripQualifiers extends Patch {
    
    // ~/.../ isn't supported by the Eclipse Groovy editor
    Pattern versionRangePattern = Pattern.compile( '^([\\[\\]()])([^,]*),([^,]*?)([\\[\\]()])$' );
    
    GlobalPatches globalPatches
    File target
    
    void apply( Pom pom ) {
        
        updateVersion( pom )
        
        pom.dependencies.each {
            String version = it.value( Dependency.VERSION )
            
            QualifierPatch p = findQualifierPatch( it.key() )
            if( p ) {
                version = p.version
            } else {
                version = stripQualifier( version )
            }
            
            it.value( Dependency.VERSION, version )
        }
    }
    
    QualifierPatch findQualifierPatch( String key ) {
        for( QualifierPatch p : globalPatches.qualifierPatches ) {
            if( p.appliesTo( key ) ) {
                return p
            }
        }
        
        return null
    }
    
    void updateVersion( Pom pom ) {
        
        String key = pom.key()
        QualifierPatch p = findQualifierPatch( key )
        if( p ) {
            updateVersion( pom, p.version )
            return
        }
        
        String oldVersion = pom.version()
        String newVersion = stripQualifier( oldVersion )
        
        if( oldVersion != newVersion ) {
            updateVersion( pom, newVersion )
        }
    }
    
    void updateVersion( Pom pom, String newVersion ) {
        def e = pom.xml( Pom.VERSION )
        if( ! e ) {
            throw new RuntimeException( 'TODO Missing version element' )
        }
        
        String oldVersion = e.text
        e.text = newVersion
        println "${newVersion} ${e.text} ${pom.version()}"
        
        renameFiles( pom, oldVersion, newVersion )
    }
    
    void renameFiles( Pom pom, String oldVersion, String newVersion ) {
        File oldPomPath = new File( pom.source )
        File newPomPath = MavenRepositoryTools.buildPath( target, pom.key(), 'pom' )
        pom.source = newPomPath.absolutePath
        
        File oldFolder = oldPomPath.parentFile
        File newFolder = newPomPath.parentFile
        newFolder.makedirs()
        
        String prefix = pom.value( Pom.ARTIFACT_ID ) + '-' + oldVersion
        String newPrefix = pom.value( Pom.ARTIFACT_ID ) + '-' + newVersion
        
        int extraFileCount = 0
        oldFolder.eachFile { it ->
            if( it.name.startsWith( prefix ) ) {
                String newName = newPrefix + it.name.substring( prefix.size() )
                File dest = new File( newFolder, newName )
                assert it.renameTo( dest )
            } else {
                extraFileCount ++
            }
        }
        
        if( extraFileCount == 0 ) {
            assert oldFolder.delete()
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
        if( parts.size() == 3 ) {
            def m = parts[2] =~ '^\\d+'
            parts[2] = m[0]
        }
        int end = Math.min( parts.size()-1, 2 )
        return parts[0..end].join( '.' )
    }
    
    @Override
    public String toString() {
        return 'StripQualifiers()';
    }
}
