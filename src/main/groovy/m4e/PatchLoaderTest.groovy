package m4e;

import static org.junit.Assert.*;
import org.junit.Test;

class PatchLoaderTest {

    @Test
    public void testLoadEclipse362() throws Exception {
        
        PatchLoader loader = new PatchLoader( new File( 'patches/eclipse-3.6.2.patches' ) )
        
        def patch = loader.load()
        
        assertEquals( 
            '''\
ReplaceDependency( PatchDependency( com.jcraft.jsch:com.jcraft.jsch:0.1.41 ) -> PatchDependency( com.jcraft:jsch:0.1.41 ) )
ReplaceDependency( PatchDependency( com.lowagie.text:com.lowagie.text:2.1.7 ) -> PatchDependency( com.lowagie:itext:2.1.7 ) )
ReplaceDependency( PatchDependency( java_cup.runtime:java_cup.runtime:0.10.0 ) -> PatchDependency( edu.princeton.cup:java-cup:10k ) )
ReplaceDependency( PatchDependency( javax.activation:javax.activation:1.1.0 ) -> PatchDependency( javax.activation:activation:1.1 ) )
ReplaceDependency( PatchDependency( javax.mail:javax.mail:1.4.0 ) -> PatchDependency( javax.mail:mail:1.4 ) )
ReplaceDependency( PatchDependency( javax.persistence:javax.persistence:2.0.1 ) -> PatchDependency( javax.persistence:persistence-api:1.0 ) )
ReplaceDependency( PatchDependency( javax.servlet.jsp:javax.servlet.jsp:2.0.0 ) -> PatchDependency( javax.servlet:jsp-api:2.0 ) )
ReplaceDependency( PatchDependency( javax.servlet:javax.servlet:2.5.0 ) -> PatchDependency( javax.servlet:servlet-api:2.5 ) )
ReplaceDependency( PatchDependency( javax.transaction:javax.transaction:1.1.1 ) -> PatchDependency( javax.transaction:jta:1.1 ) )
ReplaceDependency( PatchDependency( javax.wsdl:javax.wsdl:1.5.1 ) -> PatchDependency( wsdl4j:wsdl4j:1.5.1 ) )
ReplaceDependency( PatchDependency( javax.xml.bind:javax.xml.bind:2.2.0 ) -> PatchDependency( javax.xml.bind:jaxb-api:2.2 ) )
ReplaceDependency( PatchDependency( javax.xml.rpc:javax.xml.rpc:1.1.0 ) -> PatchDependency( javax.xml:jaxrpc-api:1.1 ) )
ReplaceDependency( PatchDependency( javax.xml.soap:javax.xml.soap:1.2.0 ) -> PatchDependency( javax.xml:saaj-api:1.2 ) )
ReplaceDependency( PatchDependency( javax.xml.stream:javax.xml.stream:1.0.1 ) -> PatchDependency( javax.xml:jsr173:1.0 ) )
ReplaceDependency( PatchDependency( javax.xml:javax.xml:1.3.4 ) -> PatchDependency( xml-apis:xml-apis:1.3.04 ) )
ReplaceDependency( PatchDependency( org.apache.ant:org.apache.ant:1.7.1 ) -> PatchDependency( org.apache.ant:ant:1.7.1 ) )
ReplaceDependency( PatchDependency( org.apache.axis:org.apache.axis:1.4.0 ) -> PatchDependency( org.apache.axis:axis:1.4 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.bridge:1.6.0 ) -> PatchDependency( batik:batik-bridge:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.css:1.6.0 ) -> PatchDependency( batik:batik-css:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.dom:1.6.0 ) -> PatchDependency( batik:batik-dom:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.dom.svg:1.6.0 ) -> PatchDependency( batik:batik-svg:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.ext.awt:1.6.0 ) -> PatchDependency( batik:batik-awt-util:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.parser:1.6.0 ) -> PatchDependency( batik:batik-parser:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.pdf:1.6.0 ) -> PatchDependency( batik:batik-rasterizer:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.svggen:1.6.0 ) -> PatchDependency( batik:batik-svggen:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.transcoder:1.6.0 ) -> PatchDependency( batik:batik-transcoder:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.util:1.6.0 ) -> PatchDependency( batik:batik-util:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.util.gui:1.6.0 ) -> PatchDependency( batik:batik-gui-util:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.xml:1.6.0 ) -> PatchDependency( batik:batik-xml:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.bcel:org.apache.bcel:5.2.0 ) -> PatchDependency( org.apache.bcel:bcel:5.2 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.codec:1.3.0 ) -> PatchDependency( commons-codec:commons-codec:1.3 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.collections:3.2.0 ) -> PatchDependency( commons-collections:commons-collections:3.2 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.discovery:0.2.0 ) -> PatchDependency( commons-discovery:commons-discovery:0.2 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.el:1.0.0 ) -> PatchDependency( commons-el:commons-el:1.0 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.httpclient:3.1.0 ) -> PatchDependency( commons-httpclient:commons-httpclient:3.1 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.lang:2.3.0 ) -> PatchDependency( commons-lang:commons-lang:2.3 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.logging:1.1.1 ) -> PatchDependency( commons-logging:commons-logging:1.1.1 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.net:2.0.0 ) -> PatchDependency( commons-net:commons-net:2.0 ) )
ReplaceDependency( PatchDependency( org.apache.derby:org.apache.derby:10.5.1 ) -> PatchDependency( org.apache.derby:derby:10.5.3.0_1 ) )
ReplaceDependency( PatchDependency( org.apache.jasper:org.apache.jasper:5.5.17 ) -> PatchDependency( tomcat:jasper-compiler:5.5.23 ) )
ReplaceDependency( PatchDependency( org.apache.log4j:org.apache.log4j:1.2.15 ) -> PatchDependency( log4j:log4j:1.2.15 ) )
ReplaceDependency( PatchDependency( org.apache.lucene:org.apache.lucene:1.9.1 ) -> PatchDependency( org.apache.lucene:lucene-core:1.9.1 ) )
ReplaceDependency( PatchDependency( org.apache.lucene:org.apache.lucene.analysis:1.9.1 ) -> PatchDependency( org.apache.lucene:lucene-analyzers:1.9.1 ) )
ReplaceDependency( PatchDependency( org.apache.oro:org.apache.oro:2.0.8 ) -> PatchDependency( oro:oro:2.0.8 ) )
ReplaceDependency( PatchDependency( org.apache.velocity:org.apache.velocity:1.5.0 ) -> PatchDependency( velocity:velocity:1.5 ) )
ReplaceDependency( PatchDependency( org.apache.xalan:org.apache.xalan:2.7.1 ) -> PatchDependency( xalan:xalan:2.7.1 ) )
ReplaceDependency( PatchDependency( org.apache.xerces:org.apache.xerces:2.9.0 ) -> PatchDependency( xerces:xercesImpl:2.9.0 ) )
ReplaceDependency( PatchDependency( org.apache.xml:org.apache.xml.resolver:1.2.0 ) -> PatchDependency( xml-resolver:xml-resolver:1.2 ) )
ReplaceDependency( PatchDependency( org.apache.xml:org.apache.xml.serializer:2.7.1 ) -> PatchDependency( xalan:serializer:2.7.1 ) )
ReplaceDependency( PatchDependency( org.apache.xmlrpc:org.apache.xmlrpc:3.0.0 ) -> PatchDependency( xmlrpc:xmlrpc:3.0 ) )
ReplaceDependency( PatchDependency( org.h2:org.h2:1.1.117 ) -> PatchDependency( com.h2database:h2database:1.1.117 ) )
ReplaceDependency( PatchDependency( org.hamcrest.core:org.hamcrest.core:1.1.0 ) -> PatchDependency( org.hamcrest:hamcrest-core:1.1 ) )
ReplaceDependency( PatchDependency( org.jdom:org.jdom:1.0.0 ) -> PatchDependency( jdom:jdom:1.0 ) )
ReplaceDependency( PatchDependency( org.junit4:org.junit4:4.8.1 ) -> PatchDependency( junit:junit:4.8.1 ) )
ReplaceDependency( PatchDependency( org.junit:org.junit:3.8.2 ) -> PatchDependency( junit:junit:3.8.2 ) )
ReplaceDependency( PatchDependency( org.mortbay.jetty:org.mortbay.jetty.server:6.1.23 ) -> PatchDependency( org.mortbay.jetty:jetty:6.1.23 ) )
ReplaceDependency( PatchDependency( org.mortbay.jetty:org.mortbay.jetty.util:6.1.23 ) -> PatchDependency( org.mortbay.jetty:jetty-util:6.1.23 ) )
ReplaceDependency( PatchDependency( org.mozilla.javascript:org.mozilla.javascript:[1.6.0,2.0.0) ) -> PatchDependency( rhino:js:1.7R2 ) )
ReplaceDependency( PatchDependency( org.objectweb.asm:org.objectweb.asm:3.2.0 ) -> PatchDependency( asm:asm:3.2 ) )
ReplaceDependency( PatchDependency( org.sat4j.core:org.sat4j.core:2.2.0 ) -> PatchDependency( org.sat4j:org.sat4j.core:2.2.3 ) )
ReplaceDependency( PatchDependency( org.sat4j.pb:org.sat4j.pb:2.2.0 ) -> PatchDependency( org.sat4j:org.sat4j.pb:2.2.3 ) )
ReplaceDependency( PatchDependency( org.w3c.css:org.w3c.css.sac:1.3.0 ) -> PatchDependency( org.w3c.css:sac:1.3 ) )
ReplaceDependency( PatchDependency( org.w3c.sac:org.w3c.sac:1.3.0 ) -> PatchDependency( org.w3c.css:sac:1.3 ) )
DeleteDependency( system.bundle:system.bundle:[0,) )''', patch?.patches?.join( '\n' ) )
    }

