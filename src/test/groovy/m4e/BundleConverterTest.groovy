/*******************************************************************************
 * Copyright (c) 26.04.2012 Aaron Digulla.
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
import java.util.jar.Manifest;
import org.junit.Test;

class BundleConverterTest {

    @Test
    public void testConvert() throws Exception {
        
        assertEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.pany.eclipse</groupId>
  <artifactId>com.pany.eclipse.bundle</artifactId>
  <version>2.2.0.v20120426_155010</version>
  <name>Bundle</name>
  <description>Converted with MT4E 0.14-SNAPSHOT (16.05.2012)</description>
  <properties>
    <mt4e.osgi.importPackage>org.apache.commons.logging,org.apache.log4j,org.eclipse.xtend2.lib,org.eclipse.xtext.xtend2.lib</mt4e.osgi.importPackage>
    <mt4e.osgi.exportPackage>com.pany.eclipse.bundle,com.pany.eclipse.bundle.x,com.pany.eclipse.bundle.y</mt4e.osgi.exportPackage>
    <mt4e.osgi.isSingleton>true</mt4e.osgi.isSingleton>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.eclipse.xtext</groupId>
      <artifactId>org.eclipse.xtext</artifactId>
      <version>[2.2.1,)</version>
    </dependency>
    <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>org.apache.commons.logging</artifactId>
      <version>[1.1.1,)</version>
      <optional>true</optional>
    </dependency>
    <dependency>
      <groupId>org.eclipse.emf</groupId>
      <artifactId>org.eclipse.emf.mwe2.launch</artifactId>
      <version>[0,)</version>
      <optional>true</optional>
    </dependency>
    <dependency>
      <groupId>org.eclipse.xtext</groupId>
      <artifactId>org.eclipse.xtext.util</artifactId>
      <version>[2.2.1,)</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.core</groupId>
      <artifactId>org.eclipse.core.resources</artifactId>
      <version>[3.7.101,4.0)</version>
    </dependency>
  </dependencies>
</project>
'''
            , convert( MANIFEST ) )
    }
    
    @Test
    public void testConvertComGoogleInject() throws Exception {
        
        assertEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.google.inject</groupId>
  <artifactId>com.google.inject</artifactId>
  <version>3.0.0.no_aop</version>
  <name>guice</name>
  <description>Guice is a lightweight dependency injection framework for Java 5 and above
    Converted with MT4E 0.14-SNAPSHOT (16.05.2012)</description>
  <url>http://code.google.com/p/google-guice/</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    </license>
  </licenses>
  <properties>
    <mt4e.osgi.importPackage>javax.inject;version="1.0.0"</mt4e.osgi.importPackage>
    <mt4e.osgi.exportPackage>com.google.inject;version="1.3";uses:="com.google.inject,  com.google.inject.binder,  com.google.inject.spi,  com.google.inject.matcher,  javax.inject",com.google.inject.binder;version="1.3";uses:="com.google.inject",com.google.inject.internal;version="1.3";x-internal:="true",com.google.inject.matcher;version="1.3",com.google.inject.name;version="1.3";uses:="com.google.inject",com.google.inject.spi;version="1.3";uses:="com.google.inject.binder,com.google.inject.matcher,com.google.inject",com.google.inject.util;version="1.3";uses:="com.google.inject.util,  com.google.inject.spi,  javax.inject,  com.google.inject"</mt4e.osgi.exportPackage>
  </properties>
</project>
'''
            , convert( MANIFEST_COM_GOOGLE_INJECT ) )
    }

    private String convert( String manifest ) {
        def m = new Manifest( new ByteArrayInputStream( manifest.getBytes( 'UTF-8' ) ) )
        
        def statistics = new ImportStatistics()
        
        def tool = new BundleConverter( manifest: m, statistics: statistics )
        tool.examineManifest()
        
        def buffer = new StringWriter()
        tool.createPom( buffer )
        
        return buffer.toString()
    }
    
    private final static String MANIFEST = '''\
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: Bundle
Bundle-Vendor: My Company
Bundle-Version: 2.2.0.v20120426_155010
Bundle-SymbolicName: com.pany.eclipse.bundle; singleton:=true
Bundle-ActivationPolicy: lazy
Require-Bundle: org.eclipse.xtext;bundle-version="2.2.1";visibility:=reexport,
 org.apache.commons.logging;bundle-version="1.1.1";resolution:=optional;visibility:=reexport,
 org.eclipse.emf.mwe2.launch;resolution:=optional,
 org.eclipse.xtext.util;bundle-version="2.2.1",
 org.eclipse.core.resources;bundle-version="[3.7.101,4.0)"
Import-Package: org.apache.commons.logging,
 org.apache.log4j,
 org.eclipse.xtend2.lib,
 org.eclipse.xtext.xtend2.lib
Bundle-RequiredExecutionEnvironment: JavaSE-1.6
Export-Package: com.pany.eclipse.bundle,
 com.pany.eclipse.bundle.x,
 com.pany.eclipse.bundle.y
'''
    
    private final static String MANIFEST_COM_GOOGLE_INJECT = '''\
Manifest-Version: 1.0
Bundle-DocURL: http://code.google.com/p/google-guice/
Bundle-RequiredExecutionEnvironment: J2SE-1.5,JavaSE-1.6
Bundle-SymbolicName: com.google.inject
Export-Package: com.google.inject;version="1.3"; uses:="com.google.inj
 ect,  com.google.inject.binder,  com.google.inject.spi,  com.google.i
 nject.matcher,  javax.inject",com.google.inject.binder;version="1.3";
 uses:="com.google.inject",com.google.inject.internal;version="1.3";x-
 internal:=true,com.google.inject.matcher;version="1.3",com.google.inj
 ect.name;version="1.3";uses:="com.google.inject",com.google.inject.sp
 i;version="1.3";uses:="com.google.inject.binder,com.google.inject.mat
 cher,com.google.inject",com.google.inject.util;version="1.3"; uses:="
 com.google.inject.util,  com.google.inject.spi,  javax.inject,  com.g
 oogle.inject"
Bundle-Version: 3.0.0.no_aop
Bundle-Description: Guice is a lightweight dependency injection framew
 ork for Java 5 and above
Bundle-ClassPath: .
Ant-Version: Apache Ant 1.7.1
Bundle-Vendor: Eclipse.org
Bundle-Name: guice
Bundle-License: http://www.apache.org/licenses/LICENSE-2.0.txt
Created-By: 1.6.0_23 (Sun Microsystems Inc.)
Bundle-Copyright: Copyright (C) 2006 Google Inc.
Import-Package: javax.inject;version="1.0.0"
Bundle-ManifestVersion: 2
'''

}
