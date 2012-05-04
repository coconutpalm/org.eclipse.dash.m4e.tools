package m4e.patch;

import static org.junit.Assert.*;
import m4e.MopSetup;
import m4e.Pom;
import m4e.XmlFormatter
import m4e.maven.ImportExportDB
import org.junit.Test;

class ImportDependenciesPatchTest {

    static {
        MopSetup.setup()
    }

    @Test
    public void testGoogleInject() throws Exception {
        
        def db = new ImportExportDB()
        
        def pom = Pom.load( JAVAX_INJECT_POM )
        db.updatePom( pom )
        
        pom = Pom.load( GOOGLE_INJECT_POM )
        db.updatePom( pom )
        
        def patch = new ImportDependenciesPatch( db: db )
        patch.apply( pom )
        
        def formatter = new XmlFormatter( pom: pom )
        formatter.format()

        assertEquals( EXPECTED_GOOGLE_INJECT_POM,
            pom.toString() )
        
    }

    public static final String EXPECTED_GOOGLE_INJECT_POM = '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.google.inject</groupId>
  <artifactId>com.google.inject</artifactId>
  <version>3.0.0.no_aop</version>
  <name>guice</name>
  <description>Guice is a lightweight dependency injection framework for Java 5 and above
    Converted with MT4E 0.13 (27.04.2012)</description>
  <url>http://code.google.com/p/google-guice/</url>
  <properties>
    <mt4e.osgi.importPackage>javax.inject;version="1.0.0"</mt4e.osgi.importPackage>
    <mt4e.osgi.exportPackage>com.google.inject;version="1.3";uses:="com.google.inject,  com.google.inject.binder,  com.google.inject.spi,  com.google.inject.matcher,  javax.inject",com.google.inject.binder;version="1.3";uses:="com.google.inject",com.google.inject.internal;version="1.3";x-internal:="true",com.google.inject.matcher;version="1.3",com.google.inject.name;version="1.3";uses:="com.google.inject",com.google.inject.spi;version="1.3";uses:="com.google.inject.binder,com.google.inject.matcher,com.google.inject",com.google.inject.util;version="1.3";uses:="com.google.inject.util,  com.google.inject.spi,  javax.inject,  com.google.inject"</mt4e.osgi.exportPackage>
  </properties>
  <dependencies>
    <dependency>
      <groupId>javax.inject</groupId>
      <artifactId>javax.inject</artifactId>
      <version>[1.0.0,)</version>
    </dependency>
  </dependencies>
</project>
'''
    
    public static final String GOOGLE_INJECT_POM = '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.google.inject</groupId>
  <artifactId>com.google.inject</artifactId>
  <version>3.0.0.no_aop</version>
  <name>guice</name>
  <description>Guice is a lightweight dependency injection framework for Java 5 and above
    Converted with MT4E 0.13 (27.04.2012)</description>
  <url>http://code.google.com/p/google-guice/</url>
  <properties>
    <mt4e.osgi.importPackage>javax.inject;version="1.0.0"</mt4e.osgi.importPackage>
    <mt4e.osgi.exportPackage>com.google.inject;version="1.3";uses:="com.google.inject,  com.google.inject.binder,  com.google.inject.spi,  com.google.inject.matcher,  javax.inject",com.google.inject.binder;version="1.3";uses:="com.google.inject",com.google.inject.internal;version="1.3";x-internal:="true",com.google.inject.matcher;version="1.3",com.google.inject.name;version="1.3";uses:="com.google.inject",com.google.inject.spi;version="1.3";uses:="com.google.inject.binder,com.google.inject.matcher,com.google.inject",com.google.inject.util;version="1.3";uses:="com.google.inject.util,  com.google.inject.spi,  javax.inject,  com.google.inject"</mt4e.osgi.exportPackage>
  </properties>
</project>
'''
    
    public static final String JAVAX_INJECT_POM = '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>javax.inject</groupId>
  <artifactId>javax.inject</artifactId>
  <version>1.0.0</version>
  <name>Atinject Dependency Injection Annotations</name>
  <description>Converted with MT4E 0.13 (27.04.2012)</description>
  <properties>
    <mt4e.osgi.exportPackage>javax.inject;version="1.0.0"</mt4e.osgi.exportPackage>
  </properties>
</project>
'''

}
