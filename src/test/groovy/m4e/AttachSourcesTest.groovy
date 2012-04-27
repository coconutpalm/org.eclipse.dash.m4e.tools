/*******************************************************************************
 * Copyright (c) 25.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

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
