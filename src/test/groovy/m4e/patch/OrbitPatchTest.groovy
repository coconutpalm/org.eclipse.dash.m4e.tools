package m4e.patch;

import static org.junit.Assert.*;
import java.io.File;
import m4e.MavenRepositoryTools;
import m4e.MopSetup;
import m4e.PatchCmd
import m4e.Pom;
import org.junit.Test;

public class OrbitPatchTest {

    static final File testFolder = new File( "tmp-test" )

    @Test
    public void testOrbitPatch() {
        
        MopSetup.setup()

        File template = new File( 'data/input/stripQualifier' )
        File target = new File( testFolder, 'testOrbitPatch/m2repo' )
        assert target.deleteDir()
        
        template.copy( target )
        
        def tool = new PatchCmd( workDir: target.parentFile, target: target )
        tool.init()
        
        tool.globalPatches.renameOrbitBundles = true
        
        tool.loadPatches()
        
        tool.applyPatches()
        
        def l = []
        MavenRepositoryTools.eachPom( target ) { it ->
            l << it.pathRelativeTo( target )
        }
        
        assertEquals( '''\
org/eclipse/swt/org.eclipse.swt.gtk.linux.x86/3.7.1/org.eclipse.swt.gtk.linux.x86-3.7.1.pom
org/eclipse/orbit/orbit.org.apache.batik.dom/1.6.0/orbit.org.apache.batik.dom-1.6.0.pom
org/eclipse/orbit/orbit.org.apache.batik.util/1.6.0/orbit.org.apache.batik.util-1.6.0.pom''',
            l.join( '\n' ) )
    }

}
