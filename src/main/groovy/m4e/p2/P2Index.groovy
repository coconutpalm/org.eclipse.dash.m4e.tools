package m4e.p2

import java.io.File;


class P2Index {

    String contentJarName
    String contentXmlName
    
    P2Index( File file ) {
        
        Properties properties = new Properties();
        file.withInputStream {
            properties.load( it )
        }
        
        def value = properties.getProperty( 'metadata.repository.factory.order' )
        value = value.trim().removeEnd( ',!' )
        
        contentXmlName = value
        contentJarName = value.removeEnd( '.xml' ) + '.jar'
    }
}
