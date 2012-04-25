package m4e;

public enum Warning {
    MISSING_BINARY_BUNDLE_FOR_SOURCES( 1 ),
    UNEXPECTED_FILE_IN_SOURCE_BUNDLE( 2 ),
    BINARY_DIFFERENCE( 3 );
    
    public final static String BASE_URL = "http://wiki.eclipse.org/MT4E_";
    
    private final int id;
    
    private Warning( int id ) {
        this.id = id;
    }
    
    public String url() {
        return BASE_URL + String.format( "W%04d", id );
    }
}