/*******************************************************************************
 * Copyright (c) 21.08.2011 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/
package m4e

import de.pdark.decentxml.*

class PomElement {
    Pom pom
    Element xml
    
    String value( TextNode node ) {
        Element e = xml( node )
            return e == null ? null : e.trimmedText
    }
    
    List list( ListNode node ) {
        def result = xml.getChild( node.name )?.getChildren( node.child ).collect {
            new PomElement( pom: this.pom, xml: it )
        }
        
        return result ?: []
    }
    
    Element xml( TextNode node ) {
        return xml.getChild( node.name )
    }
    
    Element xml( ListNode node ) {
        return xml.getChild( node.name )
    }
    
    void remove() {
        PomUtils.removeWithIndent( xml )
    }
}

class TextNode {
    String name
    String defaultValue
}

class ListNode {
    String name
    String child
}

class Pom extends PomElement {
    
    String source

	public static load( def input ) {
        String source
		if( input instanceof String ) {
            source = "<String>"
			input = new XMLStringSource( input )
		}
		if( input instanceof File ) {
            source = input.absolutePath
			input = new XMLInputStreamReader( input.newInputStream() )
		}
		
		XMLParser parser = new XMLParser();
		def doc = parser.parse( input )
		
        def pom = new Pom( doc: doc, source: source )
        pom.init()
		return pom
	}
	
	Document doc;
	
    void init() {
        this.pom = this
        this.xml = doc.getRootElement()
        
        assert 'project' == this.xml.name
    }
    
	void save( File file ) {
		
		XMLWriter writer = new XMLWriter( new OutputStreamWriter( new FileOutputStream( file ), doc.encoding ) )
		try {
			doc.toXML( writer )
		} finally {
			writer.close()
		}
	}
	
	String toString() {
		return doc.toXML()
	}
	
    static final ListNode DEPENDENCIES = new ListNode( name: 'dependencies', child: 'dependency' )
    
    List<Dependency> getDependencies() {
        return list( DEPENDENCIES ).collect() {
            Dependency.wrap( it )
        }
    }
    
    Profile profile( String name ) {
        def profiles = PomUtils.getOrCreate( xml, 'profiles' )
        def profile = profiles.getChildren( 'profile' ).find {
            def id = it.getChild( 'id' )
            return id.text == name
        }
//        println "name=${name} profile=${profile}"
        
        if( profile ) {
            return new Profile( xml: profile, pom: this )
        }
        
        return createNewProfile( profiles, name )
    }
    
    private Profile createNewProfile( Element profiles, String name ) {
        def xml = new Element( 'profile' )
        profiles.addNode( xml )
        
        def id = PomUtils.getOrCreate( xml, 'id' )
        id.text = name
        
        def profile = new Profile( xml: xml, pom: this )
        profile.activeByDefault( false )
        
        PomUtils.getOrCreate( xml, 'dependencies' )
        
        return profile
    }
}

class PomUtils {
    static void removeWithIndent( Element e ) {
        if( !e || !e.parentElement ) {
            return
        }
        
        int index = e.parentElement.nodeIndexOf( e )
        if( index > 0 ) {
            index --
            Node previous = e.parentElement.getNode( index )
            if( XMLUtils.isText( previous ) ) {
                e.parentElement.removeNode( index )
            }
        }
        e.remove()
    }
    
    static int getLevel( Element e ) {
        int level = 0
        
        while( e ) {
            e = e.getParentElement()
            level ++
        }
        
        return -- level
    }
    
    static Element getOrCreate( Element e, String name ) {
        Element child = e.getChild( name )
        
        if( !child ) {
            child = new Element( name )
            e.addNode( child )
        }
        
        return child
    }
}

class Dependency extends PomElement {
    final static TextNode GROUP_ID = new TextNode( name: 'groupId' )
    final static TextNode ARTIFACT_ID = new TextNode( name: 'artifactId' )
    final static TextNode VERSION = new TextNode( name: 'version' )
    final static TextNode CLASSIFIER = new TextNode( name: 'classifier' )
    final static TextNode TYPE = new TextNode( name: 'type', defaultValue: 'jar' )
    final static TextNode SCOPE = new TextNode( name: 'scope', defaultValue: 'compile' )
    final static TextNode OPTIONAL = new TextNode( name: 'optional', defaultValue: 'false' )
    
    @Override
    public String toString() {
        return "Dependency( ${key()} )";
    }
    
    String key() {
        return "${value( GROUP_ID )}:${value( ARTIFACT_ID )}:${value( VERSION )}";
    }
    
    static Dependency wrap( PomElement e ) {
        if( e instanceof Dependency ) {
            return e
        }
        
        return new Dependency( xml: e.xml, pom: e.pom )
    }
}

class Profile extends PomElement {
    static final ListNode DEPENDENCIES = Pom.DEPENDENCIES
    
    List<Dependency> getDependencies() {
        return list( DEPENDENCIES ).collect() {
            Dependency.wrap( it )
        }
    }
    
    void addDependency( Dependency d ) {
        d.remove()
        xml( DEPENDENCIES ).addNode( d.xml )
    }
    
    void activeByDefault( boolean value ) {
        def activation = PomUtils.getOrCreate( xml, 'activation' )
        def activeByDefault = PomUtils.getOrCreate( activation, 'activeByDefault')
        activeByDefault.text = Boolean.toString( value )
    }
    
    void cleanUp() {
        def activation = xml.getChild( 'activation' )
        if( null == activation ) {
            return
        }
        
        def activeByDefault = activation.getChild( 'activeByDefault' )
        if( null == activeByDefault || activeByDefault.text == 'true' ) {
            return
        }
        
        PomUtils.removeWithIndent( activation )
    }
    
    String getId() {
        def id = xml.getChild( 'id' )
        return id ? id.text : null
    }
    
    @Override
    public String toString() {
        return "Profile( ${id} )";
    }
}

class XmlFormatter {
    Pom pom
    
    void format() {
        pom.xml.getChild( 'profiles' )?.getChildren( 'profile' ).each {
            Profile p = new Profile( xml: it, pom: pom )
            p.cleanUp()
        }
        
        format( pom.xml )
    }
    
    void format( Element e ) {
        int level = PomUtils.getLevel( e ) + 1
        String indent = '\n' + '  ' * level
        
        int N = e.nodeCount()
//        println "level=${level} N=${N} ${e.name}"
        
        // Must work backwards because indentElement() adds new nodes
        for( int i=N-1; i>=0; i-- ) {
            Node n = e.getNode( i )
            
            if( XMLUtils.isElement( n ) ) {
                indentElement( e, i, n, indent )
            }
        }
        
        N = e.nodeCount()
        if( N > 0 ) {
            indent = '\n' + '  ' * ( level - 1 )
            
            Node n = e.getNode( N - 1 )
            if( XMLUtils.isElement( n ) ) {
//                println "Indent closing element ${e.name}"
                def textNode = new Text( indent )
                e.addNode( textNode )
            } else if( XMLUtils.isText( n ) ) {
                String text = n.text
                if( !text.endsWith( indent ) ) {
                    if( n.isWhitespace() ) {
                        n.text = indent
                    } else {
                        // Do nothing; this would modify text nodes like '<profile>id</profile>'
                    }
                }
            }
        }
    }
    
    void indentElement( Element parent, int index, Element e, String indent ) {
        boolean addNode = true
        
        if( index > 0 ) {
            Node previous = parent.getNode( index - 1 )
            
            if( XMLUtils.isText( previous ) ) {
                String text = previous.text
//                println "indent ${e.name} text=${escape(text)} indent=${escape(indent)} ws=${previous.isWhitespace()}"
                if( text.endsWith( indent ) ) {
//                    println '  same'
                    addNode = false
                } else if( previous.isWhitespace() ) {
//                    println '  reuse'
                    previous.text = indent
                    addNode = false
                }
            }
        }
        
        if( addNode ) {
            def textNode = new Text( indent )
            parent.addNode( index, textNode )
        }
        
        format( e )
    }
    
    String escape( String text ) {
        StringBuilder buffer = new StringBuilder()
        for( int i=0; i<text.size(); i++ ) {
            char c = text[i]
            
            switch( c ) {
                case '\n': buffer.append( '\\n' ); break
                case '\r': buffer.append( '\\r' ); break
                case '\t': buffer.append( '\\t' ); break
                default: buffer.append( c )
            }
        }
        
        return buffer.toString()
    }
}
