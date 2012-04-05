package m4e;

import static org.junit.Assert.*;
import org.junit.Test;

class P2ListCmdTest {
    
    static {
        MopSetup.setup()
    }

    @Test
    public void testEmptyVersion() throws Exception {
        def version = new Version( null )
        assertEquals( '', version.toString() )
    }
    
    @Test
    public void testShortVersion() throws Exception {
        def version = new Version( '1.2.3' )
        assertEquals( '1.2.3', version.toString() )
        assertEquals( 1, version.major )
        assertEquals( 2, version.minor )
        assertEquals( 3, version.service )
        assertEquals( null, version.qualifier )
    }
    
    @Test
    public void testVersionWithQualifier() throws Exception {
        def version = new Version( '0.8.2.v201202150957' )
        assertEquals( '0.8.2.v201202150957', version.toString() )
        assertEquals( 0, version.major )
        assertEquals( 8, version.minor )
        assertEquals( 2, version.service )
        assertEquals( 'v201202150957', version.qualifier )
    }
    
    @Test
    public void testEmptyVersionRange() throws Exception {
        def range = new VersionRange( null )
        assertEquals( '', range.toString() )
    }
    
    @Test
    public void testVersionRangeCompareEmpty() throws Exception {
        assertCompare( 0, new Version( null ), new Version( null ) )
    }
    
    private void assertCompare( int expected, Comparable c1, Comparable c2 ) {
        int d = c1.compareTo( c2 )
        d = Math.signum( d )
        assertEquals( "Comparing ${c1} and ${c2}", expected, d )
    }
    
    @Test
    public void testVersionRangeCompareBlank() throws Exception {
        assertCompare( 0, new Version( null ), new Version( '' ) )
    }
    
    @Test
    public void testVersionRangeCompareWithoutQualifier() throws Exception {
        assertCompare( 1, new Version( '1.0.0' ), new Version( '0.0.0' ) )
        assertCompare( 1, new Version( '3.1.0' ), new Version( '0.0.0' ) )
        assertCompare( 0, new Version( '3.1.0' ), new Version( '3.1.0' ) )
        assertCompare( -1, new Version( '1.0.0' ), new Version( '1.0.1' ) )
    }
    
    @Test
    public void testVersionRangeCompareWithQualifier() throws Exception {
        assertCompare( 0, new Version( '1.0.0.v20120402' ), new Version( '1.0.0.v20120402' ) )
        assertCompare( 1, new Version( '1.0.0.v20120402' ), new Version( '1.0.0.v20120401' ) )
        assertCompare( -1, new Version( '1.0.0.v20120402' ), new Version( '1.0.0.v20120405' ) )
    }
    
    @Test
    public void testVersionRangeCompareMixed() throws Exception {
        assertCompare( 1, new Version( '1.0.0.v20120402' ), new Version( '1.0.0' ) )
        assertCompare( -1, new Version( '1.0.0' ), new Version( '1.0.0.v20120401' ) )
    }
    
    @Test
    public void testNextVersion() throws Exception {
        assertEquals( "1.0.1", new Version( '1.0.0.v20120402' ).next().toString() )
    }
    
    @Test
    public void testVersionRange() throws Exception {
        def range = new VersionRange( '[0.0.0,3.0.1.v20110824)' )
        assertEquals( '[0.0.0,3.0.1.v20110824)', range.toString() )
        assertEquals( true, range.includeLower )
        assertEquals( '0.0.0', range.lower.toString() )
        assertEquals( '3.0.1.v20110824', range.upper.toString() )
        assertEquals( false, range.includeUpper )
        
        assertEquals( false, range.upper.equals( range.lower ) )
        assertTrue( range.upper.compareTo( range.lower ) > 0 )
        assertTrue( range.lower.compareTo( range.upper ) < 0 )
    }
    
    @Test
    public void testVersionRange_2() throws Exception {
        def range = new VersionRange( '[1.0.0.v20111230-0120,1.0.0.v20111230-0120]' )
        assertEquals( '[1.0.0.v20111230-0120,1.0.0.v20111230-0120]', range.toString() )
        assertEquals( true, range.includeLower )
        assertEquals( '1.0.0.v20111230-0120', range.lower.toString() )
        assertEquals( '1.0.0.v20111230-0120', range.upper.toString() )
        assertEquals( true, range.includeUpper )
        
        assertEquals( range.lower, range.upper )
        assertEquals( 0, range.upper.compareTo( range.lower ) )
    }
    
    @Test
    public void testVersionRange_Contains() throws Exception {
        def range = new VersionRange( '[0.0.0,3.0.1.v20110824)' )
        
        assertEquals( true, range.contains( new Version( '1.0.0' ) ) )
        assertEquals( true, range.contains( new Version( '0.0.0' ) ) )
        assertEquals( true, range.contains( new Version( '3.0.0' ) ) )
        assertEquals( true, range.contains( new Version( '3.0.1' ) ) )
        assertEquals( true, range.contains( new Version( '3.0.1.v20110823' ) ) )
        assertEquals( false, range.contains( new Version( '3.0.1.v20110824' ) ) )
        assertEquals( false, range.contains( new Version( '3.0.2' ) ) )
    }

    static File testFolder = new File( "tmp-test" )
    static File contentJar = new File( testFolder, "pydevRepo/content.jar" )
    static File contentXmlFile = new File( testFolder, "pydevRepo/content.xml" )
    
    static boolean testDownloadRanOnce
    
