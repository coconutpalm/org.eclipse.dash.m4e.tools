/*******************************************************************************
 * Copyright (c) 24.04.2012 Aaron Digulla.
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
import org.junit.Test;

class QualifierPatchTest {

    @Test
    public void testAppliesText() throws Exception {
        
        QualifierPatch p = new QualifierPatch( 'a:b:c', '1.0.0' )
        
        assertEquals( true, p.appliesTo( 'a:b:c' ) )
        assertEquals( false, p.appliesTo( 'x:b:c' ) )
        assertEquals( false, p.appliesTo( 'a:x:c' ) )
        assertEquals( false, p.appliesTo( 'a:b:x' ) )
    }
    
    @Test
    public void testAppliesPattern() throws Exception {
        
        QualifierPatch p = new QualifierPatch( 'a:*:c', '1.0.0' )
        
        assertEquals( true, p.appliesTo( 'a:b:c' ) )
        assertEquals( false, p.appliesTo( 'x:b:c' ) )
        assertEquals( true, p.appliesTo( 'a:x:c' ) )
        assertEquals( false, p.appliesTo( 'a:b:x' ) )
    }
    
    @Test
    public void testAppliesPattern_2() throws Exception {
        
        QualifierPatch p = new QualifierPatch( 'a:b*:c', '1.0.0' )
        
        assertEquals( true, p.appliesTo( 'a:b:c' ) )
        assertEquals( true, p.appliesTo( 'a:bx:c' ) )
        assertEquals( false, p.appliesTo( 'a:x:c' ) )
    }
}
