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
import java.io.File;
import m4e.CommonConstants;
import m4e.CommonTestCode;
import m4e.MopSetup;
import m4e.PatchCmd
import m4e.Pom;
import org.junit.Test;

class StripQualifiersTest extends CommonTestCode implements CommonConstants {
    
    @Test
    public void testNoVersion1() throws Exception {
        assertEquals( null, new StripQualifiers().stripQualifier( null ) )
    }
    
    @Test
    public void testVersion1() throws Exception {
        assertEquals( '1', new StripQualifiers().stripQualifier( '1' ) )
    }
    
    @Test
    public void testVersion1_0() throws Exception {
        assertEquals( '1.0', new StripQualifiers().stripQualifier( '1.0' ) )
    }
    
    @Test
    public void testVersion1_3_2() throws Exception {
        assertEquals( '1.3.2', new StripQualifiers().stripQualifier( '1.3.2' ) )
    }
    
    @Test
    public void testVersionMinusQualifier() throws Exception {
        assertEquals( '3.7.1', new StripQualifiers().stripQualifier( '3.7.1-v3738a' ) )
    }
    
    @Test
    public void testVersionSnapshot() throws Exception {
        assertEquals( '3.7.1-SNAPSHOT', new StripQualifiers().stripQualifier( '3.7.1-SNAPSHOT' ) )
    }
    
    @Test
    public void testEmptyVersion() throws Exception {
        assertEquals( '', new StripQualifiers().stripQualifier( '' ) )
    }
    
    @Test
    public void testRange1() throws Exception {
        assertEquals( '[2.6.2,3.0)', new StripQualifiers().stripQualifier( '[2.6.2,3.0)' ) )
    }
    
    @Test
    public void testRange2() throws Exception {
        assertEquals( '[3.6.1,4)', new StripQualifiers().stripQualifier( '[3.6.1,4)' ) )
    }
    
    @Test
    public void testRange3() throws Exception {
        assertEquals( '[0,)', new StripQualifiers().stripQualifier( '[0,)' ) )
    }
    
    @Test
    public void testQualifier() throws Exception {
        assertEquals( '[2.5.0,3.0.0)', new StripQualifiers().stripQualifier( '[2.5.0.v200906151043,3.0.0)' ) )
    }
    
    @Test
    public void testQualifier2() throws Exception {
        assertEquals( '2.1.2', new StripQualifiers().stripQualifier( '2.1.2.v20101206-r8635' ) )
    }
    
    @Test
    public void testQualifier3() throws Exception {
        assertEquals( '[2.1.2,3.0.0)', new StripQualifiers().stripQualifier( '[2.1.2.v20101206-r8635,3.0.0)' ) )
    }

    @Test
    public void testStripQualifierFromPomVersion() throws Exception {
        
        File target = prepareRepo( new File( 'data/input/stripQualifier' ), 'testStripQualifierFromPomVersion/m2repo' )
        
        def tool = new PatchCmd( target: target )
        tool.init()
        
        tool.globalPatches.qualifierPatches << new QualifierPatch( 'org.apache.batik:org.apache.batik.util:1.6.0.v201011041432', '1.6.0.1' )
        
        tool.loadPatches()
        tool.applyPatches()
        
        assertEquals( '''\
org/apache/batik/org.apache.batik.dom/1.6.0/org.apache.batik.dom-1.6.0.pom
org/apache/batik/org.apache.batik.dom/1.6.0/org.apache.batik.dom-1.6.0.pom.bak
org/apache/batik/org.apache.batik.util/1.6.0.1/org.apache.batik.util-1.6.0.1.pom
org/apache/batik/org.apache.batik.util/1.6.0.1/org.apache.batik.util-1.6.0.1.pom.bak
org/eclipse/m2e/org.eclipse.m2e.logback.configuration/1.0.200/org.eclipse.m2e.logback.configuration-1.0.200.pom
org/eclipse/m2e/org.eclipse.m2e.logback.configuration/1.0.200/org.eclipse.m2e.logback.configuration-1.0.200.pom.bak
org/eclipse/swt/org.eclipse.swt.gtk.linux.x86/3.7.1/org.eclipse.swt.gtk.linux.x86-3.7.1-sources.jar
org/eclipse/swt/org.eclipse.swt.gtk.linux.x86/3.7.1/org.eclipse.swt.gtk.linux.x86-3.7.1-sources.jar.sha1
org/eclipse/swt/org.eclipse.swt.gtk.linux.x86/3.7.1/org.eclipse.swt.gtk.linux.x86-3.7.1.jar
org/eclipse/swt/org.eclipse.swt.gtk.linux.x86/3.7.1/org.eclipse.swt.gtk.linux.x86-3.7.1.jar.bak
org/eclipse/swt/org.eclipse.swt.gtk.linux.x86/3.7.1/org.eclipse.swt.gtk.linux.x86-3.7.1.pom
org/eclipse/swt/org.eclipse.swt.gtk.linux.x86/3.7.1/org.eclipse.swt.gtk.linux.x86-3.7.1.pom.bak
org/eclipse/swt/org.eclipse.swt.gtk.linux.x86/3.7.1/org.eclipse.swt.gtk.linux.x86-3.7.1.sha1
org/eclipse/swt/org.eclipse.swt.gtk.linux.x86/3.7.1/org.eclipse.swt.gtk.linux.x86-3.7.1xxx'''
            , listFiles( target ) )
        
        File dir = new File( target, 'org/eclipse/swt/org.eclipse.swt.gtk.linux.x86' )
        def pom = Pom.load( new File( dir, '3.7.1/org.eclipse.swt.gtk.linux.x86-3.7.1.pom' ) )
        
        assertEquals( '3.7.1', pom.version() )
        
        File newDir = new File( dir, pom.version() )
        assert newDir.exists()

        fileEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.eclipse.swt</groupId>
  <artifactId>org.eclipse.swt.gtk.linux.x86</artifactId>
  <version>3.7.1</version>
  <name>%fragmentName</name>
  <licenses>
    <license>
      <name>Eclipse Public License - v 1.0</name>
      <url>http://www.eclipse.org/org/documents/epl-v10.html</url>
    </license>
  </licenses>
  
  <dependencies>
    <dependency>
      <groupId>g</groupId>
      <artifactId>a</artifactId>
      <version>3.7.1</version>
    </dependency>
  </dependencies>
</project>''',
            new File( newDir, 'org.eclipse.swt.gtk.linux.x86-3.7.1.pom' ) )

        dir = new File( target, 'org/apache/batik/org.apache.batik.util' )
        
        pom = Pom.load( new File( dir, '1.6.0.1/org.apache.batik.util-1.6.0.1.pom' ) )
        assertEquals( '1.6.0.1', pom.version() )
        
        newDir = new File( dir, pom.version() )
        assert newDir.exists()
        
        fileEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.apache.batik</groupId>
  <artifactId>org.apache.batik.util</artifactId>
  <version>1.6.0.1</version>
  <name>Apache Batik Utilities</name>
  <dependencies>
    <dependency>
      <groupId>org.apache.batik</groupId>
      <artifactId>org.apache.batik.util.gui</artifactId>
      <version>[1.6.0,1.7.0)</version>
    </dependency>
  </dependencies>
</project>''',
            new File( newDir, 'org.apache.batik.util-1.6.0.1.pom' ) )

