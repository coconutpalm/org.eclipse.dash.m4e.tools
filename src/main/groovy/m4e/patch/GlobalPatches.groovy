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

class GlobalPatches {
    List<String> artifactsToDelete = []
    List<QualifierPatch> qualifierPatches = []
    List<String> orbitExclusions = []
    List<DeleteClasses> deleteClasses = []
    
    boolean renameOrbitBundles
    
    void merge( GlobalPatches other ) {
        artifactsToDelete.addAll( other.artifactsToDelete )
        qualifierPatches.addAll( other.qualifierPatches )
        orbitExclusions.addAll( other.orbitExclusions )
        deleteClasses.addAll( other.deleteClasses )
        
        renameOrbitBundles |= other.renameOrbitBundles
    }
}
