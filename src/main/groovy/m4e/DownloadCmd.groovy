/*******************************************************************************
 * Copyright (c) 27.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e

import m4e.p2.Downloader;
import m4e.ui.M2RepoView;

class DownloadCmd extends AbstractCommand {

    static final String DESCRIPTION = '''\
url...
- Download one or more URLs from eclipse.org and put them into tmp/downloads/
'''
    
    @Override
    public void run( String... args ) {
        
        if( args.size() == 1 ) {
            throw new UserError( 'Missing URLs to download' )
        }
        
        def downloader = new Downloader( cacheRoot: new File( workDir, 'p2' ) )
        File target = new File( workDir, 'downloads' )
        
        args[1..-1].collect { new URL( it ) }.each { url ->
            def dest = new File( target, PathUtils.basename( url.path ) )
            
            if( dest.exists() ) {
                log.info( 'Already downloaded {}', dest.name )
            } else {
                def cached = downloader.download( url )
                cached.copy( dest )
            }
        }
    }
}
