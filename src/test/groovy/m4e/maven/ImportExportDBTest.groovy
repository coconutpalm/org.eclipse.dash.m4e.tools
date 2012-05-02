package m4e.maven;

import static org.junit.Assert.*;
import m4e.CommonTestCode;
import org.junit.Test;

class ImportExportDBTest {

    public final static File TEST_DATA_FILE = new File( 'data/input/importExportDB.txt' )
    
    @Test
    public void testLoad() throws Exception {
        
        def db = new ImportExportDB( file: TEST_DATA_FILE )
        db.load()
        
        assert 3 == db.size()
    }
    
    @Test
    public void testSave() throws Exception {
        
        def db = new ImportExportDB( file: TEST_DATA_FILE )
        db.load()
        
        db.file = CommonTestCode.newFile( 'ImportExportDBTest/testSave/db' )
        db.save()
        
        String expected = TEST_DATA_FILE.getText( 'UTF-8' )
        String actual = db.file.getText( 'UTF-8' )
        
        assertEquals( expected, actual )
    }
    
    @Test
    public void testQuery() throws Exception {
        
        def db = new ImportExportDB( file: TEST_DATA_FILE )
        db.load()
        
        def l = db.artifactsThatExport( 'javax.servlet' )
        assertEquals( '[javax.servlet:javax.servlet:2.5.0]', l.toString() )
    }
}
