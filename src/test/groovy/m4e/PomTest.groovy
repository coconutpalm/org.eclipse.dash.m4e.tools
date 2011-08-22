package m4e;

import static org.junit.Assert.*;
import org.junit.Test;

class PomTest {

    @Test
    public void testLoad() throws Exception {
        def pom = Pom.load( POM_XML )
        
        assertEquals( "[Dependency( org.slf4j:slf4j-api:1.6.2 )]", pom.dependencies?.toString() )
    }
    
    static final String POM_XML = '''\
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>

        <dependencies>
                <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                        <version>1.6.2</version>
                        <optional>false</optional>
                </dependency>
        </dependencies>
</project>
'''
}
