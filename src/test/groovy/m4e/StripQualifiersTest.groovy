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

class StripQualifiersTest {
    
    @Test
    public void testNoVersion1() throws Exception {
        assertEquals( null, new StripQualifiers().stripQualifier( null ) )
    }
    
    @Test
    public void testVersion1() throws Exception {
        assertEquals( '1', new StripQualifiers().stripQualifier( '1' ) )
    }
    
    @Test
    public void testVersion1_0() throws Exception {
        assertEquals( '1.0', new StripQualifiers().stripQualifier( '1.0' ) )
    }
    
    @Test
    public void testVersion1_3_2() throws Exception {
        assertEquals( '1.3.2', new StripQualifiers().stripQualifier( '1.3.2' ) )
    }
    
    @Test
    public void testEmptyVersion() throws Exception {
        assertEquals( '', new StripQualifiers().stripQualifier( '' ) )
    }
    
    @Test
    public void testRange1() throws Exception {
        assertEquals( '[2.6.2,3.0)', new StripQualifiers().stripQualifier( '[2.6.2,3.0)' ) )
    }
    
    @Test
    public void testRange2() throws Exception {
        assertEquals( '[3.6.1,4)', new StripQualifiers().stripQualifier( '[3.6.1,4)' ) )
    }
    
    @Test
    public void testRange3() throws Exception {
        assertEquals( '[0,)', new StripQualifiers().stripQualifier( '[0,)' ) )
    }
    
    @Test
    public void testQualifier() throws Exception {
        assertEquals( '[2.5.0,3.0.0)', new StripQualifiers().stripQualifier( '[2.5.0.v200906151043,3.0.0)' ) )
    }
    
    @Test
    public void testQualifier2() throws Exception {
        assertEquals( '2.1.2', new StripQualifiers().stripQualifier( '2.1.2.v20101206-r8635' ) )
    }
    
    @Test
    public void testQualifier3() throws Exception {
        assertEquals( '[2.1.2,3.0.0)', new StripQualifiers().stripQualifier( '[2.1.2.v20101206-r8635,3.0.0)' ) )
    }
}
