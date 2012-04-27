/*******************************************************************************
 * Copyright (c) 24.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e.patch

import java.util.List;
import m4e.Pom;

/** A patch that delegates to a list of other patches */
class PatchSet extends Patch {
    List<Patch> patches = []
    
    void apply( Pom pom ) {
        for( def patch in patches ) {
            patch.apply( pom )
        }
    }
}
