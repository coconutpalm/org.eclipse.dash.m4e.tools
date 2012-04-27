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

package m4e.p2

import java.util.Map;

class DependencyCache {
    Map<String, P2Dependency> cache = [:]
    
    P2Dependency dependency( String id, String type, VersionRange versionRange ) {
        String key = "${id}:${type}:${versionRange}"
        
        P2Dependency result = cache.get( key )
        if( !result ) {
            result = new P2Dependency( id: id, type: type, versionRange: versionRange )
            cache[key] = result
        }
        
        return result
    }
}
