package m4e;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pdark.decentxml.XMLParser;
import de.pdark.decentxml.XMLStringSource;

class InstallCmdTest {
    
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
        
        File data = new File( 'data/input/import' )
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
  <dependencies>
    <dependency>
      <groupId>org.hamcrest.core</groupId>
      <artifactId>org.hamcrest.core</artifactId>
      <version>1.1.0</version>
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
  <dependencies>
    <dependency>
      <groupId>org.hamcrest.core</groupId>
      <artifactId>org.hamcrest.core</artifactId>
      <version>1.1.0</version>
    </dependency>
    <dependency>
      <groupId>org.junit</groupId>
      <artifactId>org.junit</artifactId>
      <version>4.8.1</version>
    </dependency>
  </dependencies>
</project>
'''
            , actual)

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
