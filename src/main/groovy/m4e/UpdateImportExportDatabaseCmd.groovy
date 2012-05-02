package m4e

import m4e.maven.ImportExportDB

class UpdateImportExportDatabaseCmd extends AbstractCommand {

    static final String DESCRIPTION = '''\
repository
- Create or update an existing import/export database
'''
    
    @Override
    public void run( String... args ) {
        File repo = repoOption( args, 1 )
        
        File dbPath = new File( repo, '.mt4e/importExportDB' )
        
        def db = new ImportExportDB( file: dbPath )
        
        int count
        MavenRepositoryTools.eachPom( repo ) {
            def pom = Pom.load( it )
            
            count ++
            db.updatePom( pom )
        }
        
        log.info( "Processed ${count} artifacts" )
        log.info( "Found ${db.size()} with import/export information" )
        
        db.save()
    }

}
