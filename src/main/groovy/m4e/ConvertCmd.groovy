/*******************************************************************************
 * Copyright (c) 26.07.2011 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/
package m4e

class ConvertCmd extends AbstractCommand {

    final static String DESCRIPTION = '''\
groupId:artifactId:version patches...
- Convert everything in the directory "downloads" into one big Maven repository
    
The first argument is used to create a POM file with a dependencyManagement element.'''
    
    void run( String... args ) {
        File downloads = new File( 'downloads' ).absoluteFile
        if( !downloads.exists() ) {
            throw new UserError( "Missing directory ${downloads}. Please create it and copy all files into it that you want to convert." )
        }
        
        if( args.size() != 2 ) {
            throw new UserError( "Expected one argument: The groupId:artifactId:version of a POM in which to write the dependencyManagement element" )
        }
        
        targetRepo = new File( workDir, 'm2repo' )
        
        log.info( "Converting everything in ${downloads} into ${targetRepo}" )

        checkDependencyManagementInfo( args[1] )
        
        List<String> patches = args[2..<args.size()] as List
                
        importArchives( downloads )
        
        mergeRepos()
        
        attachSources()
        
        applyPatches( patches )
        
        analyze()
        
        createDependencyManagement()
        
        log.info( "The converted Maven repository can be found here: ${targetRepo}" )
    }
    
    String dmInfo
    
    void checkDependencyManagementInfo( String info ) {
        String[] parts = info.split( ':' )
        if( parts.size() != 3 ) {
            throw new UserError( "Expected argument format 'groupId:artifactId:version' but was '${info}'" )
        }
        
        dmInfo = info
    }
    
    File targetRepo
    List<File> m2repos = []
    
    void importArchives( File file ) {
        if( file.isDirectory() ) {
            file.eachFile { importArchives( it ) }
        } else {
            def cmd = new InstallCmd( workDir: workDir )
            cmd.run( 'import', file.absolutePath )
            
            m2repos.addAll( cmd.m2repos )
        }
    }
    
    void mergeRepos() {
        if( !targetRepo.deleteDir() ) {
            throw new RuntimeException( "Error deleting ${targetRepo}" )
        }
        
        def cmd = new MergeCmd( workDir: workDir )
        
        List<String> args = []
        args << 'merge'
        
        for( File file : m2repos ) {
            args << file.absolutePath
        }
        
        args << targetRepo.absolutePath
        
        cmd.run( args )
    }
    
    void attachSources() {
        def cmd = new AttachSourcesCmd( workDir: workDir )
        cmd.run( 'as', targetRepo.absolutePath )
    }
    
    void applyPatches( List<String> patches ) {
        List args = [ 'ap', targetRepo.absolutePath ]
        args << patches
        
        def cmd = new PatchCmd( workDir: workDir )
        cmd.run( args as String[] )
    }
    
    void analyze() {
        def cmd = new AnalyzeCmd( workDir: workDir )
        cmd.run( 'an', targetRepo.absolutePath )
    }
    
    void createDependencyManagement() {
        def cmd = new DependencyManagementCmd( workDir: workDir )
        cmd.run( 'dm',  targetRepo.absolutePath, dmInfo )
    }
}
