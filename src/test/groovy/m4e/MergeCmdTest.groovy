package m4e;

import static org.junit.Assert.*;
import org.junit.Test;

class MergeCmdTest extends CommonTestCode implements CommonConstants {

    @Test
    public void testMerge() throws Exception {
        
        File target = newFile( 'testMerge' )
        target.parentFile?.makedirs()
        
        def tool = new MergeCmd()
        def args = [ 'merge', 'data/input/mergeTest/repo1', 'data/input/mergeTest/repo2', target ]
        def input1 = new File( 'data/input/mergeTest/repo1' ).absoluteFile.normalize()
        def input2 = new File( 'data/input/mergeTest/repo2' ).absoluteFile.normalize()
        def output = target.absoluteFile.normalize()
        def read = { file ->
            def text = file.getText( UTF_8 )
            
            text.replace( input1, '${input1}' )
            .replace( input2, '${input2}' )
            .replace( input2, '${input2}' )
            .replace( output, '${m2repo}' )
        }
        
        try {
            tool.run( args )
        } catch( UserError e ) {
            String expected = "Target repository ${target.absolutePath} already exists. Cowardly refusing to continue."
            assertEquals( expected, e.message )
        }
        
        assert target.deleteDir()
        
        tool.run( args )
        
        assert target.exists()
        
        File mt4eFolder = new File( target, MT4E_FOLDER )
        
        assertEquals( '''\
logs/install.xml
logs/merge.xml
logs/only_repo1.xml
logs/only_repo2.xml
snapshotVersionMapping'''
            , listFiles( mt4eFolder ) )
        
        File logFolder = new File( mt4eFolder, 'logs' )
        
        def lines = read( new File( logFolder, 'merge.xml' ) ).split( '\n' )
        lines = [lines[0]] + lines[1..-2].sort() + [lines[-1]]
        
        assertEquals( '''\
<mt4e-log command='merge'>
<warning code='W0003' source='${input2}/org/eclipse/core/org.eclipse.core.resources/3.7.101/org.eclipse.core.resources-3.7.101.jar' target='${m2repo}/org/eclipse/core/org.eclipse.core.resources/3.7.101/org.eclipse.core.resources-3.7.101.jar'>File ${input2}/org/eclipse/core/org.eclipse.core.resources/3.7.101/org.eclipse.core.resources-3.7.101.jar differs from ${m2repo}/org/eclipse/core/org.eclipse.core.resources/3.7.101/org.eclipse.core.resources-3.7.101.jar</warning>
<warning code='W0006'>There is an existing mapping d:x:1.1-SNAPSHOT:1.1 -&gt; 1.2</warning>
</mt4e-log>'''
            , lines.join( '\n' ) )
        
        assertEquals( '''\
<merged>
<source file="${input1}/.mt4e/logs/install.xml">
<mt4e-log command='install'>
<error code='E0003' jar='${input}/plugins/nomanifest.jar'>Can't find manifest in ${input}/plugins/nomanifest.jar</error>
</mt4e-log>

</source>
<source file="${input2}/.mt4e/logs/install.xml">
<mt4e-log command='install'>
<warning code='W0004' jar='.../m2repo/org/eclipse/birt/org.eclipse.birt.report.data.oda.jdbc.dbprofile/3.7.0.v20110603/org.eclipse.birt.report.data.oda.jdbc.dbprofile-3.7.0.v20110603.jar' nestedJarPath='.,src'>Multiple nested JARs are not supported; just copying the original bundle</warning>
</mt4e-log>

</source>
</merged>
'''
            , read( new File( logFolder, 'install.xml' ) ) )
            
        assertEquals( '''\
<merged>
<source file="${input1}/.mt4e/logs/only_repo1.xml">
<mt4e-log command=dummy/>

</source>
</merged>
'''
            , read( new File( logFolder, 'only_repo1.xml' ) ) )
        
        assertEquals( '''\
<merged>
<source file="${input2}/.mt4e/logs/only_repo2.xml">
<mt4e-log command=dummy/>

</source>
</merged>
'''
            , read( new File( logFolder, 'only_repo2.xml' ) ) )
        
        fileEquals( '''\
b:y 1.0 1.0-SNAPSHOT
c:x 1.0 1.0-SNAPSHOT
d:x 1.1 1.1-SNAPSHOT
b:x 2.0 2.0-SNAPSHOT'''
            , new File( mt4eFolder, SNAPSHOT_VERSION_MAPPING_FILE ) )
        
        assertEquals( 'xxx\n', read( new File( target, 'org/eclipse/core/org.eclipse.core.resources/3.7.101/org.eclipse.core.resources-3.7.101.jar' ) ) )
        assertEquals( 'xxx\n', read( new File( target, 'org/eclipse/core/org.eclipse.core.resources/3.7.101/org.eclipse.core.resources-3.7.101.pom' ) ) )
    }
}
