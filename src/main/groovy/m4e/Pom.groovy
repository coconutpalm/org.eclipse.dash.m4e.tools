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

class Dependency {
	Element xml
	
	Element getXml_optional() {
		return xml.getChild( 'optional' )
	}
}
