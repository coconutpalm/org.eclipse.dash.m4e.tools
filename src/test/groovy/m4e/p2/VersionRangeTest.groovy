/*******************************************************************************
 * Copyright (c) 23.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e.p2

import static org.junit.Assert.*;

import org.junit.Test;

import m4e.MopSetup;

class VersionRangeTest {
    
    static {
        MopSetup.setup()
    }
    
    @Test
    public void testEmptyVersionRange() throws Exception {
        def range = new VersionRange( null )
        assertEquals( '', range.toString() )
    }
    
    @Test
    public void testVersionRangeCompareEmpty() throws Exception {
        assertCompare( 0, new Version( null ), new Version( null ) )
    }
    
    private void assertCompare( int expected, Comparable c1, Comparable c2 ) {
        int d = c1.compareTo( c2 )
        d = Math.signum( d )
        assertEquals( "Comparing ${c1} and ${c2}", expected, d )
    }
    
    @Test
    public void testVersionRangeCompareBlank() throws Exception {
        assertCompare( 0, new Version( null ), new Version( '' ) )
    }
    
    @Test
    public void testVersionRangeCompareBlank_2() throws Exception {
        assertCompare( 1, new Version( '1' ), new Version( '' ) )
    }
    
    @Test
    public void testVersionRangeCompareBlank_3() throws Exception {
        assertCompare( -1, new Version( '' ), new Version( '1' ) )
    }
    
    @Test
    public void testVersionRangeCompareWithoutQualifier() throws Exception {
        assertCompare( 1, new Version( '1.0.0' ), new Version( '0.0.0' ) )
    }
    
    @Test
    public void testVersionRangeCompareWithoutQualifier_2() throws Exception {
        assertCompare( 1, new Version( '3.1.0' ), new Version( '0.0.0' ) )
    }
    
    @Test
    public void testVersionRangeCompareWithoutQualifier_3() throws Exception {
        assertCompare( 0, new Version( '3.1.0' ), new Version( '3.1.0' ) )
    }
    
    @Test
    public void testVersionRangeCompareWithoutQualifier_4() throws Exception {
        assertCompare( -1, new Version( '1.0.0' ), new Version( '1.0.1' ) )
    }
    
    @Test
    public void testVersionRangeCompareWithQualifier() throws Exception {
        assertCompare( 0, new Version( '1.0.0.v20120402' ), new Version( '1.0.0.v20120402' ) )
        assertCompare( 1, new Version( '1.0.0.v20120402' ), new Version( '1.0.0.v20120401' ) )
        assertCompare( -1, new Version( '1.0.0.v20120402' ), new Version( '1.0.0.v20120405' ) )
    }
    
    @Test
    public void testVersionRangeCompareMixed() throws Exception {
        assertCompare( 1, new Version( '1.0.0.v20120402' ), new Version( '1.0.0' ) )
        assertCompare( -1, new Version( '1.0.0' ), new Version( '1.0.0.v20120401' ) )
    }
    
    @Test
    public void testNextVersion() throws Exception {
        assertEquals( "1.0.1", new Version( '1.0.0.v20120402' ).next().toString() )
    }
    
    @Test
    public void testVersionRange() throws Exception {
        def range = new VersionRange( '[0.0.0,3.0.1.v20110824)' )
        assertEquals( '[0.0.0,3.0.1.v20110824)', range.toString() )
        assertEquals( true, range.includeLower )
        assertEquals( '0.0.0', range.lower.toString() )
        assertEquals( '3.0.1.v20110824', range.upper.toString() )
        assertEquals( false, range.includeUpper )
        
        assertEquals( false, range.upper.equals( range.lower ) )
        assertTrue( range.upper.compareTo( range.lower ) > 0 )
        assertTrue( range.lower.compareTo( range.upper ) < 0 )
    }
    
    @Test
    public void testVersionRange_2() throws Exception {
        def range = new VersionRange( '[1.0.0.v20111230-0120,1.0.0.v20111230-0120]' )
        assertEquals( '[1.0.0.v20111230-0120,1.0.0.v20111230-0120]', range.toString() )
        assertEquals( true, range.includeLower )
        assertEquals( '1.0.0.v20111230-0120', range.lower.toString() )
        assertEquals( '1.0.0.v20111230-0120', range.upper.toString() )
        assertEquals( true, range.includeUpper )
        
        assertEquals( range.lower, range.upper )
        assertEquals( 0, range.upper.compareTo( range.lower ) )
    }

    @Test
    public void testVersionRange_3() throws Exception {
        def range = new VersionRange( '1' )
        assertEquals( false, range.includeLower )
        assertEquals( '1', range.lower.toString() )
        assertEquals( null, range.upper )
        assertEquals( false, range.includeUpper )
        assertEquals( '1', range.toString() )
    }

    @Test
    public void testVersionRange_Contains() throws Exception {
        def range = new VersionRange( '[0.0.0,3.0.1.v20110824)' )
        
        assertEquals( true, range.contains( new Version( '1.0.0' ) ) )
        assertEquals( true, range.contains( new Version( '0.0.0' ) ) )
        assertEquals( true, range.contains( new Version( '3.0.0' ) ) )
        assertEquals( true, range.contains( new Version( '3.0.1' ) ) )
        assertEquals( true, range.contains( new Version( '3.0.1.v20110823' ) ) )
        assertEquals( false, range.contains( new Version( '3.0.1.v20110824' ) ) )
        assertEquals( false, range.contains( new Version( '3.0.2' ) ) )
    }

}
