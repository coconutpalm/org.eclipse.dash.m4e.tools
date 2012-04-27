/*******************************************************************************
 * Copyright (c) 30.03.2012 Aaron Digulla.
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

class P2ListCmdTest {
    
    static {
        MopSetup.setup()
    }
    
    @Test
    public void testListM2e () throws Exception {
        
        def workDir = CommonTestCode.newFile( 'testListM2e' )
        assert workDir.deleteDir(), "Failed to delete ${workDir.absolutePath}"
        workDir.makedirs()

        def cmd = new P2ListCmd( workDir: workDir )
        
        cmd.run([ 'p2ls', '--ui', 'text', 'http://download.eclipse.org/technology/m2e/releases/1.0/1.0.200.20111228-1245' ])
        
        assertEquals( 0, cmd.errorCount )
        assertEquals( 0, cmd.warningCount )
    }
}
