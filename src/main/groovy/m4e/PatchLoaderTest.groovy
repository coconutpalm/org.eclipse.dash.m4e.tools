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
import org.junit.Test;

class PatchLoaderTest {

    @Test
    public void testLoadEclipse362() throws Exception {
        
        PatchLoader loader = new PatchLoader( new File( 'patches/eclipse-3.6.2.patches' ) )
        
        def patch = loader.load()
        
        assertEquals( 
            '''\
DeleteDependency( system.bundle:system.bundle:[0,) )
ReplaceDependencies( defaultProfile=m4e.orbit, profile=m4e.maven-central, replacements=63 )''', patch?.patches?.join( '\n' ) )
    }

    @Test
    public void testLoadEclipse370() throws Exception {
        
        PatchLoader loader = new PatchLoader( new File( 'patches/eclipse-3.7.0.patches' ) )
        
        def patch = loader.load()
        
        assertEquals( 
            '''\
DeleteDependency( system.bundle:system.bundle:[0,) )
ReplaceDependencies( defaultProfile=m4e.orbit, profile=m4e.maven-central, replacements=70 )''', patch?.patches?.join( '\n' ) )
    }
    
    @Test
    public void testDuplicateReplacements() throws Exception {
        PatchLoader loader = new PatchLoader( '''\
replace( 'a:b:1', 'x:y:1' )
replace( 'a:b:1', 'x:y:2' )
''' )
        
        try {
            loader.load()
            fail( 'Expected exception' )
        } catch( UserError e ) {
            assertEquals( "Found duplicate replace a:b:1 in patch '{set.source}'".toString(), e.message )
        }
    }
}
