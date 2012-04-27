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

import java.util.Map;


class VersionCache {
    
    Map<String, Version> versions = [:].withDefault { pattern -> new Version( pattern ) }
    Map<String, VersionRange> ranges = [:].withDefault { pattern -> new VersionRange( pattern, this ) }
    
    Version version( String pattern ) {
        return versions.get( pattern )
    }
    
    VersionRange range( String pattern ) {
        return ranges.get( pattern )
    }
}
