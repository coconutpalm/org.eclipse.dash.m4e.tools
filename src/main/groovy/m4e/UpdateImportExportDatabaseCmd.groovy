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

package m4e

import m4e.maven.ImportExportDB

class UpdateImportExportDatabaseCmd extends AbstractCommand {

    static final String DESCRIPTION = '''\
repository
- Create or update an existing import/export database
'''
    
    @Override
    public void doRun( String... args ) {
        File repo = repoOption( args, 1 )
        
        File dbPath = new File( repo, MT4E_FOLDER + '/' + IMPORT_EXPORT_DB_FILE )
        
        def db = new ImportExportDB( file: dbPath )
        
        int count = 0
        MavenRepositoryTools.eachPom( repo ) {
            def pom = Pom.load( it )
            log.debug( 'Processing {}', pom.key() )
            
            count ++
            db.add( pom )
        }
        
        log.info( "Processed ${count} artifacts" )
        log.info( "Found ${db.size()} with import/export information" )
        
        db.save()
    }

}
