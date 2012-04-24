package m4e.patch

import java.util.regex.Pattern;

class QualifierPatch {
    /** Apply this patch to these POMs */
    Pattern pattern
    /** The new version string for matching POMs */
    String version
    
    QualifierPatch( String pattern, String version ) {
        this.pattern = compile( pattern )
        this.version = version
    }
    
    Pattern compile( String text ) {
        return Pattern.compile( text.replace( '.', '\\.' ).replace( '*', '[^:]*' ) )
    }
    
    boolean appliesTo( String key ) {
        if( pattern.matcher( key ).matches() ) {
            return true
        }
        
        return false
    }
}
