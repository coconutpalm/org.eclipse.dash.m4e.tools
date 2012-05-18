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

    @Test
    public void testConvertSourceBundles() throws Exception {
        
        File source = CommonTestCode.prepareRepo( new File( 'data/input/importSourceBundles' ), 'testConvertSourceBundles' )
        File workDir = new File( source, 'tmp' )
        File repo = new File( source, 'm2repo' )
        File plugins = new File( source, 'plugins' )
        
        InstallCmd cmd = new InstallCmd( workDir: workDir )
        
        ImportTool tool = new ImportTool( m2repo: repo, installCmd: cmd )
        tool.versionMap.m2repo = repo
        
        tool.doImport( new File( plugins, 'de.itemis.xtext.typesystem.source_2.0.5.201205161310.jar' ) )
        tool.doImport( new File( plugins, 'de.itemis.xtext.typesystem_2.0.5.201205161310.jar' ) )
        tool.doImport( new File( plugins, 'org.apache.commons.lang3_3.1.0.jar' ) )
        tool.doImport( new File( plugins, 'org.apache.commons.lang3.sources_3.1.0.jar' ) )
        
        tool.versionMap.close()
        
        // Sources not yet in the right place
        assertEquals( '''\
.mt4e/snapshotVersionMapping
de/itemis/xtext/de.itemis.xtext.typesystem/2.0.5-SNAPSHOT/de.itemis.xtext.typesystem-2.0.5-SNAPSHOT.jar
de/itemis/xtext/de.itemis.xtext.typesystem/2.0.5-SNAPSHOT/de.itemis.xtext.typesystem-2.0.5-SNAPSHOT.pom
de/itemis/xtext/de.itemis.xtext.typesystem/2.0.5.201205161310/de.itemis.xtext.typesystem-2.0.5.201205161310-sources.jar
org/apache/commons/org.apache.commons.lang3/3.1.0/org.apache.commons.lang3-3.1.0-sources.jar
org/apache/commons/org.apache.commons.lang3/3.1.0/org.apache.commons.lang3-3.1.0.jar
org/apache/commons/org.apache.commons.lang3/3.1.0/org.apache.commons.lang3-3.1.0.pom''',
            CommonTestCode.listFiles( repo ) )
        
        tool.moveSourceBundles()
        
        assertEquals( '''\
.mt4e/snapshotVersionMapping
de/itemis/xtext/de.itemis.xtext.typesystem/2.0.5-SNAPSHOT/de.itemis.xtext.typesystem-2.0.5-SNAPSHOT-sources.jar
de/itemis/xtext/de.itemis.xtext.typesystem/2.0.5-SNAPSHOT/de.itemis.xtext.typesystem-2.0.5-SNAPSHOT.jar
de/itemis/xtext/de.itemis.xtext.typesystem/2.0.5-SNAPSHOT/de.itemis.xtext.typesystem-2.0.5-SNAPSHOT.pom
org/apache/commons/org.apache.commons.lang3/3.1.0/org.apache.commons.lang3-3.1.0-sources.jar
org/apache/commons/org.apache.commons.lang3/3.1.0/org.apache.commons.lang3-3.1.0.jar
org/apache/commons/org.apache.commons.lang3/3.1.0/org.apache.commons.lang3-3.1.0.pom''',
            CommonTestCode.listFiles( repo ) )
        
        CommonTestCode.fileEquals( '''\
de.itemis.xtext:de.itemis.xtext.typesystem 2.0.5.201205161310 2.0.5-SNAPSHOT''', new File( repo, '.mt4e/snapshotVersionMapping' ) )
        
        CommonTestCode.fileEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>de.itemis.xtext</groupId>
  <artifactId>de.itemis.xtext.typesystem</artifactId>
  <version>2.0.5-SNAPSHOT</version>
  <name>Typesystem</name>
  <description>Converted with MT4E 0.14-SNAPSHOT (16.05.2012)</description>
  <properties>
    <mt4e.osgi.exportPackage>de.itemis.xtext.typesystem,de.itemis.xtext.typesystem.characteristics,de.itemis.xtext.typesystem.checks,de.itemis.xtext.typesystem.checks.custom,de.itemis.xtext.typesystem.exceptions,de.itemis.xtext.typesystem.rules,de.itemis.xtext.typesystem.trace,de.itemis.xtext.typesystem.util</mt4e.osgi.exportPackage>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.eclipse.ui</groupId>
      <artifactId>org.eclipse.ui</artifactId>
      <version>[3.7.0,)</version>
      <optional>true</optional>
    </dependency>
    <dependency>
      <groupId>org.eclipse.core</groupId>
      <artifactId>org.eclipse.core.runtime</artifactId>
      <version>[3.7.0,)</version>
    </dependency>
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
      <artifactId>org.eclipse.emf.mwe.utils</artifactId>
      <version>[1.2.1,)</version>
      <optional>true</optional>
    </dependency>
    <dependency>
      <groupId>com.ibm.icu</groupId>
      <artifactId>com.ibm.icu</artifactId>
      <version>[4.4.2,)</version>
      <optional>true</optional>
    </dependency>
    <dependency>
      <groupId>org.eclipse.emf</groupId>
      <artifactId>org.eclipse.emf.ecore</artifactId>
      <version>[2.7.0,)</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.emf</groupId>
      <artifactId>org.eclipse.emf.common</artifactId>
      <version>[2.7.0,)</version>
    </dependency>
    <dependency>
      <groupId>org.apache.log4j</groupId>
      <artifactId>org.apache.log4j</artifactId>
      <version>[1.2.15,)</version>
    </dependency>
  </dependencies>
</project>''', new File( repo, 'de/itemis/xtext/de.itemis.xtext.typesystem/2.0.5-SNAPSHOT/de.itemis.xtext.typesystem-2.0.5-SNAPSHOT.pom' ) )
        
        CommonTestCode.fileEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.apache.commons</groupId>
  <artifactId>org.apache.commons.lang3</artifactId>
  <version>3.1.0</version>
  <name>Commons Lang3</name>
  <description>Converted with MT4E 0.14-SNAPSHOT (16.05.2012)</description>
  <properties>
    <mt4e.osgi.exportPackage>org.apache.commons.lang3;version="3.1.0",org.apache.commons.lang3.builder;version="3.1.0",org.apache.commons.lang3.concurrent;version="3.1.0",org.apache.commons.lang3.event;version="3.1.0",org.apache.commons.lang3.exception;version="3.1.0",org.apache.commons.lang3.math;version="3.1.0",org.apache.commons.lang3.mutable;version="3.1.0",org.apache.commons.lang3.reflect;version="3.1.0",org.apache.commons.lang3.text;version="3.1.0",org.apache.commons.lang3.text.translate;version="3.1.0",org.apache.commons.lang3.time;version="3.1.0",org.apache.commons.lang3.tuple;version="3.1.0"</mt4e.osgi.exportPackage>
  </properties>
</project>''', new File( repo, 'org/apache/commons/org.apache.commons.lang3/3.1.0/org.apache.commons.lang3-3.1.0.pom' ) )
    }

    /** Different import order */
    @Test
    public void testConvertSourceBundles_2() throws Exception {
        
        File source = CommonTestCode.prepareRepo( new File( 'data/input/importSourceBundles' ), 'testConvertSourceBundles' )
        File workDir = new File( source, 'tmp' )
        File repo = new File( source, 'm2repo' )
        File plugins = new File( source, 'plugins' )
        
        InstallCmd cmd = new InstallCmd( workDir: workDir )
        
        ImportTool tool = new ImportTool( m2repo: repo, installCmd: cmd )
        tool.versionMap.m2repo = repo
        
        tool.doImport( new File( plugins, 'de.itemis.xtext.typesystem_2.0.5.201205161310.jar' ) )
        tool.doImport( new File( plugins, 'de.itemis.xtext.typesystem.source_2.0.5.201205161310.jar' ) )
        tool.doImport( new File( plugins, 'org.apache.commons.lang3.sources_3.1.0.jar' ) )
        tool.doImport( new File( plugins, 'org.apache.commons.lang3_3.1.0.jar' ) )

        String expected = '''\
.mt4e/snapshotVersionMapping
de/itemis/xtext/de.itemis.xtext.typesystem/2.0.5-SNAPSHOT/de.itemis.xtext.typesystem-2.0.5-SNAPSHOT-sources.jar
de/itemis/xtext/de.itemis.xtext.typesystem/2.0.5-SNAPSHOT/de.itemis.xtext.typesystem-2.0.5-SNAPSHOT.jar
de/itemis/xtext/de.itemis.xtext.typesystem/2.0.5-SNAPSHOT/de.itemis.xtext.typesystem-2.0.5-SNAPSHOT.pom
org/apache/commons/org.apache.commons.lang3/3.1.0/org.apache.commons.lang3-3.1.0-sources.jar
org/apache/commons/org.apache.commons.lang3/3.1.0/org.apache.commons.lang3-3.1.0.jar
org/apache/commons/org.apache.commons.lang3/3.1.0/org.apache.commons.lang3-3.1.0.pom'''
                
        assertEquals( expected,
            CommonTestCode.listFiles( repo ) )
        
        tool.moveSourceBundles()

        // No change        
        assertEquals( expected,
            CommonTestCode.listFiles( repo ) )
    }
    
    private String convert( String manifest ) {
        def m = new Manifest( new ByteArrayInputStream( manifest.getBytes( 'UTF-8' ) ) )
        
        def cmd = new InstallCmd()
        
        def tool = new BundleConverter( manifest: m, installCmd: cmd )
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
