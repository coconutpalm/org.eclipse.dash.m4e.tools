package m4e

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractCommand {

    final Logger log = LoggerFactory.getLogger( getClass() )
    
    File workDir
    
    abstract void run( String... args );
}