    @Test
    public void testDownload() throws Exception {
        if( testDownloadRanOnce ) {
            return
        }
        
        testDownloadRanOnce = true
        
        contentJar.usefulDelete()
        
        def url = new File( "data/input/pydev" ).toURI().toURL()
        
        P2Repo repo = new P2Repo( workDir: new File( testFolder, 'pydevRepo' ), url: url, progressFactory: new MockProgressFactory() )
        repo.downloadContentJar( contentJar )
        
        assertTrue( contentJar.exists() )
    }
    
    static boolean testUnpackRanOnce
    
    @Test
    public void testUnpack() throws Exception {
        
        if( testUnpackRanOnce ) {
            return
        }
        
        testUnpackRanOnce = true
        
        testDownload()
        
        contentXmlFile.usefulDelete()
        
        P2Repo repo = new P2Repo( workDir: new File( testFolder, 'pydevRepo' ), progressFactory: new MockProgressFactory() )
        repo.unpackContentJar( contentJar, contentXmlFile )
        
        assertTrue( contentXmlFile.exists() )
    }
    
    static P2Repo __pydevRepo
    
    private P2Repo pydevRepo() {
        if( __pydevRepo ) {
            return __pydevRepo
        }
        
        testUnpack()
        
        def repo = new P2Repo(  workDir: new File( testFolder, 'pydevRepo' ), url: new URL( 'http://pydev.org/updates' ) )
        def parser = new ContentXmlParser( repo: repo )
        parser.parseXml( contentXmlFile )

        __pydevRepo = repo
        return __pydevRepo
    }
    
    @Test
    public void testPyDevCategories() throws Exception {
        
        assertEquals(
             '''\
P2Category( id=PyDev, version=0.0.0, name=PyDev )
P2Category( id=PyDev Mylyn Integration, version=0.0.0, name=PyDev Mylyn Integration (optional) )'''
             , pydevRepo().categories.join( '\n' ) )
    }
    
    @Test
    public void testPyDevFeatures() throws Exception {
        
        assertEquals(
            '''\
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=1.6.2.2010090711, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=1.6.2.2010090812, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=1.6.3.2010100513, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=1.6.4.2011010200, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=1.6.5.2011020317, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=2.0.0.2011040403, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=2.1.0.2011052613, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=2.2.0.2011062419, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=2.2.1.2011071313, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=2.2.2.2011082312, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=2.2.3.2011100616, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=2.2.4.2011110216, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=2.3.0.2011121518, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=2.4.0.2012020116, name=PyDev Django Templates Editor )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.5.0.1251989166, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.5.1.1258496115, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.5.2.1260362205, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.5.3.1260479439, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.5.4.2010011921, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.5.5.2010030420, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.5.6.2010033101, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.5.7.2010050621, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.5.8.2010062823, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.5.9.2010063000, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.5.9.2010063001, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.6.0.2010071813, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.6.1.2010080312, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.6.2.2010090711, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.6.2.2010090812, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.6.3.2010100513, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.6.4.2011010200, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=1.6.5.2011020317, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=2.0.0.2011040403, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=2.1.0.2011052613, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=2.2.0.2011062419, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=2.2.1.2011071313, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=2.2.2.2011082312, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=2.2.3.2011100616, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=2.2.4.2011110216, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=2.3.0.2011121518, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.feature.feature.group, version=2.4.0.2012020116, name=PyDev for Eclipse )
P2Feature( id=org.python.pydev.mylyn.feature.feature.group, version=0.3.0, name=Pydev Mylyn Integration )'''
            , pydevRepo().features.join( '\n' ) )
    }
    
