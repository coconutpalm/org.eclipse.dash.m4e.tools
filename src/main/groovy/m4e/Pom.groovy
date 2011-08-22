package m4e

import de.pdark.decentxml.Document;
import de.pdark.decentxml.Element;
import de.pdark.decentxml.XMLInputStreamReader;
import de.pdark.decentxml.XMLParser;
import de.pdark.decentxml.XMLStringSource;
import de.pdark.decentxml.XMLWriter;

class Pom {

	public static load( def input ) {
		if( input instanceof String ) {
			input = new XMLStringSource( input )
		}
		if( input instanceof File ) {
			input = new XMLInputStreamReader( input.newInputStream() )
		}
		
		XMLParser parser = new XMLParser();
		def doc = parser.parse( input )
		
		return new Pom( doc: doc )
	}
	
	Document doc;
	
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
	
	private List<Dependency> __dependencies
	
	List<Element> getDependencies() {
		if( !__dependencies ) {
			Element dependencies = doc.getChild( 'project' ).getChild( 'dependencies' )
			__dependencies = dependencies ? dependencies.getChildren( 'dependency' ).collect {
				new Dependency( xml: it )
			} : []
		}
		
		return __dependencies
	}
}

class TextNode {
    String name
    String defaultValue
}

class PomElement {
    Element xml
    List children
    
    String value( TextNode node ) {
        Element e = xml( node )
        return e == null ? null : e.trimmedText
    }
    
    Element xml( TextNode node ) {
        return xml.getChild( node.name )
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
    
    Dependency() {
        children = [
            GROUP_ID, ARTIFACT_ID, VERSION, CLASSIFIER, TYPE, SCOPE, OPTIONAL
            // TODO Support exclusions
        ]
    }
    
    @Override
    public String toString() {
        return "Dependency( ${key()} )";
    }
    
    String key() {
        return "${value( GROUP_ID )}:${value( ARTIFACT_ID )}:${value( VERSION )}";
    }
}
