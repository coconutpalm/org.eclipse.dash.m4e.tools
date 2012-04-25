package m4e;

import static org.junit.Assert.*;
import org.junit.Test;

class AttachSourcesTest {

    @Test
    public void testAttachSources() throws Exception {
        
        def workDir = CommonTestCode.prepareRepo( new File( 'data/input/attachSources' ), 'testAttachSources' )
        def repo = new File( workDir, 'm2repo' )
        
        def tool = new AttachSourcesCmd( workDir: workDir )
        tool.attachSources( repo )
        
        assert new File( repo, 'org/mozilla/javascript/org.mozilla.javascript/1.7.2.v201005080400/org.mozilla.javascript-1.7.2.v201005080400-sources.jar' ).exists()
        
        assert 3 == tool.count
        assert 0 == tool.errorCount
        assert 0 == tool.warningCount
    }
}
