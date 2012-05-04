/*******************************************************************************
 * Copyright (c) 04.05.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e;

import static org.junit.Assert.*;
import m4e.patch.ImportDependenciesPatchTest;
import org.junit.Test;

class PatchCmdTest {

    @Test
    public void testGoogleInject() throws Exception {
        
        def tool = new PatchCmd()
        tool.init()
        
        tool.loadPatches( 'data/input/googleInject.patch' )
        
        def pom = Pom.load( ImportDependenciesPatchTest.EXPECTED_GOOGLE_INJECT_POM )
        tool.patchPom( pom )
        
        XmlFormatter.format( pom )
        
        assertEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.eclipse.orbit</groupId>
  <artifactId>orbit.com.google.inject</artifactId>
  <version>3.0.0</version>
  <name>guice supplied by Eclipse Orbit</name>
  <description>Guice is a lightweight dependency injection framework for Java 5 and above
    Converted with MT4E 0.13 (27.04.2012)</description>
  <url>http://code.google.com/p/google-guice/</url>
  <properties>
    <mt4e.osgi.importPackage>javax.inject;version="1.0.0"</mt4e.osgi.importPackage>
    <mt4e.osgi.exportPackage>com.google.inject;version="1.3";uses:="com.google.inject,  com.google.inject.binder,  com.google.inject.spi,  com.google.inject.matcher,  javax.inject",com.google.inject.binder;version="1.3";uses:="com.google.inject",com.google.inject.internal;version="1.3";x-internal:="true",com.google.inject.matcher;version="1.3",com.google.inject.name;version="1.3";uses:="com.google.inject",com.google.inject.spi;version="1.3";uses:="com.google.inject.binder,com.google.inject.matcher,com.google.inject",com.google.inject.util;version="1.3";uses:="com.google.inject.util,  com.google.inject.spi,  javax.inject,  com.google.inject"</mt4e.osgi.exportPackage>
  </properties>
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
          <groupId>org.eclipse.orbit</groupId>
          <artifactId>orbit.javax.inject</artifactId>
          <version>[1.0.0,)</version>
        </dependency>
      </dependencies>
    </profile>
    <profile>
      <id>m4e.maven-central</id>
      <dependencies>
        <dependency>
          <groupId>javax.inject</groupId>
          <artifactId>javax.inject</artifactId>
          <version>1</version>
        </dependency>
      </dependencies>
    </profile>
  </profiles>
</project>
''',
            pom.toString())
    }
}
