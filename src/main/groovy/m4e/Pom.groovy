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

	public static load( def input ) {
		if( input instanceof String ) {
			input = new XMLStringSource( input )
		}
		if( input instanceof File ) {
			input = new XMLInputStreamReader( input.newInputStream() )
		}
		
		XMLParser parser = new XMLParser();
		def doc = parser.parse( input )
		
        def pom = new Pom( doc: doc )
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
}

class PomUtils {
    static void removeWithIndent( Element e ) {
        if( !e ) {
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