    @Test
    public void testLoadEclipse370() throws Exception {
        
        PatchLoader loader = new PatchLoader( new File( 'patches/eclipse-3.7.0.patches' ) )
        
        def patch = loader.load()
        
        assertEquals( 
            '''\
ReplaceDependency( PatchDependency( ch.qos.logback:ch.qos.logback.classic:0.9.27 ) -> PatchDependency( ch.qos.logback:logback-classic:0.9.27 ) )
ReplaceDependency( PatchDependency( ch.qos.logback:ch.qos.logback.core:0.9.27 ) -> PatchDependency( ch.qos.logback:logback-core:0.9.27 ) )
ReplaceDependency( PatchDependency( ch.qos.logback:ch.qos.logback.slf4j:0.9.27 ) -> PatchDependency( ch.qos.logback:logback-classic:0.9.27 ) )
ReplaceDependency( PatchDependency( com.google.collect:com.google.collect:1.0.0 ) -> PatchDependency( com.google.collections:google-collections:1.0 ) )
ReplaceDependency( PatchDependency( com.google.inject:com.google.inject:2.0.0 ) -> PatchDependency( com.google.inject:guice:2.0 ) )
ReplaceDependency( PatchDependency( com.jcraft.jsch:com.jcraft.jsch:0.1.41 ) -> PatchDependency( com.jcraft:jsch:0.1.41 ) )
ReplaceDependency( PatchDependency( com.lowagie.text:com.lowagie.text:2.1.7 ) -> PatchDependency( com.lowagie:itext:2.1.7 ) )
ReplaceDependency( PatchDependency( com.sun.jna:com.sun.jna:3.2.7 ) -> PatchDependency( com.sun.jna:jna:3.0.9 ) )
ReplaceDependency( PatchDependency( java_cup.runtime:java_cup.runtime:0.10.0 ) -> PatchDependency( edu.princeton.cup:java-cup:10k ) )
ReplaceDependency( PatchDependency( javax.activation:javax.activation:1.1.0 ) -> PatchDependency( javax.activation:activation:1.1 ) )
ReplaceDependency( PatchDependency( javax.mail:javax.mail:1.4.0 ) -> PatchDependency( javax.mail:mail:1.4 ) )
ReplaceDependency( PatchDependency( javax.persistence:javax.persistence:2.0.1 ) -> PatchDependency( javax.persistence:persistence-api:1.0 ) )
ReplaceDependency( PatchDependency( javax.servlet.jsp:javax.servlet.jsp:2.0.0 ) -> PatchDependency( javax.servlet:jsp-api:2.0 ) )
ReplaceDependency( PatchDependency( javax.servlet:javax.servlet:2.5.0 ) -> PatchDependency( javax.servlet:servlet-api:2.5 ) )
ReplaceDependency( PatchDependency( javax.transaction:javax.transaction:1.1.1 ) -> PatchDependency( javax.transaction:jta:1.1 ) )
ReplaceDependency( PatchDependency( javax.wsdl:javax.wsdl:1.5.1 ) -> PatchDependency( wsdl4j:wsdl4j:1.5.1 ) )
ReplaceDependency( PatchDependency( javax.xml.bind:javax.xml.bind:2.1.9 ) -> PatchDependency( javax.xml.bind:jaxb-api:2.1 ) )
ReplaceDependency( PatchDependency( javax.xml.bind:javax.xml.bind:2.2.0 ) -> PatchDependency( javax.xml.bind:jaxb-api:2.2.4 ) )
ReplaceDependency( PatchDependency( javax.xml.rpc:javax.xml.rpc:1.1.0 ) -> PatchDependency( javax.xml:jaxrpc-api:1.1 ) )
ReplaceDependency( PatchDependency( javax.xml.soap:javax.xml.soap:1.2.0 ) -> PatchDependency( javax.xml:saaj-api:1.2 ) )
ReplaceDependency( PatchDependency( javax.xml.stream:javax.xml.stream:1.0.1 ) -> PatchDependency( javax.xml:jsr173:1.0 ) )
ReplaceDependency( PatchDependency( javax.xml:javax.xml:1.3.4 ) -> PatchDependency( xml-apis:xml-apis:1.3.04 ) )
ReplaceDependency( PatchDependency( org.apache.ant:org.apache.ant:1.7.1 ) -> PatchDependency( org.apache.ant:ant:1.7.1 ) )
ReplaceDependency( PatchDependency( org.apache.axis:org.apache.axis:1.4.0 ) -> PatchDependency( org.apache.axis:axis:1.4 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.bridge:1.6.0 ) -> PatchDependency( batik:batik-bridge:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.css:1.6.0 ) -> PatchDependency( batik:batik-css:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.dom:1.6.0 ) -> PatchDependency( batik:batik-dom:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.dom.svg:1.6.0 ) -> PatchDependency( batik:batik-svg:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.ext.awt:1.6.0 ) -> PatchDependency( batik:batik-awt-util:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.parser:1.6.0 ) -> PatchDependency( batik:batik-parser:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.pdf:1.6.0 ) -> PatchDependency( batik:batik-rasterizer:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.svggen:1.6.0 ) -> PatchDependency( batik:batik-svggen:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.transcoder:1.6.0 ) -> PatchDependency( batik:batik-transcoder:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.util:1.6.0 ) -> PatchDependency( batik:batik-util:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.util.gui:1.6.0 ) -> PatchDependency( batik:batik-gui-util:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.batik:org.apache.batik.xml:1.6.0 ) -> PatchDependency( batik:batik-xml:1.6 ) )
ReplaceDependency( PatchDependency( org.apache.bcel:org.apache.bcel:5.2.0 ) -> PatchDependency( org.apache.bcel:bcel:5.2 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.codec:1.3.0 ) -> PatchDependency( commons-codec:commons-codec:1.3 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.collections:3.2.0 ) -> PatchDependency( commons-collections:commons-collections:3.2 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.discovery:0.2.0 ) -> PatchDependency( commons-discovery:commons-discovery:0.2 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.el:1.0.0 ) -> PatchDependency( commons-el:commons-el:1.0 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.httpclient:3.1.0 ) -> PatchDependency( commons-httpclient:commons-httpclient:3.1 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.lang:2.3.0 ) -> PatchDependency( commons-lang:commons-lang:2.3 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.logging:1.1.1 ) -> PatchDependency( commons-logging:commons-logging:1.1.1 ) )
ReplaceDependency( PatchDependency( org.apache.commons:org.apache.commons.net:2.0.0 ) -> PatchDependency( commons-net:commons-net:2.0 ) )
ReplaceDependency( PatchDependency( org.apache.derby:org.apache.derby:10.5.1 ) -> PatchDependency( org.apache.derby:derby:10.5.3.0_1 ) )
ReplaceDependency( PatchDependency( org.apache.jasper:org.apache.jasper:5.5.17 ) -> PatchDependency( tomcat:jasper-compiler:5.5.23 ) )
ReplaceDependency( PatchDependency( org.apache.log4j:org.apache.log4j:1.2.15 ) -> PatchDependency( log4j:log4j:1.2.15 ) )
ReplaceDependency( PatchDependency( org.apache.lucene:org.apache.lucene:1.9.1 ) -> PatchDependency( org.apache.lucene:lucene-core:1.9.1 ) )
ReplaceDependency( PatchDependency( org.apache.lucene:org.apache.lucene.analysis:1.9.1 ) -> PatchDependency( org.apache.lucene:lucene-analyzers:1.9.1 ) )
ReplaceDependency( PatchDependency( org.apache.oro:org.apache.oro:2.0.8 ) -> PatchDependency( oro:oro:2.0.8 ) )
ReplaceDependency( PatchDependency( org.apache.velocity:org.apache.velocity:1.5.0 ) -> PatchDependency( velocity:velocity:1.5 ) )
ReplaceDependency( PatchDependency( org.apache.xalan:org.apache.xalan:2.7.1 ) -> PatchDependency( xalan:xalan:2.7.1 ) )
ReplaceDependency( PatchDependency( org.apache.xerces:org.apache.xerces:2.9.0 ) -> PatchDependency( xerces:xercesImpl:2.9.0 ) )
ReplaceDependency( PatchDependency( org.apache.xml:org.apache.xml.resolver:1.2.0 ) -> PatchDependency( xml-resolver:xml-resolver:1.2 ) )
ReplaceDependency( PatchDependency( org.apache.xml:org.apache.xml.serializer:2.7.1 ) -> PatchDependency( xalan:serializer:2.7.1 ) )
ReplaceDependency( PatchDependency( org.apache.xmlrpc:org.apache.xmlrpc:3.0.0 ) -> PatchDependency( xmlrpc:xmlrpc:3.0 ) )
ReplaceDependency( PatchDependency( org.h2:org.h2:1.1.117 ) -> PatchDependency( com.h2database:h2database:1.1.117 ) )
ReplaceDependency( PatchDependency( org.hamcrest.core:org.hamcrest.core:1.1.0 ) -> PatchDependency( org.hamcrest:hamcrest-core:1.1 ) )
ReplaceDependency( PatchDependency( org.jdom:org.jdom:1.0.0 ) -> PatchDependency( jdom:jdom:1.0 ) )
ReplaceDependency( PatchDependency( org.junit4:org.junit4:4.8.1 ) -> PatchDependency( junit:junit:4.8.1 ) )
ReplaceDependency( PatchDependency( org.junit:org.junit:3.8.2 ) -> PatchDependency( junit:junit:3.8.2 ) )
ReplaceDependency( PatchDependency( org.mortbay.jetty:org.mortbay.jetty.server:6.1.23 ) -> PatchDependency( org.mortbay.jetty:jetty:6.1.23 ) )
ReplaceDependency( PatchDependency( org.mortbay.jetty:org.mortbay.jetty.util:6.1.23 ) -> PatchDependency( org.mortbay.jetty:jetty-util:6.1.23 ) )
ReplaceDependency( PatchDependency( org.mozilla.javascript:org.mozilla.javascript:[1.6.0,2.0.0) ) -> PatchDependency( rhino:js:1.7R2 ) )
ReplaceDependency( PatchDependency( org.objectweb.asm:org.objectweb.asm:3.2.0 ) -> PatchDependency( asm:asm:3.2 ) )
ReplaceDependency( PatchDependency( org.sat4j.core:org.sat4j.core:2.2.0 ) -> PatchDependency( org.sat4j:org.sat4j.core:2.2.3 ) )
ReplaceDependency( PatchDependency( org.sat4j.pb:org.sat4j.pb:2.2.0 ) -> PatchDependency( org.sat4j:org.sat4j.pb:2.2.3 ) )
ReplaceDependency( PatchDependency( org.w3c.css:org.w3c.css.sac:1.3.0 ) -> PatchDependency( org.w3c.css:sac:1.3 ) )
ReplaceDependency( PatchDependency( org.w3c.sac:org.w3c.sac:1.3.0 ) -> PatchDependency( org.w3c.css:sac:1.3 ) )
DeleteDependency( system.bundle:system.bundle:[0,) )''', patch?.patches?.join( '\n' ) )
    }
    
    @Test
    public void testDuplicateReplacements() throws Exception {
        PatchLoader loader = new PatchLoader( '''\
replace( 'a:b:1', 'x:y:1' )
replace( 'a:b:1', 'x:y:2' )
''' )
        
        try {
            loader.load()
            fail( 'Expected exception' )
        } catch( UserError e ) {
            assertEquals( "Found duplicate replace a:b:1 in patch '{set.source}'".toString(), e.message )
        }
    }
}