package m4e.p2;

import static org.junit.Assert.*;
import m4e.CommonTestCode;
import m4e.MopSetup;

import org.junit.Test;

class M2ETest {
    
    static {
        MopSetup.setup()
    }

    @Test
    public void testM2E() throws Exception {
        def url = new URL( 'http://download.eclipse.org/technology/m2e/releases/1.0/1.0.200.20111228-1245' )
        def loader = new P2RepoLoader( workDir: CommonTestCode.newFile( "m2e" ), url: url )
        def repo = loader.load()
        
        def bundle = repo.latest( 'org.slf4j.api' )
        assertEquals( 'P2Plugin( id=org.slf4j.api, version=1.6.1.v20100831-0715, name=SLF4J API )', bundle.toString() )
        assertEquals( 
            '''\
P2Dependency( id=ch.qos.logback.slf4j, version=[0.9.27.v20110224-1110,0.9.27.v20110224-1110], type=osgi.bundle )
P2Dependency( id=org.slf4j.api, version=1.6.1.v20100831-0715, type=osgi.bundle )'''
            , bundle.dependencies.join( '\n' ) )
        
        bundle = repo.latest( 'ch.qos.logback.slf4j' )
        assertEquals( 'P2Plugin( id=ch.qos.logback.slf4j, version=0.9.27.v20110224-1110, name=Logback Native SLF4J Logger Module )', bundle.toString() )
        
        def deps = new DependencySet( repo: repo )
        deps.resolveDependencies( "org.eclipse.m2e.feature.feature.group" )
        deps.resolveDependencies( "org.eclipse.m2e.logback.feature.feature.group" )
        
        def unknownIds = deps.unknownIds as ArrayList
        unknownIds.sort()
        assertEquals( '''\
com.ibm.icu
groovy.lang
javax.crypto
javax.crypto.spec
javax.jms
javax.mail
javax.mail.internet
javax.management
javax.naming
javax.net
javax.net.ssl
javax.servlet
javax.servlet.http
javax.sql
javax.xml.parsers
org.codehaus.groovy.control
org.codehaus.groovy.reflection
org.codehaus.groovy.runtime
org.codehaus.groovy.runtime.callsite
org.codehaus.groovy.runtime.typehandling
org.codehaus.janino
org.eclipse.compare
org.eclipse.compare.rangedifferencer
org.eclipse.core.expressions
org.eclipse.core.filebuffers
org.eclipse.core.filesystem
org.eclipse.core.jobs
org.eclipse.core.resources
org.eclipse.core.runtime
org.eclipse.core.variables
org.eclipse.debug.core
org.eclipse.debug.ui
org.eclipse.emf.ecore
org.eclipse.emf.ecore.edit
org.eclipse.emf.ecore.xmi
org.eclipse.emf.edit
org.eclipse.emf.edit.ui
org.eclipse.epp.usagedata.gathering
org.eclipse.equinox.common
org.eclipse.equinox.internal.p2.discovery.compatibility
org.eclipse.equinox.internal.p2.ui
org.eclipse.equinox.internal.p2.ui.dialogs
org.eclipse.equinox.internal.p2.ui.model
org.eclipse.equinox.internal.p2.ui.viewers
org.eclipse.equinox.internal.provisional.configurator
org.eclipse.equinox.p2.core
org.eclipse.equinox.p2.discovery
org.eclipse.equinox.p2.discovery.compatibility
org.eclipse.equinox.p2.engine
org.eclipse.equinox.p2.metadata
org.eclipse.equinox.p2.operations
org.eclipse.equinox.p2.repository
org.eclipse.equinox.p2.repository.metadata
org.eclipse.equinox.p2.ui
org.eclipse.equinox.p2.ui.discovery
org.eclipse.equinox.registry
org.eclipse.jdt.core
org.eclipse.jdt.debug.ui
org.eclipse.jdt.feature.group
org.eclipse.jdt.junit
org.eclipse.jdt.launching
org.eclipse.jdt.ui
org.eclipse.jem.util
org.eclipse.jface
org.eclipse.jface.text
org.eclipse.ltk.core.refactoring
org.eclipse.ltk.core.refactoring.resource
org.eclipse.ltk.ui.refactoring
org.eclipse.osgi
org.eclipse.platform.feature.group
org.eclipse.rcp.feature.group
org.eclipse.search
org.eclipse.search.ui.text
org.eclipse.swt
org.eclipse.ui
org.eclipse.ui.console
org.eclipse.ui.editors
org.eclipse.ui.externaltools
org.eclipse.ui.forms
org.eclipse.ui.forms.editor
org.eclipse.ui.ide
org.eclipse.ui.workbench
org.eclipse.ui.workbench.texteditor
org.eclipse.wst.common.emf
org.eclipse.wst.common.uriresolver
org.eclipse.wst.sse.core
org.eclipse.wst.sse.ui
org.eclipse.wst.xml.core
org.eclipse.wst.xml.ui
org.eclipse.wst.xsd.core
org.ietf.jgss
org.maven.ide.eclipse
org.osgi.framework
org.osgi.service.log
org.osgi.util.tracker
org.w3c.dom
org.xml.sax
org.xml.sax.helpers
sun.reflect''', unknownIds.join( '\n' ) )
        
        assertEquals(
            '''\
P2Feature( id=org.eclipse.m2e.feature.feature.group, version=1.0.200.20111228-1245, name=m2e - Maven Integration for Eclipse )
P2Plugin( id=org.eclipse.m2e.maven.indexer, version=1.0.200.20111228-1245, name=Maven / Nexus Indexer Bundle )
P2Plugin( id=org.eclipse.m2e.maven.runtime, version=1.0.200.20111228-1245, name=Embedded Maven Runtime Bundle )
P2Plugin( id=com.ning.async-http-client, version=1.6.3.201112281337, name=async-http-client )
P2Plugin( id=org.jboss.netty, version=3.2.4.Final-201112281337, name=The Netty Project )
P2Plugin( id=org.slf4j.api, version=1.6.1.v20100831-0715, name=SLF4J API )
P2Plugin( id=ch.qos.logback.slf4j, version=0.9.27.v20110224-1110, name=Logback Native SLF4J Logger Module )
P2Plugin( id=ch.qos.logback.classic, version=0.9.27.v20110224-1110, name=Logback Classic Module )
P2Plugin( id=ch.qos.logback.core, version=0.9.27.v20110224-1110, name=Logback Core Module )
P2Plugin( id=org.eclipse.m2e.archetype.common, version=1.0.200.20111228-1245, name=Maven Archetype Common Bundle )
P2Plugin( id=org.eclipse.m2e.editor, version=1.0.200.20111228-1245, name=Maven Integration for Eclipse (Editors) )
P2Plugin( id=org.eclipse.m2e.core, version=1.0.200.20111228-1245, name=Maven Integration for Eclipse )
P2Plugin( id=org.eclipse.m2e.editor.xml, version=1.0.200.20111228-1245, name=Maven POM XML Editor )
P2Plugin( id=org.eclipse.m2e.core.ui, version=1.0.200.20111228-1245, name=Maven Integration for Eclipse )
P2Plugin( id=org.eclipse.m2e.model.edit, version=1.0.200.20111228-1245, name=Maven Project Model Edit Bundle )
P2Plugin( id=org.eclipse.m2e.jdt, version=1.0.200.20111228-1245, name=Maven Integration for Eclipse JDT )
P2Plugin( id=org.eclipse.m2e.lifecyclemapping.defaults, version=1.0.200.20111228-1245, name=Default Build Lifecycle Mapping Metadata )
P2Plugin( id=org.eclipse.m2e.scm, version=1.0.200.20111228-1245, name=SCM Maven Integration for Eclipse )
P2Plugin( id=org.eclipse.m2e.discovery, version=1.0.200.20111228-1245, name=m2e Marketplace )
P2Plugin( id=org.eclipse.m2e.usagedata, version=1.0.200.20111228-1245, name=m2e / UDC integration Marketplace )
P2Plugin( id=org.eclipse.m2e.refactoring, version=1.0.200.20111228-1245, name=Maven Integration for Eclipse Refactoring )
P2Plugin( id=org.eclipse.m2e.launching, version=1.0.200.20111228-1245, name=Maven Integration for Eclipse Launching )
P2Feature( id=org.eclipse.m2e.logback.feature.feature.group, version=1.0.200.20111228-1245, name=m2e - slf4j over logback logging (Optional) )
P2Plugin( id=org.eclipse.m2e.logback.configuration, version=1.0.200.20111228-1245, name=m2e logback configuration )
P2Plugin( id=org.eclipse.m2e.logback.appender, version=1.0.200.20111228-1245, name=m2e logback appender )'''
            , deps.bundles.join( '\n' ) )
        
        deps.download( CommonTestCode.newFile( 'org.eclipse.m2e' ) )
    }
    
}
