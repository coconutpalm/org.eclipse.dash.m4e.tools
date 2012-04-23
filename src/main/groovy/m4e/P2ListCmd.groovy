package m4e

import m4e.p2.P2RepoLoader;
import m4e.p2.IP2Repo;

class P2ListCmd extends AbstractCommand {

    final static String DESCRIPTION = '''\
URL
    - List the content of a P2 repository.'''
        
    void run( String... args ) {
        
        if( args.size() < 2 ) {
            throw new UserError( "Expected at least one argument: The URL of the p2 repository to list" )
        }
        
        def url = new URL( args[1] )
        log.info( 'Listing {}...', url )
        
        def loader = new P2RepoLoader( workDir: workDir, url: url )
        IP2Repo repo = loader.load()
        
        repo.list( new IndentPrinter() )
    }
}

