package m4e

class CommonTestCode {

    static {
        MopSetup.setup()
    }
    
    static final File testDir = new File( 'tmp-test' )
    
    static File prepareRepo( File template, String repoName ) {
        
        File copy = newFile( repoName )
        assert copy.deleteDir()
        
        template.copy( copy )
        
        return copy
    }
    
    static File newFile( String path ) {
        return new File( testDir, path )
    }
}
