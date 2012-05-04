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

package m4e.p2;

import static org.junit.Assert.*;

import org.junit.Test;

import m4e.MopSetup;

class VersionTest {
    
    static {
        MopSetup.setup()
    }
    
    @Test
    public void testEmptyVersion() throws Exception {
        def version = new Version( null )
        assertEquals( '', version.toString() )
    }
    
    @Test
    public void testVersion_1() throws Exception {
        def version = new Version( '1' )
        assertEquals( '1', version.toString() )
        assertEquals( 1, version.major )
        assertEquals( 0, version.minor )
        assertEquals( 0, version.service )
        assertEquals( null, version.qualifier )
        assertEquals( '1.0.1', version.next().toString() )
    }
    
    @Test
    public void testVersion_1_2() throws Exception {
        def version = new Version( '1.2' )
        assertEquals( '1.2', version.toString() )
        assertEquals( 1, version.major )
        assertEquals( 2, version.minor )
        assertEquals( 0, version.service )
        assertEquals( null, version.qualifier )
        assertEquals( '1.2.1', version.next().toString() )
    }
    
    @Test
    public void testVersion_1_2_3() throws Exception {
        def version = new Version( '1.2.3' )
        assertEquals( '1.2.3', version.toString() )
        assertEquals( 1, version.major )
        assertEquals( 2, version.minor )
        assertEquals( 3, version.service )
        assertEquals( null, version.qualifier )
    }

    @Test
    public void testVersionWithQualifier() throws Exception {
        def version = new Version( '0.8.2.v201202150957' )
        assertEquals( '0.8.2.v201202150957', version.toString() )
        assertEquals( 0, version.major )
        assertEquals( 8, version.minor )
        assertEquals( 2, version.service )
        assertEquals( 'v201202150957', version.qualifier )
    }
    
    @Test
    public void testNegativeMajor() throws Exception {
        try {
            new Version( '-1' )
        } catch( VersionException e ) {
            assertEquals( "Error parsing version '-1': major must be > 0: -1", e.message )
        }
    }
    
    @Test
    public void testNegativeMinor() throws Exception {
        try {
            new Version( '0.-17' )
        } catch( VersionException e ) {
            assertEquals( "Error parsing version '0.-17': minor must be > 0: -17", e.message )
        }
    }
    
    @Test
    public void testNegativeService() throws Exception {
        try {
            new Version( '0.0.-703' )
        } catch( VersionException e ) {
            assertEquals( "Error parsing version '0.0.-703': service must be > 0: -703", e.message )
        }
    }
    
    @Test
    public void testTooBig() throws Exception {
        try {
            new Version( "${Integer.MAX_VALUE}.0.1" )
        } catch( VersionException e ) {
            assertEquals( "Error parsing version '2147483647.0.1': major must be < 1048575: 2147483647", e.message )
        }
    }

}