    @Test
    public void testPyDevPlugins() throws Exception {

        assertEquals(
                '''\
P2Plugin( id=com.python.pydev, version=1.5.0.1251989166, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.5.1.1258496115, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.5.2.1260362205, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.5.3.1260479439, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.5.4.2010011921, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.5.5.2010030420, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.5.6.2010033101, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.5.7.2010050621, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.5.8.2010062823, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.5.9.2010063000, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.5.9.2010063001, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.6.0.2010071813, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.6.1.2010080312, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.6.2.2010090711, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.6.2.2010090812, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.6.3.2010100513, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.6.4.2011010200, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=1.6.5.2011020317, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=2.0.0.2011040403, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=2.1.0.2011052613, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=2.2.0.2011062419, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=2.2.1.2011071313, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=2.2.2.2011082312, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=2.2.3.2011100616, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=2.2.4.2011110216, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=2.3.0.2011121518, name=Pydev Extensions )
P2Plugin( id=com.python.pydev, version=2.4.0.2012020116, name=Pydev Extensions )
P2Plugin( id=com.python.pydev.analysis, version=1.5.0.1251989166, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.5.1.1258496115, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.5.2.1260362205, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.5.3.1260479439, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.5.4.2010011921, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.5.5.2010030420, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.5.6.2010033101, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.5.7.2010050621, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.5.8.2010062823, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.5.9.2010063000, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.5.9.2010063001, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.6.0.2010071813, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.6.1.2010080312, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.6.2.2010090711, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.6.2.2010090812, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.6.3.2010100513, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.6.4.2011010200, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=1.6.5.2011020317, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=2.0.0.2011040403, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=2.1.0.2011052613, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=2.2.0.2011062419, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=2.2.1.2011071313, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=2.2.2.2011082312, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=2.2.3.2011100616, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=2.2.4.2011110216, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=2.3.0.2011121518, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.analysis, version=2.4.0.2012020116, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.5.0.1251989166, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.5.1.1258496115, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.5.2.1260362205, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.5.3.1260479439, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.5.4.2010011921, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.5.5.2010030420, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.5.6.2010033101, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.5.7.2010050621, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.5.8.2010062823, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.5.9.2010063000, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.5.9.2010063001, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.6.0.2010071813, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.6.1.2010080312, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.6.2.2010090711, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.6.2.2010090812, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.6.3.2010100513, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.6.4.2011010200, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=1.6.5.2011020317, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=2.0.0.2011040403, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=2.1.0.2011052613, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=2.2.0.2011062419, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=2.2.1.2011071313, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=2.2.2.2011082312, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=2.2.3.2011100616, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=2.2.4.2011110216, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=2.3.0.2011121518, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=2.4.0.2012020116, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.5.0.1251989166, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.5.1.1258496115, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.5.2.1260362205, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.5.3.1260479439, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.5.4.2010011921, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.5.5.2010030420, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.5.6.2010033101, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.5.7.2010050621, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.5.8.2010062823, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.5.9.2010063000, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.5.9.2010063001, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.6.0.2010071813, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.6.1.2010080312, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.6.2.2010090711, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.6.2.2010090812, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.6.3.2010100513, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.6.4.2011010200, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=1.6.5.2011020317, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=2.0.0.2011040403, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=2.1.0.2011052613, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=2.2.0.2011062419, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=2.2.1.2011071313, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=2.2.2.2011082312, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=2.2.3.2011100616, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=2.2.4.2011110216, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=2.3.0.2011121518, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.debug, version=2.4.0.2012020116, name=Extensions Debug Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.5.0.1251989166, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.5.1.1258496115, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.5.2.1260362205, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.5.3.1260479439, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.5.4.2010011921, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.5.5.2010030420, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.5.6.2010033101, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.5.7.2010050621, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.5.8.2010062823, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.5.9.2010063000, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.5.9.2010063001, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.6.0.2010071813, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.6.1.2010080312, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.6.2.2010090711, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.6.2.2010090812, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.6.3.2010100513, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.6.4.2011010200, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=1.6.5.2011020317, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=2.0.0.2011040403, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=2.1.0.2011052613, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=2.2.0.2011062419, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=2.2.1.2011071313, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=2.2.2.2011082312, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=2.2.3.2011100616, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=2.2.4.2011110216, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=2.3.0.2011121518, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=2.4.0.2012020116, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.5.0.1251989166, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.5.1.1258496115, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.5.2.1260362205, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.5.3.1260479439, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.5.4.2010011921, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.5.5.2010030420, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.5.6.2010033101, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.5.7.2010050621, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.5.8.2010062823, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.5.9.2010063000, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.5.9.2010063001, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.6.0.2010071813, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.6.1.2010080312, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.6.2.2010090711, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.6.2.2010090812, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.6.3.2010100513, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.6.4.2011010200, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=1.6.5.2011020317, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=2.0.0.2011040403, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=2.1.0.2011052613, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=2.2.0.2011062419, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=2.2.1.2011071313, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=2.2.2.2011082312, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=2.2.3.2011100616, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=2.2.4.2011110216, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=2.3.0.2011121518, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=2.4.0.2012020116, name=Refactoring Plug-in )
P2Plugin( id=config.a.jre, version=1.6.0, name=config.a.jre )
P2Plugin( id=org.python.pydev, version=1.5.0.1251989166, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.5.1.1258496115, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.5.2.1260362205, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.5.3.1260479439, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.5.4.2010011921, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.5.5.2010030420, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.5.6.2010033101, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.5.7.2010050621, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.5.8.2010062823, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.5.9.2010063000, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.5.9.2010063001, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.6.0.2010071813, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.6.1.2010080312, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.6.2.2010090711, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.6.2.2010090812, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.6.3.2010100513, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.6.4.2011010200, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=1.6.5.2011020317, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=2.0.0.2011040403, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=2.1.0.2011052613, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=2.2.0.2011062419, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=2.2.1.2011071313, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=2.2.2.2011082312, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=2.2.3.2011100616, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=2.2.4.2011110216, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=2.3.0.2011121518, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev, version=2.4.0.2012020116, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev.ast, version=1.5.0.1251989166, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.5.1.1258496115, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.5.2.1260362205, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.5.3.1260479439, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.5.4.2010011921, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.5.5.2010030420, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.5.6.2010033101, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.5.7.2010050621, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.5.8.2010062823, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.5.9.2010063000, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.5.9.2010063001, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.6.0.2010071813, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.6.1.2010080312, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.6.2.2010090711, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.6.2.2010090812, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.6.3.2010100513, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.6.4.2011010200, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=1.6.5.2011020317, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=2.0.0.2011040403, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=2.1.0.2011052613, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=2.2.0.2011062419, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=2.2.1.2011071313, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=2.2.2.2011082312, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=2.2.3.2011100616, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=2.2.4.2011110216, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=2.3.0.2011121518, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.ast, version=2.4.0.2012020116, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.5.0.1251989166, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.5.1.1258496115, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.5.2.1260362205, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.5.3.1260479439, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.5.4.2010011921, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.5.5.2010030420, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.5.6.2010033101, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.5.7.2010050621, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.5.8.2010062823, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.5.9.2010063000, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.5.9.2010063001, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.6.0.2010071813, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.6.1.2010080312, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.6.2.2010090711, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.6.2.2010090812, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.6.3.2010100513, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.6.4.2011010200, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=1.6.5.2011020317, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=2.0.0.2011040403, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=2.1.0.2011052613, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=2.2.0.2011062419, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=2.2.1.2011071313, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=2.2.2.2011082312, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=2.2.3.2011100616, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=2.2.4.2011110216, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=2.3.0.2011121518, name=Core Plug-in )
P2Plugin( id=org.python.pydev.core, version=2.4.0.2012020116, name=Core Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.5.0.1251989166, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.5.1.1258496115, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.5.2.1260362205, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.5.3.1260479439, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.5.4.2010011921, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.5.5.2010030420, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.5.6.2010033101, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.5.7.2010050621, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.5.8.2010062823, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.5.9.2010063000, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.5.9.2010063001, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.6.0.2010071813, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.6.1.2010080312, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.6.2.2010090711, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.6.2.2010090812, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.6.3.2010100513, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.6.4.2011010200, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=1.6.5.2011020317, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=2.0.0.2011040403, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=2.1.0.2011052613, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=2.2.0.2011062419, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=2.2.1.2011071313, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=2.2.2.2011082312, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=2.2.3.2011100616, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=2.2.4.2011110216, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=2.3.0.2011121518, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.customizations, version=2.4.0.2012020116, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.debug, version=1.5.0.1251989166, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.5.1.1258496115, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.5.2.1260362205, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.5.3.1260479439, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.5.4.2010011921, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.5.5.2010030420, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.5.6.2010033101, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.5.7.2010050621, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.5.8.2010062823, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.5.9.2010063000, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.5.9.2010063001, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.6.0.2010071813, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.6.1.2010080312, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.6.2.2010090711, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.6.2.2010090812, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.6.3.2010100513, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.6.4.2011010200, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=1.6.5.2011020317, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=2.0.0.2011040403, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=2.1.0.2011052613, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=2.2.0.2011062419, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=2.2.1.2011071313, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=2.2.2.2011082312, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=2.2.3.2011100616, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=2.2.4.2011110216, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=2.3.0.2011121518, name=Pydev debug )
P2Plugin( id=org.python.pydev.debug, version=2.4.0.2012020116, name=Pydev debug )
P2Plugin( id=org.python.pydev.django, version=1.5.6.2010033101, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=1.5.7.2010050621, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=1.5.8.2010062823, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=1.5.9.2010063000, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=1.5.9.2010063001, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=1.6.0.2010071813, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=1.6.1.2010080312, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=1.6.2.2010090711, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=1.6.2.2010090812, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=1.6.3.2010100513, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=1.6.4.2011010200, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=1.6.5.2011020317, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=2.0.0.2011040403, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=2.1.0.2011052613, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=2.2.0.2011062419, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=2.2.1.2011071313, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=2.2.2.2011082312, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=2.2.3.2011100616, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=2.2.4.2011110216, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=2.3.0.2011121518, name=Pydev Django )
P2Plugin( id=org.python.pydev.django, version=2.4.0.2012020116, name=Pydev Django )
P2Plugin( id=org.python.pydev.django_templates, version=1.6.2.2010090711, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.django_templates, version=1.6.2.2010090812, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.django_templates, version=1.6.3.2010100513, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.django_templates, version=1.6.4.2011010200, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.django_templates, version=1.6.5.2011020317, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.django_templates, version=2.0.0.2011040403, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.django_templates, version=2.1.0.2011052613, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.django_templates, version=2.2.0.2011062419, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.django_templates, version=2.2.1.2011071313, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.django_templates, version=2.2.2.2011082312, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.django_templates, version=2.2.3.2011100616, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.django_templates, version=2.2.4.2011110216, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.django_templates, version=2.3.0.2011121518, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.django_templates, version=2.4.0.2012020116, name=Aptana Django Templates Editor )
P2Plugin( id=org.python.pydev.help, version=1.5.0.1251989166, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.5.1.1258496115, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.5.2.1260362205, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.5.3.1260479439, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.5.4.2010011921, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.5.5.2010030420, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.5.6.2010033101, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.5.7.2010050621, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.5.8.2010062823, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.5.9.2010063000, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.5.9.2010063001, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.6.0.2010071813, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.6.1.2010080312, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.6.2.2010090711, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.6.2.2010090812, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.6.3.2010100513, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.6.4.2011010200, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=1.6.5.2011020317, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=2.0.0.2011040403, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=2.1.0.2011052613, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=2.2.0.2011062419, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=2.2.1.2011071313, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=2.2.2.2011082312, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=2.2.3.2011100616, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=2.2.4.2011110216, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=2.3.0.2011121518, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.help, version=2.4.0.2012020116, name=Pydev Online Help )
P2Plugin( id=org.python.pydev.jython, version=1.5.0.1251989166, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.5.1.1258496115, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.5.2.1260362205, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.5.3.1260479439, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.5.4.2010011921, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.5.5.2010030420, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.5.6.2010033101, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.5.7.2010050621, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.5.8.2010062823, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.5.9.2010063000, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.5.9.2010063001, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.6.0.2010071813, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.6.1.2010080312, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.6.2.2010090711, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.6.2.2010090812, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.6.3.2010100513, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.6.4.2011010200, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=1.6.5.2011020317, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=2.0.0.2011040403, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=2.1.0.2011052613, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=2.2.0.2011062419, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=2.2.1.2011071313, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=2.2.2.2011082312, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=2.2.3.2011100616, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=2.2.4.2011110216, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=2.3.0.2011121518, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.jython, version=2.4.0.2012020116, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.mylyn, version=0.3.0, name=Pydev Mylyn Integration Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.5.0.1251989166, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.5.1.1258496115, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.5.2.1260362205, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.5.3.1260479439, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.5.4.2010011921, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.5.5.2010030420, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.5.6.2010033101, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.5.7.2010050621, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.5.8.2010062823, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.5.9.2010063000, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.5.9.2010063001, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.6.0.2010071813, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.6.1.2010080312, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.6.2.2010090711, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.6.2.2010090812, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.6.3.2010100513, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.6.4.2011010200, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=1.6.5.2011020317, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=2.0.0.2011040403, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=2.1.0.2011052613, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=2.2.0.2011062419, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=2.2.1.2011071313, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=2.2.2.2011082312, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=2.2.3.2011100616, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=2.2.4.2011110216, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=2.3.0.2011121518, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.parser, version=2.4.0.2012020116, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.red_core, version=1.5.8.2010062823, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=1.5.9.2010063000, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=1.5.9.2010063001, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=1.6.0.2010071813, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=1.6.1.2010080312, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=1.6.2.2010090711, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=1.6.2.2010090812, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=1.6.3.2010100513, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=1.6.4.2011010200, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=1.6.5.2011020317, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=2.0.0.2011040403, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=2.1.0.2011052613, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=2.2.0.2011062419, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=2.2.1.2011071313, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=2.2.2.2011082312, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=2.2.3.2011100616, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=2.2.4.2011110216, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=2.3.0.2011121518, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.red_core, version=2.4.0.2012020116, name=Pydev Red Core )
P2Plugin( id=org.python.pydev.refactoring, version=1.5.0.1251989166, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.5.1.1258496115, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.5.2.1260362205, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.5.3.1260479439, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.5.4.2010011921, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.5.5.2010030420, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.5.6.2010033101, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.5.7.2010050621, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.5.8.2010062823, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.5.9.2010063000, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.5.9.2010063001, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.6.0.2010071813, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.6.1.2010080312, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.6.2.2010090711, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.6.2.2010090812, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.6.3.2010100513, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.6.4.2011010200, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=1.6.5.2011020317, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=2.0.0.2011040403, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=2.1.0.2011052613, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=2.2.0.2011062419, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=2.2.1.2011071313, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=2.2.2.2011082312, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=2.2.3.2011100616, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=2.2.4.2011110216, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=2.3.0.2011121518, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.refactoring, version=2.4.0.2012020116, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.templates, version=1.5.0.1251989166, name=Templates Plug-in )
P2Plugin( id=org.python.pydev.templates, version=1.5.1.1258496115, name=Templates Plug-in )
P2Plugin( id=org.python.pydev.templates, version=1.5.2.1260362205, name=Templates Plug-in )
P2Plugin( id=org.python.pydev.templates, version=1.5.3.1260479439, name=Templates Plug-in )
P2Plugin( id=org.python.pydev.templates, version=1.5.4.2010011921, name=Templates Plug-in )
P2Plugin( id=org.python.pydev.templates, version=1.5.5.2010030420, name=Templates Plug-in )
P2Plugin( id=org.python.pydev.templates, version=1.5.6.2010033101, name=Templates Plug-in )
P2Plugin( id=org.python.pydev.templates, version=1.5.7.2010050621, name=Templates Plug-in )'''
                , pydevRepo().plugins.join( '\n' ) )
    }
    
