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
package m4e.patch;

import static org.junit.Assert.*;
import m4e.Pom;
import m4e.XmlFormatter;
import org.junit.Test;

class ReplaceDependenciesTest {
    
    @Test
    public void testPatchScope() throws Exception {
        def pom = runTest( 'rhino:js:1.7R2:scope=test' )
        
        String expected = POM_WITH_RHINO_DEPENDENCY.replace('${opt}', '          <scope>test</scope>')
        assertEquals( expected, pom.toString() )
    }
    
    @Test
    public void testPatchScopeOptional() throws Exception {
        def pom = runTest( 'rhino:js:1.7R2:scope=test:optional=true' )
            
            String expected = POM_WITH_RHINO_DEPENDENCY.replace('${opt}', '          <optional>true</optional>\n          <scope>test</scope>')
            assertEquals( expected, pom.toString() )
    }
    
    @Test
    public void testPatchScopeOptional2() throws Exception {
        def pom = runTest( 'rhino:js:1.7R2:scope=test:optional=false' )
            
            String expected = POM_WITH_RHINO_DEPENDENCY.replace('${opt}', '          <scope>test</scope>')
            assertEquals( expected, pom.toString() )
    }
    
    @Test
    public void testPatch() throws Exception {
        def pom = runTest( 'rhino:js:1.7R2' )
            
            String expected = POM_WITH_RHINO_DEPENDENCY.replace('${opt}\n', '')
            assertEquals( expected, pom.toString() )
    }

    Pom runTest( String replacement ) {
        def pom = Pom.load( POM_WITH_JAVASCRIPT_DEPENDENCY )
        
        def op = new ReplaceDependency(
            pattern: PatchDependency.fromString( 'org.mozilla.javascript:org.mozilla.javascript:[1.6.0,2.0.0)' ),
            replacement: PatchDependency.fromString( replacement )
        )
        
        def tool = new ReplaceDependencies( defaultProfile: 'm4e.orbit', profile: 'm4e.maven-central' )
        tool.replacements << op
        
        tool.apply( pom )
        
        XmlFormatter formatter = new XmlFormatter( pom: pom )
        formatter.format()
        
        return pom
    }
    
    static final String POM_WITH_JAVASCRIPT_DEPENDENCY = '''\
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <dependencies>
    <dependency>
      <groupId>org.mozilla.javascript</groupId>
      <artifactId>org.mozilla.javascript</artifactId>
      <version>[1.6.0,2.0.0)</version>
      <optional>false</optional>
    </dependency>
  </dependencies>
</project>
'''
    
    static final String POM_WITH_RHINO_DEPENDENCY = '''\
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <dependencies>
  </dependencies>
  <profiles>
    <profile>
      <id>m4e.orbit</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <dependencies>
        <dependency>
          <groupId>org.mozilla.javascript</groupId>
          <artifactId>org.mozilla.javascript</artifactId>
          <version>[1.6.0,2.0.0)</version>
          <optional>false</optional>
        </dependency>
      </dependencies>
    </profile>
    <profile>
      <id>m4e.maven-central</id>
      <dependencies>
        <dependency>
          <groupId>rhino</groupId>
          <artifactId>js</artifactId>
          <version>1.7R2</version>
${opt}
        </dependency>
      </dependencies>
    </profile>
  </profiles>
</project>
'''
}
