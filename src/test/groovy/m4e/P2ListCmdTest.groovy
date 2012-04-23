package m4e;

import static org.junit.Assert.*;

import org.junit.Test;

class P2ListCmdTest {
    
    static {
        MopSetup.setup()
    }
    
    @Test
    public void testListM2e () throws Exception {
        
        def workDir = new File( 'tmp-test/testListM2e' )
        assert workDir.deleteDir(), "Failed to delete ${workDir.absolutePath}"
        workDir.makedirs()

        def cmd = new P2ListCmd( workDir: workDir )
        
        cmd.run([ 'p2ls', 'http://download.eclipse.org/technology/m2e/releases/1.0/1.0.200.20111228-1245' ])
    }
}
