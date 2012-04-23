package m4e.p2

class P2Exception extends RuntimeException {
    P2Exception( String message ) {
        super( message )
    }
    
    P2Exception( String message, Throwable cause ) {
        super( message, cause )
    }
}