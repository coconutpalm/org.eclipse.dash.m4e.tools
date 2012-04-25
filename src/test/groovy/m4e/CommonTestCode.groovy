package m4e

class CommonTestCode {

    static {
        MopSetup.setup()
    }
    
    static final File testDir = new File( 'tmp-test' )
    
    static File prepareRepo( File template, String repoName ) {
        
        File copy = new File( testDir, repoName )
        assert copy.deleteDir()
        
        template.copy( copy )
        
        return copy
    }
}
