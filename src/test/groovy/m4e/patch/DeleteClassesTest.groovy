/*******************************************************************************
 * Copyright (c) 27.07.2011 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e.patch;

import static org.junit.Assert.*;
import m4e.CommonTestCode;
import m4e.Pom;
import org.junit.Test;

class DeleteClassesTest {

    @Test
    public void testDeleteCommonsFromBatikPDF() throws Exception {
        
        File root = CommonTestCode.prepareRepo( new File( 'data/input/deleteClasses' ), 'testDeleteCommonsFromBatikPDF' )
        File repo = new File( root, 'm2repo' )
        
        def pom = Pom.load( new File( repo, 'org/apache/batik/org.apache.batik.pdf/1.6.0.v201105071520/org.apache.batik.pdf-1.6.0.v201105071520.pom' ) )
        
        def pattern = [ 'org/apache/commons/*' ]
        def tool = new DeleteClasses( 'org.apache.batik:org.apache.batik.pdf:1.6.0*', pattern )
        tool.repo = repo
        
        tool.apply( pom )
        
        assertEquals( 29, tool.count )
    }
}
