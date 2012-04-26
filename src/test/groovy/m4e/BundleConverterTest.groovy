package m4e;

import static org.junit.Assert.*;
import java.util.jar.Manifest;
import org.junit.Test;

class BundleConverterTest {

    @Test
    public void testConvert() throws Exception {
        def m = new Manifest( new ByteArrayInputStream( MANIFEST.getBytes( 'UTF-8' ) ) )
        
        def statistics = new ImportStatistics()
        
        def tool = new BundleConverter( manifest: m, statistics: statistics )
        tool.examineManifest()
        
        def buffer = new StringWriter()
        tool.createPom( buffer )
        
        assertEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.pany.eclipse</groupId>
  <artifactId>com.pany.eclipse.bundle</artifactId>
  <version>2.2.0.v20120426_155010</version>
  <name>Bundle</name>
  <dependencies>
    <dependency>
      <groupId>org.eclipse.xtext</groupId>
      <artifactId>org.eclipse.xtext</artifactId>
      <version>2.2.1</version>
    </dependency>
    <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>org.apache.commons.logging</artifactId>
      <version>1.1.1</version>
      <optional>true</optional>
    </dependency>
    <dependency>
      <groupId>org.eclipse.emf</groupId>
      <artifactId>org.eclipse.emf.mwe2.launch</artifactId>
      <version>[0,)</version>
      <optional>true</optional>
    </dependency>
    <dependency>
      <groupId>org.eclipse.xtext</groupId>
      <artifactId>org.eclipse.xtext.util</artifactId>
      <version>2.2.1</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.core</groupId>
      <artifactId>org.eclipse.core.resources</artifactId>
      <version>[3.7.101,4.0)</version>
    </dependency>
  </dependencies>
</project>
'''
            , buffer.toString() )
    }
    
    private final static String MANIFEST = '''\
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: Bundle
Bundle-Vendor: My Company
Bundle-Version: 2.2.0.v20120426_155010
Bundle-SymbolicName: com.pany.eclipse.bundle; singleton:=true
Bundle-ActivationPolicy: lazy
Require-Bundle: org.eclipse.xtext;bundle-version="2.2.1";visibility:=reexport,
 org.apache.commons.logging;bundle-version="1.1.1";resolution:=optional;visibility:=reexport,
 org.eclipse.emf.mwe2.launch;resolution:=optional,
 org.eclipse.xtext.util;bundle-version="2.2.1",
 org.eclipse.core.resources;bundle-version="[3.7.101,4.0)"
Import-Package: org.apache.commons.logging,
 org.apache.log4j,
 org.eclipse.xtend2.lib,
 org.eclipse.xtext.xtend2.lib
Bundle-RequiredExecutionEnvironment: JavaSE-1.6
Export-Package: com.pany.eclipse.bundle,
 com.pany.eclipse.bundle.x,
 com.pany.eclipse.bundle.y
'''
    
}
