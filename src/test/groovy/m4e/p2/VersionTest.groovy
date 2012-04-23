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
    public void testShortVersion() throws Exception {
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

}
