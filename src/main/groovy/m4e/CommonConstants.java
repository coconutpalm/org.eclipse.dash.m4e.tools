package m4e;

public interface CommonConstants {

    /** Folder name inside of a Maven 2 repository where MT4E will put its files */
    static final String MT4E_FOLDER = ".mt4e";
    
    /** File name of the import/export DB in the MT4E folder */
    static final String IMPORT_EXPORT_DB_FILE = "importExportDB";
    
    /** File name of the snapshot version mapping file in the MT4E folder */
    static final String SNAPSHOT_VERSION_MAPPING_FILE = "snapshotVersionMapping";
    
    /** UTF-8 encoding/charset */
    static final String UTF_8 = "UTF-8";
    
    static final String SNAPSHOT = "SNAPSHOT";
    static final String MINUS_SNAPSHOT = "-" + SNAPSHOT;
}
