package m4e

import java.io.PrintWriter;
import java.util.List;

import m4e.p2.P2RepoLoader;
import m4e.p2.IP2Repo;
import m4e.p2.ui.P2RepoView


class P2ListCmd extends AbstractCommand {

    final static String DESCRIPTION = '''\
URL
    - List the content of a P2 repository.'''
        
    void run( String... args ) {
        
        def url = findURL( args )
        log.info( 'Listing {}...', url )
        
        def loader = new P2RepoLoader( workDir: workDir, url: url )
        IP2Repo repo = loader.load()
        
        String ui = findUI( args )
        
        switch( ui ) {
            case 'text': listToConsole( repo ); break;
            case 'swing': listToSwing( repo ); break;
            default: throw new UserError( "Unsupported UI '${ui}'" )
        }
    }
    
    void listToSwing( IP2Repo repo ) {
        def view = new P2RepoView( repo )
        view.show()
    }
    
    void listToConsole( IP2Repo repo ) {
        def writer = new PrintWriter( System.out )
        def out = new IndentPrinter( writer, '    ' )
        repo.list( out )
        
        writer.flush()
    }
    
    URL findURL( String[] args ) {
        String s = args.find { it.contains( '://' ) }
        if( s ) {
            return new URL( s )
        }
        
        throw new UserError( "Expected at least one argument: The URL of the p2 repository to list" )
    }
    
    String findUI( String[] args ) {
        int pos = Arrays.asList( args ).indexOf( '--ui' )
        if( -1 == pos ) {
            return 'text'
        }
        
        pos ++
        if( pos == args.size() ) {
            throw new UserError( "Missing argument to option --ui" )
        }
        
        String ui = args[ pos ]
        return ui
    }
}
