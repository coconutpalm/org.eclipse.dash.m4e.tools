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
package m4e;

import static org.junit.Assert.*;
import groovy.xml.MarkupBuilder;
import org.junit.Test;

class AnalyzeCmdTest implements CommonConstants {

    @Test
    public void testRenderProblem() throws Exception {
        def buffer = new StringWriter()
        def builder = new MarkupBuilder( buffer )
        
        def pom = [ key: { return 'group:artifact:version' } ] as Pom
        
        def problem = new Problem( pom, 'blabla' )
        problem.render( builder )
        
        assertEquals( '''\
<div class='problem'>POM 
  <span class='pom'>group:artifact:version</span> 
  <span class='message'>blabla</span>
</div>''', buffer.toString() )
    }
    
    @Test
    public void testAnalyzeRepo1() throws Exception {
        
        File source = new File( 'data/input/repo1' )
        File copy = CommonTestCode.prepareRepo( source, source.name )
        
        Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( 'GMT' ) )
        cal.setTimeInMillis( 0L )
        
        Analyzer tool = new Analyzer( copy, cal )
        tool.run()
        
        String expected = new File( 'data/expected/repo1-analysis-19700101-000000.html' ).getText( UTF_8 ).normalize().trim()
        String actual = tool.reportFile.getText( 'utf-8' ).normalize().trim().replace( tool.repo.path, '${repo}' )
        
        assertEquals( expected, actual )
    }
    
    @Test
    public void testAnalyzeRepo1WithIgnores() throws Exception {
        
        File source = new File( 'data/input/repo1' )
        File copy = CommonTestCode.prepareRepo( source, 'repo1WithIgnores' )
        
        Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( 'GMT' ) )
        cal.setTimeInMillis( 1336642150000L )
        
        Analyzer tool = new Analyzer( copy, cal )
        
        tool.loadIgnores( new File( 'data/input/repo1.ignore') )
        
        tool.run()
        
        String expected = new File( 'data/expected/repo1WithIgnores-analysis.html' ).getText( UTF_8 ).normalize().trim()
        String actual = tool.reportFile.getText( 'utf-8' ).normalize().trim().replace( tool.repo.path, '${repo}' )
        
        assertEquals( expected, actual )
    }
}
