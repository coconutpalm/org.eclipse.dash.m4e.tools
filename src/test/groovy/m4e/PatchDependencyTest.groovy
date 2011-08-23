/*******************************************************************************
 * Copyright (c) 23.08.2011 Aaron Digulla.
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

class PatchDependencyTest {

    @Test
    public void testOneField() throws Exception {
        try {
            PatchDependency.fromString( 'a'  )
            fail( 'Expected error' )
        } catch( UserError e ) {
            assertEquals( 'Expected at least three colon-separated values: [a]', e.message )
        }
    }
    
    @Test
    public void testTwoFields() throws Exception {
        try {
            PatchDependency.fromString( 'a:b'  )
            fail( 'Expected error' )
        } catch( UserError e ) {
            assertEquals( 'Expected at least three colon-separated values: [a:b]', e.message )
        }
    }
    
    @Test
    public void testThreeFields() throws Exception {
        def d = PatchDependency.fromString( 'a:b:1'  )
        assertEquals( 'PatchDependency( a:b:1 )', d.toString() )
        assertEquals( false, d.optional )
        assertEquals( null, d.scope )
    }
    
    @Test
    public void testOptionalTrue() throws Exception {
        def d = PatchDependency.fromString( 'a:b:1:optional=true'  )
        assertEquals( 'PatchDependency( a:b:1 )', d.toString() )
        assertEquals( true, d.optional )
    }
    
    @Test
    public void testOptionalFalse() throws Exception {
        def d = PatchDependency.fromString( 'a:b:1:optional=false'  )
        assertEquals( 'PatchDependency( a:b:1 )', d.toString() )
        assertEquals( false, d.optional )
    }
}
