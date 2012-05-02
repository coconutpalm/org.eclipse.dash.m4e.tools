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
    
    void value( TextNode node, String text ) {
        Element e = xml( node )
        if( e == null ) {
            if( !text ) {
                return
            }
            
            e = PomUtils.getOrCreate( xml, node.name )
            e.text = text
        } else {
            if( !text ) {
                PomUtils.removeWithIndent( e )
            } else {
                e.text = text
            }
        }
    }
    
    List list( ListNode node ) {
        def result = xml.getChild( node.name )?.getChildren( node.child ).collect {
            new PomElement( pom: this.pom, xml: it )
        }
        
        return result ?: []
    }
    
    Element xml( node ) {
        if( !(node instanceof String ) ) {
            node = node.name
        }
        return xml.getChild( node )
    }
    
    PomElement element( node ) {
        Element child = xml( node )
        
        return child ? new PomElement( xml: child, pom: pom ) : null
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
    
    final static TextNode GROUP_ID = new TextNode( name: 'groupId' )
    final static TextNode ARTIFACT_ID = new TextNode( name: 'artifactId' )
    final static TextNode VERSION = new TextNode( name: 'version' )
    final static TextNode PARENT = new TextNode( name: 'parent' )

    String source
    
    String key() {
        return "${groupId()}:${artifactId()}:${version()}";
    }
    
    String shortKey() {
        return "${groupId()}:${artifactId()}";
    }
    
    String groupId() {
        String groupId = value( GROUP_ID )
        if( null == groupId ) {
            PomElement parent = element( PARENT )
            groupId = parent.value( GROUP_ID )
        }
        return groupId
    }
    
    String artifactId() {
        return value( ARTIFACT_ID )
    }

    String version() {
        String version = value( VERSION )
        if( null == version ) {
            PomElement parent = element( PARENT )
            version = parent.value( VERSION )
        }
        return version
    }

	public static Pom load( def input ) {
        String source
		if( input instanceof String ) {
            source = "<String>"
			input = new XMLStringSource( input )
		}
		if( input instanceof File ) {
            source = input.absolutePath
			input = new XMLIOSource( input )
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
    
    List<String> files() {
        File dir = new File( source ).parentFile
        
        String prefix = "${artifactId()}-${version()}"
        
        Set<String> files = new TreeSet()
        dir.eachFile() { File item ->
            String name = item.name
            
            if( !name.startsWith( prefix ) || name.endsWith( '.bak' ) ) {
                return
            }
            
            name = name.substring( prefix.size() )
            
            if( name.startsWith( '.' ) || name.startsWith( '-' ) ) {
                name = name.substring( 1 )
            }
            name = name.removeEnd( '.sha1' )
            name = name.removeEnd( '.md5' )
            name = name.removeEnd( '.jar' )
            
            files << name
        }
        
        return new ArrayList( files )
    }
    
	void save( File file ) {
		
        String path = file.absolutePath
        
        File tmp = new File( path + '.tmp' )
        File bak = new File( path + '.bak' )
        
		XMLWriter writer = new XMLWriter( new OutputStreamWriter( new FileOutputStream( tmp ), doc.encoding ?: 'utf-8' ) )
		try {
			doc.toXML( writer )
		} finally {
			writer.close()
		}
        
        if( bak.exists() ) {
            bak.usefulDelete()
        }
        
        if( file.exists() ) {
            file.usefulRename( bak )
        }

        tmp.usefulRename( file )
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
        def profiles = xml.getChild( 'profiles' )
        if( ! profiles ) {
            return null
        }
        
        def profile = profiles.getChildren( 'profile' ).find {
            def id = it.getChild( 'id' )
            return id.text == name
        }
//        println "name=${name} profile=${profile}"
        
        return profile == null ? null : new Profile( xml: profile, pom: this ) 
    }
    
    Profile getOrCreateProfile( String name ) {
        def profile = profile( name )
       
        if( profile ) {
            return new Profile( xml: profile, pom: this )
        }
        
        def profiles = PomUtils.getOrCreate( xml, 'profiles' )
        return profile == null ? createNewProfile( profiles, name ) : profile
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
        
        Element parent = e.parentElement
        int index = parent.nodeIndexOf( e )
        while( index > 0 ) {
            index --
            
            Node previous = parent.getNode( index )
            if( ! XMLUtils.isText( previous ) ) {
                break
            }
            
            parent.removeNode( index )
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
    final static TextNode GROUP_ID = Pom.GROUP_ID
    final static TextNode ARTIFACT_ID = Pom.ARTIFACT_ID
    final static TextNode VERSION = Pom.VERSION
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
    
    String shortKey() {
        return "${value( GROUP_ID )}:${value( ARTIFACT_ID )}";
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
    private String indentStep = '  '
    
    void format() {
        pom.xml.getChild( 'profiles' )?.getChildren( 'profile' ).each {
            Profile p = new Profile( xml: it, pom: pom )
            p.cleanUp()
        }
        
        format( pom.xml )
    }
    
    void format( Element e ) {
        int level = PomUtils.getLevel( e ) + 1
        String indent = '\n' + indentStep * level
        
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
            indent = '\n' + indentStep * ( level - 1 )
            
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
