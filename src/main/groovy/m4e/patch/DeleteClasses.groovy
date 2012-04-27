/*******************************************************************************
 * Copyright (c) 27.07.2011 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e.patch

import java.util.jar.Manifest
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream;
import m4e.Glob
import m4e.MavenRepositoryTools;
import m4e.Pom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DeleteClasses extends Patch {
    
    Logger log = LoggerFactory.getLogger( DeleteClasses ) 

    File repo
    Glob keyPattern
    List<Glob> patterns = []
    
    /** Delete class files from a bundle.
     * 
     *  <p>This patch is applied before Orbit bundles are renamed
     */
    DeleteClasses( File repo, String key, List<String> patterns ) {
        
        this.repo = repo
        this.keyPattern = new Glob( key )
        
        patterns.each {
            def p = new Glob( it )
            this.patterns << p
        }
        
        patterns << new Glob( 'META-INF/ECLIPSEF.SF' )
        patterns << new Glob( 'META-INF/ECLIPSEF.RSA' )
    }
    
    @Override
    public void apply( Pom pom ) {
        String key = pom.key()
        if( !keyPattern.matches( key ) ) {
            return
        }
        
        File jarFile = MavenRepositoryTools.buildPath( repo, key, 'jar' )
        assert jarFile.exists()
        
        log.info( 'Deleting classes from {}', jarFile )
        
        File tmp = MavenRepositoryTools.buildPath( repo, key, 'tmp' )
        
        tmp.withOutputStream {
            def out = new ZipOutputStream( it )
            
            filterBundle( new ZipFile( jarFile ), out )
            
            out.close()
        }
        
        File backup = new File( jarFile.toString() + '.bak' )
        if( !backup.exists() ) {
            jarFile.usefulRename( backup )
        } else {
            jarFile.usefulDelete()
        }
        
        tmp.usefulRename( jarFile )
    }

    int count
    
    void filterBundle( ZipFile archive, ZipOutputStream out ) {
        
        archive.eachEntry { ZipEntry entry ->
            
            ZipEntry clone = new ZipEntry( entry.name )
            clone.time = entry.time
            clone.comment = entry.comment
            clone.extra = entry.extra
            
            if( entry.name == 'META-INF/MANIFEST.MF' ) {
                def m
                archive.withInputStream( entry ) {
                    m = new Manifest( it )
                }
                
                m.entries.clear()
                
                clone.time = System.currentTimeMillis()
                
                out.putNextEntry( clone )
                m.write( out )
                out.closeEntry()
                
                return
            }
            
            for( Glob g : patterns ) {
                if( g.matches( entry.name ) ) {
                    log.debug( 'Deleting {}', entry.name )
                    count ++
                    return
                }
            }
            
            out.putNextEntry( clone )
            archive.withInputStream( entry ) {
                out << it
            }
            out.closeEntry()
        }
        
    }
}
