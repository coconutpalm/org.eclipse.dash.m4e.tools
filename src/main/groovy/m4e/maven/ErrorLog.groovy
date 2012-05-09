package m4e.maven;

import groovy.xml.MarkupBuilder

public class ErrorLog {

    File repo
    String command
    
    Writer writer
    MarkupBuilder builder
    
    MarkupBuilder write() {
        if( ! builder ) {
            File file = new File( repo, ".mt4e/logs/${command}.xml" )
            
            file.parentFile?.makedirs()
            
            writer = file.newWriter( 'UTF-8' )
            builder = new MarkupBuilder( writer )
            
            writer.write( "<mt4e-log command='${command}'>\n" )
        }
        
        return builder
    }
    
    void close() {
        if( writer ) {
            builder.yield( "\n</mt4e-log>", false )
            
            writer.close()
            writer = null
        }
    }
}
