package m4e.maven;

import static org.junit.Assert.*;
import org.junit.Test;

class VersionUtilsTest {

    @Test
    public void testSort() throws Exception {
        def l = [ '1.0', '10.0' ]
        l.permutations().each {
            assert '[1.0, 10.0]' == VersionUtils.sort( it ).toString()
        }
    }
    
    @Test
    public void testSort_2() throws Exception {
        def l = [ '1.10', '10.0' ]
        l.permutations().each {
            assert '[1.10, 10.0]' == VersionUtils.sort( it ).toString()
        }
    }
    
    @Test
    public void testSort_3() throws Exception {
        def l = [ '2.0', '1.5', '1.10', '10.0', '1.17.1' ]
        l.permutations().each {
            assert '[1.5, 1.10, 1.17.1, 2.0, 10.0]' == VersionUtils.sort( it ).toString()
        }
    }
    
    @Test
    public void testSort_4() throws Exception {
        def l = [ '1.a2', '1.b1', '1.a10' ]
        l.permutations().each {
            assert '[1.a10, 1.a2, 1.b1]' == VersionUtils.sort( it ).toString()
        }
    }
}