    @Test
    public void testPyDevUnits() throws Exception {

        assertEquals(
            '''a.jre 1.6.0'''
            , pydevRepo().units.join( '\n' ) )
    }
    
    @Test
    public void testPyDevOthers() throws Exception {
        
        assertEquals(
            ''''''
            , pydevRepo().others.join( '\n' ) )
    }
    
    @Test
    public void testPyDevLatestFeature() throws Exception {

        assertEquals( "P2Feature( id=org.python.pydev.django_templates.feature.feature.group, version=2.4.0.2012020116, name=PyDev Django Templates Editor )"
            , pydevRepo().latest( 'org.python.pydev.django_templates.feature.feature.group' ).toString() )
    }
    
    @Test
    public void testPyDevLatestPlugin() throws Exception {

        assertEquals( "P2Plugin( id=org.python.pydev.refactoring, version=2.4.0.2012020116, name=Python Refactoring Plug-In )"
            , pydevRepo().latest( 'org.python.pydev.refactoring' ).toString() )
    }
    
    @Test
    public void testPyDevLatestPluginVersionRange() throws Exception {

        assertEquals( "P2Plugin( id=org.python.pydev.refactoring, version=2.3.0.2011121518, name=Python Refactoring Plug-In )"
            , pydevRepo().latest( 'org.python.pydev.refactoring', new VersionRange( '[2.0.0,2.4.0)' ) ).toString() )
    }
    
