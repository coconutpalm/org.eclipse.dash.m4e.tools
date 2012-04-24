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
