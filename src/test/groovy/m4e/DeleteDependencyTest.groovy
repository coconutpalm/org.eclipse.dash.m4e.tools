package m4e;

import static org.junit.Assert.*;
import org.junit.Test;

class DeleteDependencyTest {

    @Test
    public void testDeleteSystemBundle() throws Exception {
        DeleteDependency tool = new DeleteDependency( key: 'system.bundle:system.bundle:[0,)' )
        
        def pom = Pom.load( POM_XML )
        
        tool.apply( pom )
        
        assertEquals( CLEANED_POM, pom.toString() )
        assertEquals( "[]", pom.dependencies?.toString() )
    }
    
    static final String POM_XML = '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>javax.xml</groupId>
  <artifactId>javax.xml</artifactId>
  <version>1.3.4</version>
  <name>JAXP XML</name>
  <dependencies>
    <dependency>
      <groupId>system.bundle</groupId>
      <artifactId>system.bundle</artifactId>
      <version>[0,)</version>
      <optional>false</optional>
    </dependency>
  </dependencies>
</project>
'''
        
    static final String CLEANED_POM = '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>javax.xml</groupId>
  <artifactId>javax.xml</artifactId>
  <version>1.3.4</version>
  <name>JAXP XML</name>
</project>
'''
}