    @Test
    public void testPyDevResolveDependencies() throws Exception {

        def deps = new DependencySet( repo: pydevRepo() )
        deps.resolveDependencies( "org.python.pydev.templates" )
        
        assertEquals( 
            '''\
P2Plugin( id=org.python.pydev.templates, version=1.5.7.2010050621, name=Templates Plug-in )
P2Plugin( id=org.python.pydev, version=2.4.0.2012020116, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev.ast, version=2.4.0.2012020116, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.parser, version=2.4.0.2012020116, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.core, version=2.4.0.2012020116, name=Core Plug-in )
P2Plugin( id=org.python.pydev.jython, version=2.4.0.2012020116, name=Jython Plug-in )'''
            , deps.bundles.join( '\n' ) )
   
        def unknown = [] + deps.unknown
        unknown.sort()  
        assertEquals( 
            '''\
P2Dependency( id=org.eclipse.compare, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.core.expressions, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.core.filebuffers, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.core.filesystem, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.core.resources, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.core.runtime, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.core.variables, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.debug.core, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.debug.ui, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.jdt.core, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.jdt.launching, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.jdt.ui, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.jface.text, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.ltk.core.refactoring, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.ltk.ui.refactoring, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.search, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.ui, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.ui.console, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.ui.editors, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.ui.ide, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.ui.navigator, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.ui.navigator.resources, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.ui.views, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.ui.workbench.texteditor, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.junit, version=[3.8.0,3.9.9], type=osgi.bundle )'''
            , unknown.join( '\n' ) )
        
        deps.download( new File( testFolder, 'testPyDevResolveDependencies' ) )
    }
    
