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

import groovy.util.IndentPrinter;

interface IP2Repo {
    P2Bundle latest( String id )
    P2Bundle latest( String id, VersionRange range )
    P2Bundle find( String id, Version version )
    void list( IndentPrinter out )
}
