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

/** Bean that is used by the ReplaceDependencies tool to replace one dependency with another */
class ReplaceDependency {
    PatchDependency pattern
    PatchDependency replacement
    
    @Override
    public String toString() {
        return "ReplaceDependency( ${pattern} -> ${replacement} )"
    }

}
