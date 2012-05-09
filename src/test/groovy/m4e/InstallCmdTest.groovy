/*******************************************************************************
 * Copyright (c) 11.01.2012 Aaron Digulla.
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

import org.junit.Test;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pdark.decentxml.XMLParser;
import de.pdark.decentxml.XMLStringSource;

class InstallCmdTest implements CommonConstants {
    
    static {
        MopSetup.setup()
    }
    
    Logger log = LoggerFactory.getLogger( getClass() )

    def fileName = 'eclipse-3.7.1-delta-pack.zip'
    File archive = new File( 'downloads', fileName )

    @Test
    void testImportSwt() throws Exception {

        Assume.assumeTrue( Boolean.getBoolean( 'skipSlowTests' ) == false )

        downloadDeltaPack()
        
        def workDir = CommonTestCode.newFile( 'testImportSwt' )
        assert workDir.deleteDir(), "Failed to delete ${workDir.absolutePath}"
        workDir.makedirs()
        
        InstallCmd cmd = new InstallCmd( workDir: workDir )
        cmd.run([ 'install', archive.path ])
        
        def file = new File( workDir, 'eclipse-3.7.1-delta-pack/eclipse/plugins/org.eclipse.compare.win32_1.0.200.I20110510-0800.jar' )
        assert file.exists (), "Delta Pack archive wasn't unpacked as expected: Missing ${file.absolutePath}"
        
        File deltaPackHome = new File( workDir, 'eclipse-3.7.1-delta-pack_home' )
        file = new File( deltaPackHome, 'm2repo/org/eclipse/swt/org.eclipse.swt.gtk.linux.x86_64/3.7.1.v3738a/org.eclipse.swt.gtk.linux.x86_64-3.7.1.v3738a.pom' )
        assert file.exists (), "Delta Pack wasn't imported as expected: Missing ${file.absolutePath}"
        
        int pomCount = 0
        int jarCount = 0
        deltaPackHome.eachFileRecurse { it ->
            if( it.name.endsWith( '.pom' ) ) {
                pomCount ++
            } else if( it.name.endsWith( '.jar' ) ) {
                jarCount ++
            }
        }
        
        assertEquals 54, pomCount
        assertEquals 80, jarCount
        
        assertEquals( 0, cmd.errorCount )
        assertEquals( 0, cmd.warningCount )
    }
    
    @Test
    public void testImportJUnit() throws Exception {
        def workDir = CommonTestCode.newFile( 'testImportJUnit' )
        assert workDir.deleteDir(), "Failed to delete ${workDir.absolutePath}"
        workDir.makedirs()
        
        File data = new File( 'data/input/importJUnit' )
        File inputDir = new File( workDir, 'downloads/junit' )
        data.copy( inputDir )
        
        InstallCmd cmd = new InstallCmd( workDir: workDir )
        
        cmd.run([ 'install', inputDir.path ])
        
        File repo = new File( workDir, 'junit_home/m2repo' )
        
        assert new File( repo, 'org/junit/org.junit/3.8.2.v3_8_2_v20100427-1100/org.junit-3.8.2.v3_8_2_v20100427-1100.jar' ).exists()
        assert new File( repo, 'org/junit/org.junit/3.8.2.v3_8_2_v20100427-1100/org.junit-3.8.2.v3_8_2_v20100427-1100-sources.jar' ).exists()
        
        String actual = new File( repo, 'org/junit/org.junit/3.8.2.v3_8_2_v20100427-1100/org.junit-3.8.2.v3_8_2_v20100427-1100.pom' ).getText( 'UTF-8' )
        assertEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.junit</groupId>
  <artifactId>org.junit</artifactId>
  <version>3.8.2.v3_8_2_v20100427-1100</version>
  <name>JUnit Testing Framework</name>
  <description>Converted with MT4E 0.13 (27.04.2012)</description>
  <properties>
    <mt4e.osgi.exportPackage>junit.awtui;version="3.8.2",junit.extensions;version="3.8.2",junit.framework;version="3.8.2",junit.runner;version="3.8.2",junit.swingui;version="3.8.2",junit.swingui.icons;version="3.8.2",junit.textui;version="3.8.2"</mt4e.osgi.exportPackage>
  </properties>
</project>
'''
            , actual)
            
        assert new File( repo, 'org/junit/org.junit/4.8.2.v4_8_2_v20110321-1705/org.junit-4.8.2.v4_8_2_v20110321-1705.jar' ).exists()
        assert new File( repo, 'org/junit/org.junit/4.8.2.v4_8_2_v20110321-1705/org.junit-4.8.2.v4_8_2_v20110321-1705-sources.jar' ).exists()
        
        actual = new File( repo, 'org/junit/org.junit/4.8.2.v4_8_2_v20110321-1705/org.junit-4.8.2.v4_8_2_v20110321-1705.pom' ).getText( 'UTF-8' )
        assertEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.junit</groupId>
  <artifactId>org.junit</artifactId>
  <version>4.8.2.v4_8_2_v20110321-1705</version>
  <name>JUnit Testing Framework</name>
  <description>Converted with MT4E 0.13 (27.04.2012)</description>
  <properties>
    <mt4e.osgi.exportPackage>junit.extensions;version="4.8.2",junit.framework;version="4.8.2",junit.runner;version="4.8.2",junit.textui;version="4.8.2",org.junit;version="4.8.2",org.junit.experimental;version="4.8.2",org.junit.experimental.categories;version="4.8.2",org.junit.experimental.max;version="4.8.2",org.junit.experimental.results;version="4.8.2",org.junit.experimental.runners;version="4.8.2",org.junit.experimental.theories;version="4.8.2",org.junit.experimental.theories.internal;version="4.8.2";x-internal:="true",org.junit.experimental.theories.suppliers;version="4.8.2",org.junit.internal;version="4.8.2";x-internal:="true",org.junit.internal.builders;version="4.8.2";x-internal:="true",org.junit.internal.matchers;version="4.8.2";x-internal:="true",org.junit.internal.requests;version="4.8.2";x-internal:="true",org.junit.internal.runners;version="4.8.2";x-internal:="true",org.junit.internal.runners.model;version="4.8.2";x-internal:="true",org.junit.internal.runners.statements;version="4.8.2";x-internal:="true",org.junit.matchers;version="4.8.2",org.junit.rules;version="4.8.2",org.junit.runner;version="4.8.2",org.junit.runner.manipulation;version="4.8.2",org.junit.runner.notification;version="4.8.2",org.junit.runners;version="4.8.2",org.junit.runners.model;version="4.8.2"</mt4e.osgi.exportPackage>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.hamcrest.core</groupId>
      <artifactId>org.hamcrest.core</artifactId>
      <version>[1.1.0,)</version>
    </dependency>
  </dependencies>
</project>
'''
            , actual)
        
        assert new File( repo, 'org/junit4/org.junit4/4.8.1.v20100525/org.junit4-4.8.1.v20100525.jar' ).exists()
        
        actual = new File( repo, 'org/junit4/org.junit4/4.8.1.v20100525/org.junit4-4.8.1.v20100525.pom' ).getText( 'UTF-8' )
        assertEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.junit4</groupId>
  <artifactId>org.junit4</artifactId>
  <version>4.8.1.v20100525</version>
  <name>JUnit Testing Framework Version 4</name>
  <description>Converted with MT4E 0.13 (27.04.2012)</description>
  <dependencies>
    <dependency>
      <groupId>org.hamcrest.core</groupId>
      <artifactId>org.hamcrest.core</artifactId>
      <version>[1.1.0,)</version>
    </dependency>
    <dependency>
      <groupId>org.junit</groupId>
      <artifactId>org.junit</artifactId>
      <version>[4.8.1,)</version>
    </dependency>
  </dependencies>
</project>
'''
            , actual)

        assert new File( repo, 'org/eclipse/m2e/org.eclipse.m2e.logback.configuration/1.0.200.20111228-1245/org.eclipse.m2e.logback.configuration-1.0.200.20111228-1245.jar' ).exists()
        
        actual = new File( repo, 'org/eclipse/m2e/org.eclipse.m2e.logback.configuration/1.0.200.20111228-1245/org.eclipse.m2e.logback.configuration-1.0.200.20111228-1245.pom' ).getText( 'UTF-8' )
        assertEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.eclipse.m2e</groupId>
  <artifactId>org.eclipse.m2e.logback.configuration</artifactId>
  <version>1.0.200.20111228-1245</version>
  <name>m2e logback configuration</name>
  <description>Converted with MT4E 0.13 (27.04.2012)</description>
  <properties>
    <mt4e.osgi.exportPackage>org.eclipse.m2e.logback.configuration;x-internal:="true"</mt4e.osgi.exportPackage>
    <mt4e.osgi.isSingleton>true</mt4e.osgi.isSingleton>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.eclipse.core</groupId>
      <artifactId>org.eclipse.core.runtime</artifactId>
      <version>[3.5.0,)</version>
    </dependency>
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>ch.qos.logback.classic</artifactId>
      <version>[0.9.24,)</version>
    </dependency>
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>ch.qos.logback.core</artifactId>
      <version>[0.9.24,)</version>
    </dependency>
    <dependency>
      <groupId>org.slf4j.api</groupId>
      <artifactId>org.slf4j.api</artifactId>
      <version>[1.6.1,)</version>
    </dependency>
  </dependencies>
</project>
'''
            , actual)
            
    }
    
    @Test
    public void testImportErrors() throws Exception {
        def workDir = CommonTestCode.newFile( 'testImportErrors' )
        assert workDir.deleteDir(), "Failed to delete ${workDir.absolutePath}"
        workDir.makedirs()
        
        File data = new File( 'data/input/importErrors' )
        File inputDir = new File( workDir, 'downloads/junit' )
        data.copy( inputDir )
        
        InstallCmd cmd = new InstallCmd( workDir: workDir )
        
        cmd.run([ 'install', inputDir.path ])
        
        File repo = cmd.m2repos[ 0 ]
        
        File installLog = new File( repo, MT4E_FOLDER + '/logs/install.xml'  )
        assert installLog.exists()
        
        def lines = installLog.getText( UTF_8 )
        .replace( repo.absolutePath, '${m2repo}' )
        .replace( inputDir.absolutePath, '${input}' )
        .replace( '\\', '/' )
        lines = lines.split( '\n' ) as List
        
        lines = [lines[0]] + lines[1..-2].sort() + [lines[-1]]
        String actual =  lines.join( '\n' )

        assertEquals( '''\
<mt4e-log command='install'>
<error code='E0003' jar='${input}/plugins/nomanifest.jar'>Can't find manifest in ${input}/plugins/nomanifest.jar</error>
<error code='E0003' jar='${input}/plugins/unpackedPlugin/META-INF/MANIFEST.MF'>Can't find manifest ${input}/plugins/unpackedPlugin/META-INF/MANIFEST.MF</error>
<error code='E0004' file='${input}/plugins/file.txt' exception='error in opening zip file'>Error processing ${input}/plugins/file.txt: java.util.zip.ZipException: error in opening zip file</error>
<error code='E0004' file='${input}/plugins/notajar.jar' exception='error in opening zip file'>Error processing ${input}/plugins/notajar.jar: java.util.zip.ZipException: error in opening zip file</error>
<warning code='W0004' jar='${m2repo}/org/eclipse/birt/org.eclipse.birt.report.data.oda.jdbc.dbprofile/3.7.0.v20110603/org.eclipse.birt.report.data.oda.jdbc.dbprofile-3.7.0.v20110603.jar' nestedJarPath='.,src'>Multiple nested JARs are not supported; just copying the original bundle</warning>
</mt4e-log>''', actual )
    }

    void downloadDeltaPack() {
        if( archive.exists() ) {
            log.info( "Using cached version of SWT Delta Pack" )
            return
        }
        
        def url = new URL( "http://www.eclipse.org/downloads/download.php?file=/eclipse/downloads/drops/R-3.7.1-201109091335/${fileName}&protocol=http&format=xml" )
        log.info( "Fetching mirror list from www.eclipse.org..." )
        def xml = url.getText()
        log.debug( "xml:\n${xml}" )
        
        def parser = new XMLParser()
        def doc = parser.parse( new XMLStringSource( xml ) )
        def root = doc.rootElement
        
        def mirrors = root.getChildren( 'mirror' )
        def mirror = mirrors[0]
        
        url = new URL( mirror.getAttributeValue( 'url' ) )
        def label = mirror.getAttributeValue( 'label' )
        
        log.info( "Downloading SWT Delta Pack from ${label} (${url})..." )
        
        def tmp = new File( 'tmp', fileName )
        tmp.parentFile.makedirs()
        
        url.withInputStream { stream ->
            tmp << stream
        }
        
        archive.parentFile.makedirs()
        assert tmp.renameTo( archive ), "Rename ${tmp.absolutePath} -> ${archive.absolutePath} failed"
        log.info( "Downloaded ${archive.length()} bytes to ${archive.absolutePath}" )
    }
}
