package m4e

import m4e.ui.M2RepoView;

class ShowRepoCmd extends AbstractCommand {

    static final String DESCRIPTION = '''\
repository
- Show the content of an Maven 2 repository in a Swing UI
'''
    
    @Override
    public void run( String... args ) {
        
        File repo = repoOption( args, 1 )
        def view = new M2RepoView( repo: repo )
        view.show()
    }

}
