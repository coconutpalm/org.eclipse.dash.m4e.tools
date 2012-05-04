/*******************************************************************************
 * Copyright (c) 04.05.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e;

import static org.junit.Assert.*;
import m4e.patch.ImportDependenciesPatchTest;
import org.junit.Test;

class PatchCmdTest {

    @Test
    public void testGoogleInject() throws Exception {
        
        def tool = new PatchCmd()
        tool.init()
        
        tool.loadPatches( 'data/input/googleInject.patch' )
        
        def pom = Pom.load( ImportDependenciesPatchTest.EXPECTED_GOOGLE_INJECT_POM )
        tool.patchPom( pom )
        
        XmlFormatter.format( pom )
        
        assertEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.eclipse.orbit</groupId>
  <artifactId>orbit.com.google.inject</artifactId>
  <version>3.0.0</version>
  <name>guice supplied by Eclipse Orbit</name>
  <description>Guice is a lightweight dependency injection framework for Java 5 and above
    Converted with MT4E 0.13 (27.04.2012)</description>
  <url>http://code.google.com/p/google-guice/</url>
  <properties>
    <mt4e.osgi.importPackage>javax.inject;version="1.0.0"</mt4e.osgi.importPackage>
    <mt4e.osgi.exportPackage>com.google.inject;version="1.3";uses:="com.google.inject,  com.google.inject.binder,  com.google.inject.spi,  com.google.inject.matcher,  javax.inject",com.google.inject.binder;version="1.3";uses:="com.google.inject",com.google.inject.internal;version="1.3";x-internal:="true",com.google.inject.matcher;version="1.3",com.google.inject.name;version="1.3";uses:="com.google.inject",com.google.inject.spi;version="1.3";uses:="com.google.inject.binder,com.google.inject.matcher,com.google.inject",com.google.inject.util;version="1.3";uses:="com.google.inject.util,  com.google.inject.spi,  javax.inject,  com.google.inject"</mt4e.osgi.exportPackage>
  </properties>
  <dependencies>
  </dependencies>
  <profiles>
    <profile>
      <id>m4e.orbit</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <dependencies>
        <dependency>
          <groupId>org.eclipse.orbit</groupId>
          <artifactId>orbit.javax.inject</artifactId>
          <version>[1.0.0,)</version>
        </dependency>
      </dependencies>
    </profile>
    <profile>
      <id>m4e.maven-central</id>
      <dependencies>
        <dependency>
          <groupId>javax.inject</groupId>
          <artifactId>javax.inject</artifactId>
          <version>1</version>
        </dependency>
      </dependencies>
    </profile>
  </profiles>
</project>
''',
            pom.toString() )
    }
    
    @Test
    public void testBatikPDFExports() throws Exception {
        
        File root = CommonTestCode.prepareRepo( new File( 'data/input/batikPDFExports' ), 'testBatikPDFExports' )
        File repo = new File( root, 'm2repo' )

        def tool = new PatchCmd( target: repo )
        tool.run( 'patch', repo.absolutePath, 'data/input/batikPDFExports.patch' )
        
        tool.importExportDB.file = new File( root, 'importExportDB' )
        tool.importExportDB.save()
        
        def actual = tool.importExportDB.file.getText( 'UTF-8' )
        assertEquals( '''\
P org.apache.batik:org.apache.batik.pdf:1.6.0.v201105071520
E org.apache.avalon.framework
E org.apache.avalon.framework.activity
E org.apache.avalon.framework.configuration
E org.apache.avalon.framework.container
E org.apache.fop.apps
E org.apache.fop.fo
E org.apache.fop.fonts
E org.apache.fop.fonts.apps
E org.apache.fop.fonts.base14
E org.apache.fop.fonts.truetype
E org.apache.fop.fonts.type1
E org.apache.fop.image
E org.apache.fop.image.analyser
E org.apache.fop.pdf
E org.apache.fop.render.pdf
E org.apache.fop.render.ps
E org.apache.fop.svg
E org.apache.fop.util
I org.apache.batik.bridge
I org.apache.batik.dom.svg
I org.apache.batik.dom.util
I org.apache.batik.ext.awt
I org.apache.batik.ext.awt.g2d
I org.apache.batik.gvt
I org.apache.batik.gvt.renderer
I org.apache.batik.gvt.text
I org.apache.batik.transcoder
I org.apache.batik.transcoder.image
I org.apache.batik.transcoder.keys
I org.apache.batik.util
I org.w3c.dom
P org.apache.batik:org.apache.batik.transcoder:1.6.0.v201011041432
E org.apache.batik.transcoder
E org.apache.batik.transcoder.image
E org.apache.batik.transcoder.image.resources
E org.apache.batik.transcoder.keys
E org.apache.batik.transcoder.print
E org.apache.batik.transcoder.svg2svg
E org.apache.batik.transcoder.wmf
E org.apache.batik.transcoder.wmf.tosvg
P org.apache.batik:org.apache.batik.util:1.6.0.v201011041432
E org.apache.batik
E org.apache.batik.i18n
E org.apache.batik.util
E org.apache.batik.util.io
E org.apache.batik.util.resources
''',
            actual)

        assertEquals( '[org.apache.commons.io, org.apache.commons.io.output, org.apache.commons.logging, org.apache.commons.logging.impl]', Arrays.toString( tool.importExportDB.infos[ 'org.apache.batik:org.apache.batik.pdf:1.6.0.v201105071520' ]?.deletions ) )
        
        def pom = Pom.load( new File( repo, 'org/eclipse/orbit/orbit.org.apache.batik.pdf/1.6.0/orbit.org.apache.batik.pdf-1.6.0.pom' ) )        
        
        assertEquals( '''\
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.eclipse.orbit</groupId>
  <artifactId>orbit.org.apache.batik.pdf</artifactId>
  <version>1.6.0</version>
  <name>Apache Batik PDF supplied by Eclipse Orbit</name>
  <description>Converted with MT4E 0.13 (27.04.2012)</description>
  <properties>
    <mt4e.osgi.importPackage>org.apache.batik.bridge;version="[1.6.0,1.7.0)",org.apache.batik.dom.svg;version="[1.6.0,1.7.0)",org.apache.batik.dom.util;version="[1.6.0,1.7.0)",org.apache.batik.ext.awt;version="[1.6.0,1.7.0)",org.apache.batik.ext.awt.g2d;version="[1.6.0,1.7.0)",org.apache.batik.gvt;version="[1.6.0,1.7.0)",org.apache.batik.gvt.renderer;version="[1.6.0,1.7.0)",org.apache.batik.gvt.text;version="[1.6.0,1.7.0)",org.apache.batik.transcoder;version="[1.6.0,1.7.0)",org.apache.batik.transcoder.image;version="[1.6.0,1.7.0)",org.apache.batik.transcoder.keys;version="[1.6.0,1.7.0)",org.apache.batik.util;version="[1.6.0,1.7.0)",org.w3c.dom</mt4e.osgi.importPackage>
    <mt4e.osgi.exportPackage>org.apache.avalon.framework,org.apache.avalon.framework.activity,org.apache.avalon.framework.configuration,org.apache.avalon.framework.container,org.apache.fop.apps,org.apache.fop.fo,org.apache.fop.fonts,org.apache.fop.fonts.apps,org.apache.fop.fonts.base14,org.apache.fop.fonts.truetype,org.apache.fop.fonts.type1,org.apache.fop.image,org.apache.fop.image.analyser,org.apache.fop.pdf,org.apache.fop.render.pdf,org.apache.fop.render.ps,org.apache.fop.svg,org.apache.fop.util</mt4e.osgi.exportPackage>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.eclipse.orbit</groupId>
      <artifactId>orbit.org.apache.batik.transcoder</artifactId>
      <version>[1.6.0,)</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.orbit</groupId>
      <artifactId>orbit.org.apache.batik.util</artifactId>
      <version>[1.6.0,)</version>
    </dependency>
  </dependencies>
</project>
''',
            pom.toString() )
    }
}
