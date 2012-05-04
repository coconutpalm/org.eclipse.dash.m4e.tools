/*******************************************************************************
 * Copyright (c) 02.05.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e.p2;

import java.net.URL;

class P2DownloadException extends P2Exception {

    final URL url
    final List<URL> urls
    
    P2DownloadException( URL url, List<URL> urls ) {
        super( "Unable to download ${url} from\n${urls?.join( '\n' )}" )
        
        this.url = url
        this.urls = urls
    }
    
    P2DownloadException( URL url ) {
        super( "Unable to download ${url} (cached)" )
        
        this.url = url
    }
}
