package m4e

class ConvertCmd extends AbstractCommand {

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
                
        importArchives( downloads )
        
        mergeRepos()
        
        attachSources()
        
        // TODO Apply patches
        // TODO Analyze
        // TODO Create DM
        
        log.info( "The converted Maven repository can be found here: ${targetRepo}" )
    }
    
    String groupId
    String artifactId
    String version
    File dmPath
    
    void checkDependencyManagementInfo( String info ) {
        String[] parts = info.split( ':' )
        if( parts.size() != 3 ) {
            throw new UserError( "Expected argument format 'groupId:artifactId:version' but was '${info}'" )
        }
        
        groupId = parts[0]
        artifactId = parts[1]
        version = parts[2]
        
        dmPath = new File( targetRepo, groupId.replace( '.', '/' ) )
        dmPath = new File( dmPath, artifactId )
        dmPath = new File( dmPath, version )
        dmPath = new File( dmPath, "${artifactId}-${version}.pom" )
        
        log.info( "Will write dependencyManagement info to ${dmPath}" )
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
}
