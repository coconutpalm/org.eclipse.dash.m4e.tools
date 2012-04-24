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
import m4e.patch.GlobalPatches
import m4e.patch.PatchLoader
import m4e.patch.PatchSet
import m4e.patch.RemoveNonOptional
import m4e.patch.StripQualifiers
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
