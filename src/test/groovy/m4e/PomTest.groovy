/*******************************************************************************
 * Copyright (c) 22.08.2011 Aaron Digulla.
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
import org.junit.Test;

class PomTest {

    @Test
    public void testLoad() throws Exception {
        def pom = Pom.load( POM_XML )
        
        assertEquals( "[Dependency( org.slf4j:slf4j-api:1.6.2 )]", pom.dependencies?.toString() )
    }
    
    @Test
    public void testNoDependencies() throws Exception {
        def pom = Pom.load( '<project />' )
            
        assertEquals( "[]", pom.dependencies?.toString() )
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
}
