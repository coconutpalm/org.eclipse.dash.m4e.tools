package m4e.p2;

import static org.junit.Assert.*;
import m4e.CommonTestCode;
import m4e.MopSetup;

import org.junit.Test;

class XtextTest {
    
    static {
        MopSetup.setup()
    }

    @Test
    public void testXtext() throws Exception {
        def url = new URL( 'http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/' )
        def loader = new P2RepoLoader( workDir: CommonTestCode.newFile( "xtext" ), url: url )
        def repo = loader.load()
        
        def bundle = repo.find( 'org.eclipse.xtext', new Version( '2.2.1.v201112130541' ) )
        assertEquals( 'P2Plugin( id=org.eclipse.xtext, version=2.2.1.v201112130541, name=Xtext  )', bundle.toString() )
        
        bundle = repo.latest( 'org.eclipse.xtext.runtime.feature.group' )
        assertEquals( 'P2Feature( id=org.eclipse.xtext.runtime.feature.group, version=2.1.1.v201111141332, name=Xtext Runtime  )', bundle.toString() )
        
        bundle = repo.latest( 'org.eclipse.xtext.runtime.source.feature.group' )
        assertEquals( 'P2Feature( id=org.eclipse.xtext.runtime.source.feature.group, version=2.1.1.v201111141332, name=Source for Xtext Runtime  )', bundle.toString() )
        
        def deps = new DependencySet( repo: repo )
        deps.resolveDependencies( 'org.eclipse.xtext.runtime.source.feature.group' )
        
        def unknownIds = deps.unknownIds as ArrayList
        unknownIds.sort()
        assertEquals( ''
            , unknownIds.join( '\n' ) )

        assertEquals(
            '''\
P2Feature( id=org.eclipse.xtext.runtime.source.feature.group, version=2.1.1.v201111141332, name=Source for Xtext Runtime  )
P2Plugin( id=com.google.collect.source, version=1.0.0.v201105210816, name=Google Collections )
P2Plugin( id=com.google.inject.source, version=2.0.0.v201105231817, name=Google Guice Source )
P2Plugin( id=org.antlr.runtime.source, version=3.2.0.v201101311130, name=ANTLR Runtime Source )
P2Plugin( id=org.apache.log4j.source, version=1.2.15.v201012070815, name=Apache Jakarta log4j Source Bundle )
P2Plugin( id=org.eclipse.xtext.logging.source, version=1.2.15.v201111141332, name=org.eclipse.xtext.logging Source )
P2Plugin( id=org.eclipse.xtext.common.types.source, version=2.1.1.v201111141332, name=Xtext Common Types Source )
P2Plugin( id=org.eclipse.xtext.source, version=2.1.1.v201111141332, name=Xtext  Source )
P2Plugin( id=org.eclipse.xtext.util.source, version=2.1.1.v201111141332, name=Xtext Utility  Source )
P2Plugin( id=org.eclipse.xtext.generator.source, version=2.1.1.v201111141332, name=Xtext Generator  Source )
P2Plugin( id=org.eclipse.xtext.ecore.source, version=2.1.1.v201111141332, name=Xtext Ecore Support  Source )
P2Plugin( id=org.eclipse.xtext.builder.source, version=2.1.1.v201111141332, name=Xtext Builder Source )'''
            , deps.bundles.join( '\n' ) )
        
        deps = new DependencySet( repo: repo )
        deps.resolveDependencies( 'org.eclipse.xtext.runtime.feature.group' )
        
        unknownIds = deps.unknownIds as ArrayList
        unknownIds.sort()
        assertEquals( '''\
com.google.common.base
com.google.common.collect
com.ibm.icu
com.ibm.icu.text
de.itemis.xtext.antlr
org.apache.ant
org.eclipse.compare
org.eclipse.core.expressions
org.eclipse.core.filesystem
org.eclipse.core.resources
org.eclipse.core.runtime
org.eclipse.emf.codegen
org.eclipse.emf.codegen.ecore
org.eclipse.emf.common
org.eclipse.emf.common.notify
org.eclipse.emf.common.util
org.eclipse.emf.ecore
org.eclipse.emf.ecore.edit
org.eclipse.emf.ecore.resource
org.eclipse.emf.ecore.xmi
org.eclipse.emf.edit.ui
org.eclipse.jdt.core
org.eclipse.jdt.core.manipulation
org.eclipse.jdt.launching
org.eclipse.jdt.ui
org.eclipse.jface
org.eclipse.jface.text
org.eclipse.ltk.core.refactoring
org.eclipse.ltk.ui.refactoring
org.eclipse.search
org.eclipse.team.core
org.eclipse.team.ui
org.eclipse.text
org.eclipse.ui
org.eclipse.ui.editors
org.eclipse.ui.forms
org.eclipse.ui.ide
org.eclipse.ui.views
org.eclipse.ui.workbench.texteditor
org.osgi.framework'''
            , unknownIds.join( '\n' ) )
        
        assertEquals(
            '''\
P2Feature( id=org.eclipse.xtext.runtime.feature.group, version=2.1.1.v201111141332, name=Xtext Runtime  )
P2Plugin( id=org.eclipse.xtend, version=1.1.0.v201108020519, name=Xtend core  )
P2Plugin( id=org.eclipse.emf.mwe.core, version=1.2.1.v201112070431, name=MWE Core )
P2Plugin( id=org.apache.commons.cli, version=1.2.0.v201105210650, name=Apache Commons CLI )
P2Plugin( id=org.apache.commons.lang, version=2.4.0.v201005080502, name=Apache Jakarta Commons Lang )
P2Plugin( id=org.apache.commons.logging, version=1.1.1.v201005080502, name=Apache Commons Logging Plug-in )
P2Plugin( id=org.eclipse.emf.mwe2.runtime, version=2.2.0.v201112070431, name=Modeling Workflow Engine 2 Runtime )
P2Plugin( id=com.google.inject, version=2.0.0.v201105231817, name=Google Guice )
P2Plugin( id=org.aopalliance, version=1.0.0.v201105210816, name=Aopalliance Plug-in )
P2Plugin( id=org.apache.log4j, version=1.2.15.v201012070815, name=Apache Jakarta log4j Plug-in )
P2Plugin( id=org.antlr.runtime, version=3.2.0.v201101311130, name=ANTLR Runtime )
P2Plugin( id=org.eclipse.xpand, version=1.1.0.v201108020519, name=Xpand core  )
P2Plugin( id=org.eclipse.xtend.typesystem.emf, version=1.0.1.v201108020519, name=Xtend emf typesystem  )
P2Plugin( id=org.eclipse.emf.mwe.utils, version=1.2.1.v201112070431, name=MWE Utilities  )
P2Plugin( id=org.eclipse.xtext, version=2.1.1.v201111141332, name=Xtext  )
P2Plugin( id=org.eclipse.xtext.util, version=2.1.1.v201111141332, name=Xtext Utility  )
P2Plugin( id=com.google.collect, version=1.0.0.v201105210816, name=Google Collections )
P2Plugin( id=org.eclipse.xtext.generator, version=2.1.1.v201111141332, name=Xtext Generator  )
P2Plugin( id=org.eclipse.xtext.common.types, version=2.1.1.v201111141332, name=Xtext Common Types )
P2Plugin( id=org.eclipse.emf.mwe2.lib, version=2.2.0.v201112070431, name=MWE2 Lib )
P2Plugin( id=org.eclipse.xtext.xbase.lib, version=2.1.1.v201111141332, name=Xbase Runtime Library )
P2Plugin( id=org.eclipse.xtext.xtend2.lib, version=2.1.1.v201111141332, name=Xtend2 Runtime Library  )
P2Plugin( id=org.eclipse.xtext.ecore, version=2.1.1.v201111141332, name=Xtext Ecore Support  )
P2Plugin( id=org.eclipse.xtext.logging, version=1.2.15.v201111141332, name=Xtext log4j fragment  )
P2Plugin( id=org.eclipse.xtext.builder, version=2.1.1.v201111141332, name=Xtext Builder )
P2Plugin( id=org.eclipse.xtext.ui, version=2.1.1.v201111141332, name=Xtext UI Core  )
P2Plugin( id=org.eclipse.xtext.common.types.ui, version=2.1.1.v201111141332, name=Xtext Common Types UI )
P2Plugin( id=org.eclipse.xtext.runtime_root, version=2.1.1.v201111141332, name=org.eclipse.xtext.runtime_root )'''
            , deps.bundles.join( '\n' ) )
    }
}