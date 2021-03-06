/*******************************************************************************
 * Copyright (c) 23.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e.p2;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import m4e.CommonTestCode;
import m4e.MopSetup;

class PyDevTest {
    
    static {
        MopSetup.setup()
    }

    static File contentJar = CommonTestCode.newFile( "pydevRepo/content.jar" )
    static File contentXmlFile = CommonTestCode.newFile( "pydevRepo/content.xml" )
    
    static boolean testDownloadRanOnce
    
    @Test
    public void testDownload() throws Exception {
        if( testDownloadRanOnce ) {
            return
        }
        
        testDownloadRanOnce = true
        
        contentJar.usefulDelete()
        
        def url = new File( "data/input/pydev" ).toURI().toURL()
        
        File workDir = CommonTestCode.newFile( 'pydevRepo' )
        def downloader = new Downloader( cacheRoot: new File( workDir, 'p2' ), progressFactory: new MockProgressFactory() )
        File jar = downloader.download( new URL( url, "content.jar" ) )
        
        assertTrue( jar.exists() )
        
        jar.copy( contentJar )
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
        
        def loader = new P2RepoLoader( workDir: CommonTestCode.newFile( 'pydevRepo' ) )
        loader.unpackContentJar( contentJar )
        
        assertTrue( contentXmlFile.exists() )
    }
    
    static P2Repo __pydevRepo
    
    private P2Repo pydevRepo() {
        if( __pydevRepo ) {
            return __pydevRepo
        }
        
        testUnpack()
        
        def url = new File( "data/input/pydev" ).toURI().toURL()
        def repo = new P2Repo(  workDir: CommonTestCode.newFile( 'pydevRepo' ), url: url )
        
        def downloader = new Downloader( cacheRoot: new File( repo.workDir, 'p2' ), progressFactory: new MockProgressFactory() )
        repo.downloader = downloader
        
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
        
        deps.download( CommonTestCode.newFile( 'testPyDevResolveDependencies' ) )
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
                
        deps.download( CommonTestCode.newFile( 'testPyDevDownloadFeature' ) )
    }
    

}
