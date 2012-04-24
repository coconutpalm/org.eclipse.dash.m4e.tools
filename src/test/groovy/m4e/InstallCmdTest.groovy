package m4e;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pdark.decentxml.XMLParser;
import de.pdark.decentxml.XMLStringSource;

class InstallCmdTest {
    
    Logger log = LoggerFactory.getLogger( getClass() )

    def fileName = 'eclipse-3.7.1-delta-pack.zip'
    File archive = new File( 'downloads', fileName )

    @Test
    void testImportSwt() throws Exception {

	Assume.assumeTrue( Boolean.getBoolean( 'skipSlowTests' ) == false );

        MopSetup.setup()
        downloadDeltaPack()
        
        def workDir = new File( 'tmp-test/testImportSwt' )
        assert workDir.deleteDir(), "Failed to delete ${workDir.absolutePath}"
        workDir.makedirs()
        
        InstallCmd cmd = new InstallCmd( workDir: workDir )
        cmd.run([ 'install', archive.path ])
        
        def file = new File( workDir, "apache-maven-${InstallCmd.MVN_VERSION}/bin/mvn" )
        assert file.exists (), "Maven executable not found: Missing ${file.absolutePath}"
        
        file = new File( workDir, 'priming_home/m2repo/org/eclipse/core/resources/3.3.0-v20070604/resources-3.3.0-v20070604.pom' )
        assert file.exists (), "Priming archive wasn't unpacked as expected: Missing ${file.absolutePath}"
        
        file = new File( workDir, 'eclipse-3.7.1-delta-pack/eclipse/plugins/org.eclipse.compare.win32_1.0.200.I20110510-0800.jar' )
        assert file.exists (), "Delta Pack archive wasn't unpacked as expected: Missing ${file.absolutePath}"
        
        File deltaPackHome = new File( workDir, 'eclipse-3.7.1-delta-pack_home' )
        file = new File( deltaPackHome, 'm2repo/org/eclipse/swt/org.eclipse.swt.gtk.linux.x86_64/3.7.1-v3738a/org.eclipse.swt.gtk.linux.x86_64-3.7.1-v3738a.pom' )
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
        
        assertEquals 80, pomCount
        assertEquals 80, jarCount
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
