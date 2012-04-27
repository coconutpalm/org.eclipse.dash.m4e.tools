package m4e;

import static org.junit.Assert.*;
import org.junit.Test;

class GlobTest {

    @Test
    public void testNoPattern() throws Exception {
        
        def text = 'a6,.;:-_<>\\+"*%&/(){}'
        def g = new Glob( text )
        
        assert g.matches( text )
    }
    
    @Test
    public void testPattern() throws Exception {
        
        def text = 'a*6,.;:-_<>\\+"*%&/(){}'
        def g = new Glob( text )
        
        assert g.matches( 'a6,.;:-_<>\\+"*%&/(){}' )
        assert g.matches( 'aa6,.;:-_<>\\+"*%&/(){}' )
        assert g.matches( 'a66,.;:-_<>\\+"*%&/(){}' )
        assert g.matches( 'ax6,.;:-_<>\\+"*%&/(){}' )
    }
    
    @Test
    public void testPatternPomKey() throws Exception {
        
        def text = 'org.apache.batik:org.apache.batik.pdf:1.6.0*'
        def g = new Glob( text )
        
        assert g.matches( 'org.apache.batik:org.apache.batik.pdf:1.6.0' )
        assert g.matches( 'org.apache.batik:org.apache.batik.pdf:1.6.0_v2012...' )
    }
    
    @Test
    public void testPatternPath() throws Exception {
        
        def text = 'org/apache/commons/*'
        def g = new Glob( text )
        
        assert g.matches( 'org/apache/commons/io/CopyUtils.class' )
        assert g.matches( 'org/apache/commons/io/output/ByteArrayOutputStream.class' )
        assert g.matches( 'org/apache/commons/logging/Log.class' )
        assert g.matches( 'org/apache/commons/' )
        assert g.matches( 'org/apache/commons/logging/' )
    }
    
    @Test
    public void testManyRegexp() throws Exception {
        
        def text = 'org.apache.commons:*:*'
        def g = new Glob( text, '[^ :]*' )
        
        assert g.matches( 'org.apache.commons::' )
        assert g.matches( 'org.apache.commons:a:' )
        assert g.matches( 'org.apache.commons::a' )
        assert ! g.matches( 'org.apache.commons::a:' )
    }
}
