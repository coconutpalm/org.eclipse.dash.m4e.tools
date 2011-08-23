package m4e;

import static org.junit.Assert.*;
import groovy.xml.MarkupBuilder;
import org.junit.Test;

class AnalyzeCmdTest {

    @Test
    public void testRenderProblem() throws Exception {
        def buffer = new StringWriter()
        def builder = new MarkupBuilder( buffer )
        
        def pom = [ key: { return 'group:artifact:version' } ] as Pom
        
        def problem = new Problem( pom, 'blabla' )
        problem.render( builder )
        
        assertEquals( '''\
<div class='problem'>
  <span class='pom'>group:artifact:version</span>
  <span class='message'>blabla</span>
</div>''', buffer.toString() )
    }
    
    @Test
    public void testAnalyzeRepo1() throws Exception {
        
        MopSetup.setup()
        
        File source = new File( 'data/input/repo1' )
        File copy = new File( 'tmp', source.name )
        
        source.copy( copy )
        
        Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( 'GMT' ) )
        cal.setTimeInMillis( 0L )
        
        Analyzer tool = new Analyzer( copy, cal )
        tool.run()
        
        String expected = new File( 'data/expected/repo1-analysis-19700101-000000.html' ).getText( 'utf-8' )
        String actual = tool.reportFile.getText( 'utf-8' )
        
        assertEquals( expected, actual )
    }
}
