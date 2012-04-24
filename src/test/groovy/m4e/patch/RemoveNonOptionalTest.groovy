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
package m4e.patch;

import static org.junit.Assert.*;

import m4e.Pom;
import org.junit.Test;

class RemoveNonOptionalTest {

    @Test
    public void testEmptyPom() throws Exception {
		def pom = Pom.load( "<project />" )
		
		new RemoveNonOptional().apply( pom )
		
		assertEquals( '<project />', pom.toString() )
	}
    
	static final String POM_XML = '''\
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>

        <dependencies>
                <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                        <version>1.6.2</version>
                        <optional>false</optional>
                </dependency>
        </dependencies>
</project>
'''
	
    @Test
    public void testOptionalDependency() throws Exception {
		String input = POM_XML
		input = input.replace( 'false', 'true' )
    	def pom = Pom.load( input )
    			
		new RemoveNonOptional().apply( pom )
    	
		assertEquals( input, pom.toString() )
    }
	
	@Test
	public void testMandatoryDependency() throws Exception {
		String input = POM_XML
		String expected = input.replaceAll( '\\s+<optional>false</optional>', '' )
		def pom = Pom.load( input )
		
		new RemoveNonOptional().apply( pom )
		
		assertEquals( expected, pom.toString() )
	}
}
