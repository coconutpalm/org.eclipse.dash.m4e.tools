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
import m4e.maven.ImportExportDB;
import m4e.p2.Version
import m4e.patch.ArtifactRenamer
import m4e.patch.DeleteClasses
import m4e.patch.DeleteEmptyDirectories
import m4e.patch.GlobalPatches
import m4e.patch.ImportDependenciesPatch;
import m4e.patch.OrbitPatch
import m4e.patch.PatchLoader
import m4e.patch.PatchSet
import m4e.patch.QualifierPatch
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
    
    void doRun( String... args ) {
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
        
        init()

		loadPatches( patches )
        
        deleteArtifacts()
        deleteClasses()
        
        collectImportExportInformation()

        applyPatches()        
        
        log.info( "Patched ${count} POMs")
    }
    
    ImportExportDB importExportDB = new ImportExportDB()
    
    void collectImportExportInformation() {
        MavenRepositoryTools.eachPom( target ) { file ->
            try {
                def pom = Pom.load( file )
                collectImportExportInformation( pom )
            } catch( Exception e ) {
                throw new RuntimeException( "Error processing ${file}", e )
            }
        }
        
        syncExportsWithDeletes()
    }
    
    void syncExportsWithDeletes() {
        for( DeleteClasses p : globalPatches.deleteClasses ) {
            
            def exclusions = []
            p.patterns.each { g ->
                String s = g.toString()
                if( s.startsWith( 'META-INF/' ) ) {
                    return
                }
                
                exclusions << new Glob( g.toString().replace( '/', '.' ) )
            }

            importExportDB.updateExports( p.keyPattern, exclusions )
        }
    }
    
    void collectImportExportInformation( Pom pom ) {
        importExportDB.add( pom )
    }
    
    void applyPatches() {
        MavenRepositoryTools.eachPom( target ) { file ->
            
            try {
                patchPom( file )
            } catch( Exception e ) {
                throw new RuntimeException( "Error processing ${file}", e )
            }
        }
        
        for( ArtifactRenamer tool : renamed ) {
            tool.run()
        }
        
        deleteEmptyDirectories()
    }
    
    void deleteEmptyDirectories() {
        def tool = new DeleteEmptyDirectories()
        tool.delete( target )
        
        log.info( 'Deleted {} empty directories', tool.counter )
    }
    
    int count
    
    List<ArtifactRenamer> renamed = []
    
    void patchPom( File file ) {
        def pom = Pom.load( file )
        
        def orig = pom.toString()
        def oldKey = pom.key()
        
        patchPom( pom )
        
        def result = pom.toString()
        if( result != orig ) {
            new XmlFormatter( pom: pom ).format()
            
            String newKey = pom.key()
            boolean needsRename = oldKey != newKey
            if( needsRename ) {
                renamed << new ArtifactRenamer( target: target, oldKey: oldKey, newKey: newKey )
            }
            
            save( pom )
            
            count ++
        }
    }
    
    void patchPom( Pom pom ) {
        set.apply( pom )
    }
    
    protected void save( Pom pom ) {
        
        def file = new File( pom.source )
        log.debug( "Patched ${file}" )
        
        pom.save( file )
    }
    
    GlobalPatches globalPatches = new GlobalPatches()
    
    void init() {
        assert target != null
        
        globalPatches.orbitExclusions << 'org.eclipse.*'
        
        set = new PatchSet()
        
        convertSnapshotVersionsToPatches()
    }
    
    void convertSnapshotVersionsToPatches() {
        File snapshotVersionMapping = new File( target, "${MT4E_FOLDER}/${SNAPSHOT_VERSION_MAPPING_FILE}" )
        if( !snapshotVersionMapping.exists() ) {
            return
        }
        
        loadSnapshotVersionsFromFile( snapshotVersionMapping )
    }
    
    void loadSnapshotVersionsFromFile( File snapshotVersionMapping ) {
        snapshotVersionMapping.eachLine( UTF_8 ) {
            def (shortKey, eclipseVersion, mavenVersion) = it.split( ' ' )
            
            String pattern = "${shortKey}:${eclipseVersion}"
            globalPatches.qualifierPatches << new QualifierPatch( pattern, mavenVersion )
            
            def version = new Version( eclipseVersion )
            pattern = "${shortKey}:[${version.shortVersion()},)"
            globalPatches.qualifierPatches << new QualifierPatch( pattern, mavenVersion )
        }
    }
    
    PatchSet deleteClasses = new PatchSet()
    
	void loadPatches( String... patches ) {
        
		set.patches << new RemoveNonOptional()
		set.patches << deleteClasses
		set.patches << new ImportDependenciesPatch( db: importExportDB )
		set.patches << new StripQualifiers( globalPatches: globalPatches, target: target )
        
        for( String patchName : patches ) {
            def loader = new PatchLoader( new File( patchName ).getAbsoluteFile(), globalPatches )
            def patch = loader.load()
            
            set.patches << patch
        }
        
        if( globalPatches.renameOrbitBundles ) {
            set.patches << new OrbitPatch( globalPatches: globalPatches, target: target )
        }
	}
    
    void deleteClasses() {
        for( DeleteClasses p : globalPatches.deleteClasses ) {
            p.repo = target
            
            deleteClasses.patches << p
        }
    }

    void deleteArtifacts() {
        for( String pattern in globalPatches.artifactsToDelete ) {
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