    @Test
    public void testPyDevDownloadFeature() throws Exception {
        
        def deps = new DependencySet( repo: pydevRepo() )
        deps.resolveDependencies( "org.python.pydev.feature.feature.group" )
        
        assertEquals( 
            '''\
P2Feature( id=org.python.pydev.feature.feature.group, version=2.4.0.2012020116, name=PyDev for Eclipse )
P2Plugin( id=org.python.pydev.core, version=2.4.0.2012020116, name=Core Plug-in )
P2Plugin( id=org.python.pydev.parser, version=2.4.0.2012020116, name=Parser Plug-in )
P2Plugin( id=org.python.pydev.ast, version=2.4.0.2012020116, name=Ast Plug-in )
P2Plugin( id=org.python.pydev.jython, version=2.4.0.2012020116, name=Jython Plug-in )
P2Plugin( id=org.python.pydev.help, version=2.4.0.2012020116, name=Pydev Online Help )
P2Plugin( id=org.python.pydev, version=2.4.0.2012020116, name=Pydev - Python Development Environment )
P2Plugin( id=org.python.pydev.debug, version=2.4.0.2012020116, name=Pydev debug )
P2Plugin( id=org.python.pydev.refactoring, version=2.4.0.2012020116, name=Python Refactoring Plug-In )
P2Plugin( id=org.python.pydev.customizations, version=2.4.0.2012020116, name=Customizations Plug-in )
P2Plugin( id=org.python.pydev.django, version=2.4.0.2012020116, name=Pydev Django )
P2Plugin( id=com.python.pydev, version=2.4.0.2012020116, name=Pydev Extensions )
P2Plugin( id=com.python.pydev.analysis, version=2.4.0.2012020116, name=Analysis Plug-in )
P2Plugin( id=com.python.pydev.fastparser, version=2.4.0.2012020116, name=Fastparser Plug-in )
P2Plugin( id=com.python.pydev.codecompletion, version=2.4.0.2012020116, name=Codecompletion Plug-in )
P2Plugin( id=com.python.pydev.refactoring, version=2.4.0.2012020116, name=Refactoring Plug-in )
P2Plugin( id=com.python.pydev.debug, version=2.4.0.2012020116, name=Extensions Debug Plug-in )
P2Plugin( id=org.python.pydev.red_core, version=2.4.0.2012020116, name=Pydev Red Core )'''
            , deps.bundles.join( '\n' ) )
            
        def unknown = [] + deps.unknown
        unknown.sort()  
        assertEquals( 
            '''\
P2Dependency( id=com.aptana.editor.common, version=0.0.0, type=osgi.bundle )
P2Dependency( id=com.aptana.theme, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.compare, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.core.expressions, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.core.filebuffers, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.core.filesystem, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.core.resources, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.core.runtime, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.core.variables, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.debug.core, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.debug.ui, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.jdt.core, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.jdt.launching, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.jdt.ui, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.jface.text, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.ltk.core.refactoring, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.ltk.ui.refactoring, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.search, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.ui, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.ui.console, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.ui.editors, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.ui.ide, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.ui.navigator, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.ui.navigator.resources, version=0.0.0, type=osgi.bundle )
P2Dependency( id=org.eclipse.ui.views, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.eclipse.ui.workbench.texteditor, version=0.0.0, type=org.eclipse.equinox.p2.iu )
P2Dependency( id=org.junit, version=[3.8.0,3.9.9], type=osgi.bundle )'''
            , unknown.join( '\n' ) )
                
        deps.download( new File( testFolder, 'testPyDevDownloadFeature' ) )
    }
    
