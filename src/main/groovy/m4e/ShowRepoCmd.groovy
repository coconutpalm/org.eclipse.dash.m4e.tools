/*******************************************************************************
 * Copyright (c) 26.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e

import m4e.ui.M2RepoView;

class ShowRepoCmd extends AbstractCommand {

    static final String DESCRIPTION = '''\
repository
- Show the content of an Maven 2 repository in a Swing UI
'''
    
    @Override
    public void doRun( String... args ) {
        
        File repo = repoOption( args, 1 )
        def view = new M2RepoView( repo: repo )
        view.show()
    }

}
