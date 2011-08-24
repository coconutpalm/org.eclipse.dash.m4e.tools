/*******************************************************************************
 * Copyright (c) 24.08.2011 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/
package m4e

import groovy.xml.MarkupBuilder;
import java.io.File;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DependencyManagementCmd extends AbstractCommand {
        
    static final String DESCRIPTION = '''\
repository groupId:artifactId:version
- Create a POM with a huge dependencyManagement element that contains all the versions of all the artifacts in the repository
'''
    
    File repo
    File dmPom
    String groupId
    String artifactId
    String version

    void run( String... args ) {
        if( args.size() == 1 ) {
            throw new UserError( 'Missing path to repository to analyze' )
        }
        
        repo = new File( args[1] ).canonicalFile
        if( !repo.exists() ) {
            throw new UserError( "Directory ${repo} doesn't exist" )
        }

        if( args.size() <= 2 ) {
            throw new UserError( 'Missing groupId:artifactId:version of the POM to create' )
        }

        String key = args[2]
        
        dmPom = MavenRepositoryTools.buildPath( repo, key, 'pom' )
        
        log.info( "Will write dependencyManagement info to ${dmPom}" )
        
        String[] parts = key.split( ':', -1 )
        groupId = parts[0]
        artifactId = parts[1]
        version = parts[2]
        
        createPom()
    }
    
    void createPom() {
        dmPom.parentFile.makedirs()
        
        File tmp = new File( dmPom.path + '.tmp' )
        
        tmp.withWriter { Writer writer ->
            writer << """\
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>${groupId}</groupId>
    <artifactId>${artifactId}</artifactId>
    <version>${version}</version>
    
    <dependencyManagement>
        <dependencies>
"""

            writeDependencies( writer )            
            
            writer << """\
        </dependencies>
    </dependencyManagement>
</project>
"""
        }
        
        if( dmPom.exists() ) {
            dmPom.usefulDelete()
        }

        tmp.usefulRename( dmPom )
    }
    
    void writeDependencies( Writer writer ) {
        MavenRepositoryTools.eachPom( repo ) {
            if( dmPom != it ) {
                def pom = Pom.load( it )
                writeDependency( writer, pom )
            }
        }
    }
    
    /** short key -> version */
    Map<String, String> versions = [:]
    
    void writeDependency( Writer writer, Pom pom ) {
        String version = pom.version()
        String old = versions.put( pom.shortKey(), version )
        if( null != old ) {
            twoVersionsError( pom, old )
            return
        }
        
        writer << """\
            <dependency>
                <groupId>${pom.groupId()}</groupId>
                <artifactId>${pom.value( Pom.ARTIFACT_ID )}</artifactId>
                <version>${version}</version>
            </dependency>
"""
    }
    
    void twoVersionsError( Pom pom, String oldVersion ) {
        log.error( "The repository contains two versions of ${pom.shortKey()}: ${pom.version()} and ${oldVersion}. Omitting ${pom.version()}" )
    }
}