    @Test
    public void testParseIndigoSR2() throws Exception {
        def repo = new P2Repo( workDir: new File( testFolder, "testParseIndigoSR2" ), url: new URL( 'http://download.eclipse.org/releases/indigo/201202240900/' ) )
        repo.load()
        
        assertEquals( '''\
P2Category( id=Application Development Frameworks, version=0.0.0.67C3cLWJM6, name=Application Development Frameworks )
P2Category( id=Business Intelligence, Reporting and Charting, version=0.0.0.7H7e7AcLUh6hBcMAGMAMGS5sErZT, name=Business Intelligence, Reporting and Charting )
P2Category( id=Collaboration, version=0.0.0.7f8gC_cLS3Q6D3_j23pU4Me0eLz0, name=Collaboration )
P2Category( id=Database Development, version=0.0.0.27G3cLdSS08s73553K5E5ECC1GlM, name=Database Development )
P2Category( id=EclipseRT Target Platform Components, version=0.0.0.7O7a7AcLWqpJBkQGMNXUcQRK1RiD, name=EclipseRT Target Platform Components )
P2Category( id=General Purpose Tools, version=0.0.0.8K81BFcLS3Q6E7R50Bz-XajnO4dz, name=General Purpose Tools )
P2Category( id=Linux Tools, version=0.0.0.17a1cLf7kb7QCJQCQJlQCXQ, name=Linux Tools )
P2Category( id=Mobile and Device Development, version=0.0.0.7b7_CXcLbLnlAUmbhgwfJ1XsKLd4, name=Mobile and Device Development )
P2Category( id=Modeling, version=0.0.0.7z9o7JcLSSHHENJfrFEt_EPhpRDp, name=Modeling )
P2Category( id=Programming Languages, version=0.0.0.887C7AcLTAY6BYkowfw21Gms289g, name=Programming Languages )
P2Category( id=SOA Development, version=0.0.0.7E7H-cLTKnh8MGLFFuRnUEvZ208P, name=SOA Development )
P2Category( id=Testing, version=0.0.0.32-cLY3de, name=Testing )
P2Category( id=Web, XML, Java EE and OSGi Enterprise Development, version=0.0.0.43-cLWd767w31221627022110880, name=Web, XML, Java EE and OSGi Enterprise Development )'''
            , repo.categories.join( '\n' ) )
        assertEquals( 854, repo.features.size() )
        assertEquals( 4541, repo.plugins.size() )
        assertEquals( 114, repo.units.size() )
        
        assertEquals( ''''''
            , repo.others.join( '\n' ) )
        
        //println repo.latest( 'org.eclipse.m2e.feature.feature.group' )
        //println repo.others.join( '\n' )
    }
    
    @Test
    public void testParseIndigoSR2Epps() throws Exception {
        def repo = new P2Repo( workDir: new File( testFolder, "testParseIndigoSR2Epps" ), url: new URL( 'http://download.eclipse.org/technology/epp/packages/indigo/SR2/' ) )
        repo.load()
        
        assertEquals( 0, repo.categories.size() )
        assertEquals( 23, repo.features.size() )
        assertEquals( 17, repo.plugins.size() )
        assertEquals( 727, repo.units.size() )
        
        assertEquals( ''''''
            , repo.others.join( '\n' ) )
    }
    
