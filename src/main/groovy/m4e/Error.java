package m4e;

public enum Error {
    TWO_VERSIONS( 1 ),
    MAVEN_FAILED( 2 );
    
    private final int id;
    
    private Error( int id ) {
        this.id = id;
    }
    
    public String url() {
        return Warning.BASE_URL + String.format( "E%04d", id );
    }
}