        dir = new File( target, 'org/apache/batik/org.apache.batik.dom' )
        
        pom = Pom.load( new File( dir, '1.6.0/org.apache.batik.dom-1.6.0.pom' ) )
        
        assertEquals( '1.6.0', pom.version() )
        
        newDir = new File( dir, pom.version() )
        assert newDir.exists()
        
        fileEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.apache.batik</groupId>
  <artifactId>org.apache.batik.dom</artifactId>
  <version>1.6.0</version>
  <name>Apache Batik DOM</name>
  <dependencies>
    <dependency>
      <groupId>org.apache.batik</groupId>
      <artifactId>org.apache.batik.css</artifactId>
      <version>[1.6.0,1.7.0)</version>
    </dependency>
    <dependency>
      <groupId>org.apache.batik</groupId>
      <artifactId>org.apache.batik.util</artifactId>
      <version>[1.6.0,1.7.0)</version>
    </dependency>
    <dependency>
      <groupId>org.apache.batik</groupId>
      <artifactId>org.apache.batik.xml</artifactId>
      <version>[1.6.0,1.7.0)</version>
    </dependency>
    <dependency>
      <groupId>org.w3c.css</groupId>
      <artifactId>org.w3c.css.sac</artifactId>
      <version>[1.3.0,1.4.0)</version>
    </dependency>
  </dependencies>
</project>''',
            new File( newDir, 'org.apache.batik.dom-1.6.0.pom' ) )
    }
    
    @Test
    public void testSnapshotDependencies() throws Exception {
        
        File target = newFile( 'testSnapshotDependencies' )
        
        def file = new File( target, "${MT4E_FOLDER}/${SNAPSHOT_VERSION_MAPPING_FILE}" )
        file.parentFile?.makedirs()
        file << "de.itemis.xtext:de.itemis.xtext.typesystem 2.0.5.201205161310 2.0.5-SNAPSHOT\n"
        
        def tool = new PatchCmd( target: target )
        tool.init()

        tool.loadPatches()
        
        def input = '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.avanon.eclipse</groupId>
  <artifactId>com.avanon.eclipse.pm-dsl</artifactId>
  <version>2.2.0-SNAPSHOT</version>
  <name>DSL for Parameter Model</name>
  <dependencies>
    <dependency>
      <groupId>de.itemis.xtext</groupId>
      <artifactId>de.itemis.xtext.typesystem</artifactId>
      <version>[2.0.5,)</version>
    </dependency>
  </dependencies>
</project>'''

        def pom = Pom.load( input )
        tool.patchPom( pom )
        
        assertEquals( input.replace( '<version>[2.0.5,)</version>' ,'<version>2.0.5-SNAPSHOT</version>' ), pom.toString() )
    }
}