    @Test
    public void testM2E() throws Exception {
        def url = new URL( 'http://download.eclipse.org/technology/m2e/releases/1.0/1.0.200.20111228-1245' )
        def repo = new P2Repo( workDir: new File( testFolder, "m2e" ), url: url )
        repo.load()
        
        def bundle = repo.latest( 'org.slf4j.api' )
        assertEquals( 'P2Plugin( id=org.slf4j.api, version=1.6.1.v20100831-0715, name=SLF4J API )', bundle.toString() )
        assertEquals( 
            '''\
P2Dependency( id=ch.qos.logback.slf4j, version=[0.9.27.v20110224-1110,0.9.27.v20110224-1110], type=osgi.bundle )
P2Dependency( id=org.slf4j.api, version=1.6.1.v20100831-0715, type=osgi.bundle )'''
            , bundle.dependencies.join( '\n' ) )
        
        bundle = repo.latest( 'ch.qos.logback.slf4j' )
        assertEquals( 'P2Plugin( id=ch.qos.logback.slf4j, version=0.9.27.v20110224-1110, name=Logback Native SLF4J Logger Module )', bundle.toString() )
        
        def deps = new DependencySet( repo: repo )
        deps.resolveDependencies( "org.eclipse.m2e.feature.feature.group" )
        deps.resolveDependencies( "org.eclipse.m2e.logback.feature.feature.group" )
        
        def unknownIds = deps.unknownIds as ArrayList
        unknownIds.sort()
        assertEquals( '''\
com.ibm.icu
groovy.lang
javax.crypto
javax.crypto.spec
javax.jms
javax.mail
javax.mail.internet
javax.management
javax.naming
javax.net
javax.net.ssl
javax.servlet
javax.servlet.http
javax.sql
javax.xml.parsers
org.codehaus.groovy.control
org.codehaus.groovy.reflection
org.codehaus.groovy.runtime
org.codehaus.groovy.runtime.callsite
org.codehaus.groovy.runtime.typehandling
org.codehaus.janino
org.eclipse.compare
org.eclipse.compare.rangedifferencer
org.eclipse.core.expressions
org.eclipse.core.filebuffers
org.eclipse.core.filesystem
org.eclipse.core.jobs
org.eclipse.core.resources
org.eclipse.core.runtime
org.eclipse.core.variables
org.eclipse.debug.core
org.eclipse.debug.ui
org.eclipse.emf.ecore
org.eclipse.emf.ecore.edit
org.eclipse.emf.ecore.xmi
org.eclipse.emf.edit
org.eclipse.emf.edit.ui
org.eclipse.epp.usagedata.gathering
org.eclipse.equinox.common
org.eclipse.equinox.internal.p2.discovery.compatibility
org.eclipse.equinox.internal.p2.ui
org.eclipse.equinox.internal.p2.ui.dialogs
org.eclipse.equinox.internal.p2.ui.model
org.eclipse.equinox.internal.p2.ui.viewers
org.eclipse.equinox.internal.provisional.configurator
org.eclipse.equinox.p2.core
org.eclipse.equinox.p2.discovery
org.eclipse.equinox.p2.discovery.compatibility
org.eclipse.equinox.p2.engine
org.eclipse.equinox.p2.metadata
org.eclipse.equinox.p2.operations
org.eclipse.equinox.p2.repository
org.eclipse.equinox.p2.repository.metadata
org.eclipse.equinox.p2.ui
org.eclipse.equinox.p2.ui.discovery
org.eclipse.equinox.registry
org.eclipse.jdt.core
org.eclipse.jdt.debug.ui
org.eclipse.jdt.feature.group
org.eclipse.jdt.junit
org.eclipse.jdt.launching
org.eclipse.jdt.ui
org.eclipse.jem.util
org.eclipse.jface
org.eclipse.jface.text
org.eclipse.ltk.core.refactoring
org.eclipse.ltk.core.refactoring.resource
org.eclipse.ltk.ui.refactoring
org.eclipse.osgi
org.eclipse.platform.feature.group
org.eclipse.rcp.feature.group
org.eclipse.search
org.eclipse.search.ui.text
org.eclipse.swt
org.eclipse.ui
org.eclipse.ui.console
org.eclipse.ui.editors
org.eclipse.ui.externaltools
org.eclipse.ui.forms
org.eclipse.ui.forms.editor
org.eclipse.ui.ide
org.eclipse.ui.workbench
org.eclipse.ui.workbench.texteditor
org.eclipse.wst.common.emf
org.eclipse.wst.common.uriresolver
org.eclipse.wst.sse.core
org.eclipse.wst.sse.ui
org.eclipse.wst.xml.core
org.eclipse.wst.xml.ui
org.eclipse.wst.xsd.core
org.ietf.jgss
org.maven.ide.eclipse
org.osgi.framework
org.osgi.service.log
org.osgi.util.tracker
org.w3c.dom
org.xml.sax
org.xml.sax.helpers
sun.reflect''', unknownIds.join( '\n' ) )
        
        assertEquals(
            '''\
P2Feature( id=org.eclipse.m2e.feature.feature.group, version=1.0.200.20111228-1245, name=m2e - Maven Integration for Eclipse )
P2Plugin( id=org.eclipse.m2e.maven.indexer, version=1.0.200.20111228-1245, name=Maven / Nexus Indexer Bundle )
P2Plugin( id=org.eclipse.m2e.maven.runtime, version=1.0.200.20111228-1245, name=Embedded Maven Runtime Bundle )
P2Plugin( id=com.ning.async-http-client, version=1.6.3.201112281337, name=async-http-client )
P2Plugin( id=org.jboss.netty, version=3.2.4.Final-201112281337, name=The Netty Project )
P2Plugin( id=org.slf4j.api, version=1.6.1.v20100831-0715, name=SLF4J API )
P2Plugin( id=ch.qos.logback.slf4j, version=0.9.27.v20110224-1110, name=Logback Native SLF4J Logger Module )
P2Plugin( id=ch.qos.logback.classic, version=0.9.27.v20110224-1110, name=Logback Classic Module )
P2Plugin( id=ch.qos.logback.core, version=0.9.27.v20110224-1110, name=Logback Core Module )
P2Plugin( id=org.eclipse.m2e.archetype.common, version=1.0.200.20111228-1245, name=Maven Archetype Common Bundle )
P2Plugin( id=org.eclipse.m2e.editor, version=1.0.200.20111228-1245, name=Maven Integration for Eclipse (Editors) )
P2Plugin( id=org.eclipse.m2e.core, version=1.0.200.20111228-1245, name=Maven Integration for Eclipse )
P2Plugin( id=org.eclipse.m2e.editor.xml, version=1.0.200.20111228-1245, name=Maven POM XML Editor )
P2Plugin( id=org.eclipse.m2e.core.ui, version=1.0.200.20111228-1245, name=Maven Integration for Eclipse )
P2Plugin( id=org.eclipse.m2e.model.edit, version=1.0.200.20111228-1245, name=Maven Project Model Edit Bundle )
P2Plugin( id=org.eclipse.m2e.jdt, version=1.0.200.20111228-1245, name=Maven Integration for Eclipse JDT )
P2Plugin( id=org.eclipse.m2e.lifecyclemapping.defaults, version=1.0.200.20111228-1245, name=Default Build Lifecycle Mapping Metadata )
P2Plugin( id=org.eclipse.m2e.scm, version=1.0.200.20111228-1245, name=SCM Maven Integration for Eclipse )
P2Plugin( id=org.eclipse.m2e.discovery, version=1.0.200.20111228-1245, name=m2e Marketplace )
P2Plugin( id=org.eclipse.m2e.usagedata, version=1.0.200.20111228-1245, name=m2e / UDC integration Marketplace )
P2Plugin( id=org.eclipse.m2e.refactoring, version=1.0.200.20111228-1245, name=Maven Integration for Eclipse Refactoring )
P2Plugin( id=org.eclipse.m2e.launching, version=1.0.200.20111228-1245, name=Maven Integration for Eclipse Launching )
P2Feature( id=org.eclipse.m2e.logback.feature.feature.group, version=1.0.200.20111228-1245, name=m2e - slf4j over logback logging (Optional) )
P2Plugin( id=org.eclipse.m2e.logback.configuration, version=1.0.200.20111228-1245, name=m2e logback configuration )
P2Plugin( id=org.eclipse.m2e.logback.appender, version=1.0.200.20111228-1245, name=m2e logback appender )'''
            , deps.bundles.join( '\n' ) )
        
        deps.download( new File( testFolder, 'org.eclipse.m2e' ) )
    }
    
}

class MockProgressFactory extends ProgressFactory {
    @Override
    public Progress newProgress( long contentLength ) {
        return new MockProgress( contentLength );
    }
}

class MockProgress extends Progress {
    MockProgress( long contentLength ) {
        super( contentLength )
    }
    
    @Override
    void printProgress( long progress, int p ) {
        // NOP
    }
    
    @Override
    void close() {
        // NOP
    }
}