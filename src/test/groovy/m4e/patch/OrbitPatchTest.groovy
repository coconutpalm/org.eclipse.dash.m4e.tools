package m4e.patch;

import static org.junit.Assert.*;
import java.io.File;
import m4e.CommonTestCode;
import m4e.MavenRepositoryTools;
import m4e.MopSetup;
import m4e.PatchCmd
import m4e.Pom;
import org.junit.Test;

public class OrbitPatchTest {

    @Test
    public void testOrbitPatch() {
        
        def target = CommonTestCode.prepareRepo( new File( 'data/input/stripQualifier' ), 'testOrbitPatch/m2repo' )
        
        def tool = new PatchCmd( workDir: target.parentFile, target: target )
        tool.init()
        
        tool.globalPatches.renameOrbitBundles = true
        tool.loadPatches()
        
        tool.applyPatches()
        
        def l = []
        MavenRepositoryTools.eachPom( target ) { it ->
            l << it.pathRelativeTo( target )
        }
        
        l.sort()
        
        assertEquals( '''\
org/eclipse/m2e/org.eclipse.m2e.logback.configuration/1.0.200/org.eclipse.m2e.logback.configuration-1.0.200.pom
org/eclipse/orbit/orbit.org.apache.batik.dom/1.6.0/orbit.org.apache.batik.dom-1.6.0.pom
org/eclipse/orbit/orbit.org.apache.batik.util/1.6.0/orbit.org.apache.batik.util-1.6.0.pom
org/eclipse/swt/org.eclipse.swt.gtk.linux.x86/3.7.1/org.eclipse.swt.gtk.linux.x86-3.7.1.pom''',
            l.join( '\n' ) )
        
        assertEquals( 0, tool.errorCount )
        assertEquals( 0, tool.warningCount )

        def actual = new File( target, 'org/eclipse/orbit/orbit.org.apache.batik.dom/1.6.0/orbit.org.apache.batik.dom-1.6.0.pom' ).getText( 'UTF-8' )
        assertEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.eclipse.orbit</groupId>
  <artifactId>orbit.org.apache.batik.dom</artifactId>
  <version>1.6.0</version>
  <name>Apache Batik DOM supplied by Eclipse Orbit</name>
  <dependencies>
    <dependency>
      <groupId>org.eclipse.orbit</groupId>
      <artifactId>orbit.org.apache.batik.css</artifactId>
      <version>[1.6.0,1.7.0)</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.orbit</groupId>
      <artifactId>orbit.org.apache.batik.util</artifactId>
      <version>[1.6.0,1.7.0)</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.orbit</groupId>
      <artifactId>orbit.org.apache.batik.xml</artifactId>
      <version>[1.6.0,1.7.0)</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.orbit</groupId>
      <artifactId>orbit.org.w3c.css.sac</artifactId>
      <version>[1.3.0,1.4.0)</version>
    </dependency>
  </dependencies>
</project>
''',
            actual )

        actual = new File( target, 'org/eclipse/orbit/orbit.org.apache.batik.util/1.6.0/orbit.org.apache.batik.util-1.6.0.pom' ).getText( 'UTF-8' )
        assertEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.eclipse.orbit</groupId>
  <artifactId>orbit.org.apache.batik.util</artifactId>
  <version>1.6.0</version>
  <name>Apache Batik Utilities supplied by Eclipse Orbit</name>
  <dependencies>
    <dependency>
      <groupId>org.eclipse.orbit</groupId>
      <artifactId>orbit.org.apache.batik.util.gui</artifactId>
      <version>[1.6.0,1.7.0)</version>
    </dependency>
  </dependencies>
</project>
''',
            actual )
            
        actual = new File( target, 'org/eclipse/m2e/org.eclipse.m2e.logback.configuration/1.0.200/org.eclipse.m2e.logback.configuration-1.0.200.pom' ).getText( 'UTF-8' )
        assertEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.eclipse.m2e</groupId>
  <artifactId>org.eclipse.m2e.logback.configuration</artifactId>
  <version>1.0.200</version>
  <name>m2e logback configuration</name>
  <dependencies>
    <dependency>
      <groupId>org.eclipse.core</groupId>
      <artifactId>org.eclipse.core.runtime</artifactId>
      <version>3.5.0</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.orbit</groupId>
      <artifactId>orbit.ch.qos.logback.classic</artifactId>
      <version>0.9.24</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.orbit</groupId>
      <artifactId>orbit.ch.qos.logback.core</artifactId>
      <version>0.9.24</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.orbit</groupId>
      <artifactId>orbit.org.slf4j.api</artifactId>
      <version>1.6.1</version>
    </dependency>
  </dependencies>
</project>
''',
actual )

    }

}
