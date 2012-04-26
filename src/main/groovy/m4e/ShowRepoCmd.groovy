package m4e

import m4e.ui.M2RepoView;

class ShowRepoCmd extends AbstractCommand {

    static final String DESCRIPTION = '''\
repository
- Show the content of an Maven 2 repository in a Swing UI
'''
    
    @Override
    public void run( String... args ) {
        
        if( args.size() == 1 ) {
            throw new UserError( 'Missing path to repository to analyze' )
        }
        
        File repo = new File( args[1] ).absoluteFile
        if( !repo.exists() ) {
            throw new UserError( "Directory ${repo} doesn't exist" )
        }

        def view = new M2RepoView( repo: repo )
        view.show()
    }

}
