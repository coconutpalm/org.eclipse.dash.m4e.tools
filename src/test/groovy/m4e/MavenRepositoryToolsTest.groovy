package m4e;

import static org.junit.Assert.*;
import org.junit.Test;

public class MavenRepositoryToolsTest {

    File repo1 = new File( 'data/input/repo1' )
    
    @Test
    public void testBuildPath() {
        MopSetup.setup()
        
        File file = MavenRepositoryTools.buildPath( repo1, 'org.eclipse.core:org.eclipse.core.runtime:3.5.0' )
        
        assertEquals( 'data/input/repo1/org/eclipse/core/org.eclipse.core.runtime/3.5.0', file.normalize() )
        assertTrue( file.exists() )
        assertTrue( file.isDirectory() )
    }
    
    @Test
    public void testBuildPathPom() {
        MopSetup.setup()
        
        File file = MavenRepositoryTools.buildPath( repo1, 'org.eclipse.core:org.eclipse.core.runtime:3.5.0', 'pom' )
        
        assertEquals( 'data/input/repo1/org/eclipse/core/org.eclipse.core.runtime/3.5.0/org.eclipse.core.runtime-3.5.0.pom', file.normalize() )
        assertTrue( file.exists() )
        assertTrue( file.isFile() )
    }

}
