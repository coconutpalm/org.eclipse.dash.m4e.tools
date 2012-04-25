/*******************************************************************************
 * Copyright (c) 25.07.2011 Aaron Digulla.
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

class PathUtilsTest {

    @Test
    public void testNormalizeString() throws Exception {
        assert 'a' == PathUtils.normalize( 'a' )
        assert 'a/b' == PathUtils.normalize( 'a/b' )
        assert 'a/b' == PathUtils.normalize( 'a\\b' )
    }
    
    @Test
    public void testNormalizeFile() throws Exception {
        
        def f = new File( 'a' ).canonicalFile.toString().replace( '\\', '/' )
        
        assert f == PathUtils.normalize( new File( 'a' ) )
        
        f += '/b'
        assert f == PathUtils.normalize( new File( 'a/b' ) )
        assert f == PathUtils.normalize( new File( 'a\\b' ) )
    }
    
    @Test
    public void testBasename() throws Exception {
        assert 'a' == PathUtils.basename( 'a' )
        assert 'a' == PathUtils.basename( new File( 'a' ) )
        assert 'b' == PathUtils.basename( 'a/b' )
        assert 'b' == PathUtils.basename( 'a\\b' )
        assert 'b' == PathUtils.basename( new File( 'a/b' ) )
        assert 'b' == PathUtils.basename( new File( 'a\\b' ) )

    }
    
    @Test
    public void testDirnameString() throws Exception {
        assert 'a' == PathUtils.dirname( 'a' )
        assert 'x' == PathUtils.dirname( 'x/b' )
        assert 'y' == PathUtils.dirname( 'y\\b' )
    }
    
    @Test
    public void testDirname() throws Exception {
        def f = new File( 'a' ).canonicalFile.parentFile.toString().replace( '\\', '/' )
        assert f == PathUtils.dirname( new File( 'a' ) )
        
        f = new File( 'y' ).canonicalFile.toString().replace( '\\', '/' )
        assert f == PathUtils.dirname( new File( 'y/b' ) )
        
        f = new File( 'x' ).canonicalFile.toString().replace( '\\', '/' )
        assert f == PathUtils.dirname( new File( 'x\\b' ) )
    }
}
