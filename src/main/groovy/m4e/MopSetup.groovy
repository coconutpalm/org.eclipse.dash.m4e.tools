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
package m4e

import java.util.zip.ZipInputStream;

class MopSetup {
    
    private static boolean done = false

    static void setup() {
        if( done ) {
            return
        }
        
        done = true
        
        mopUnzip()
        mopUntar()
        mopCopy()
        
        mopString()
        mopFile()
    }
    
    static void mopString() {
        String.metaClass.removeEnd = { String pattern ->
            String result = delegate
            
            if( pattern && result.endsWith( pattern ) ) {
                result = result.substring( 0, result.size() - pattern.size() )
            }
            
            return result
        }
        
        String.metaClass.substringBeforeLast = { String pattern ->
            String result = delegate
            
            if( pattern ) {
                int pos = result.lastIndexOf( pattern )
                if( pos >= 0 ) {
                    return result[0..<pos]
                }
            }
            
            return result
        }
        
        String.metaClass.endsWithOneOf = { String... patterns ->
            for( String pattern : patterns ) {
                if( delegate.endsWith( pattern ) ) {
                    return true
                }
            }
            
            return false
        }
    }
    
    static void mopFile() {
        File.metaClass.makedirs() {
            if( delegate.exists() ) {
                if( delegate.isDirectory() ) {
                    return
                } else {
                    throw new IOException( "File ${delegate.absolutePath} exists but it's not a directory" )
                }
            }
            
            if( !delegate.mkdirs() ) {
                throw new IOException( "Error creating directory ${delegate.absolutePath}" )
            }
        }
    }
    
    static void mopCopy() {
        File.metaClass.copy = { File target ->
            if( delegate.isDirectory() ) {
                target.makedirs()
                
                delegate.eachFile() { src ->
                    String name = src.name
                    File dest = new File( target, name )
                    
                    if( src.isDirectory() ) {
                        dest.makedirs()
                    }
                    
                    src.copy( dest )
                }
            } else {
                if( target.exists() ) {
                    target.delete()
                }
                
                target.parentFile?.makedirs()
                
                delegate.withInputStream() { it ->
                    target << it
                }
            }
        }
    }
    
    static void mopUnzip() {
        File.metaClass.isChildOf { File parent ->
            File child = delegate.canonicalFile
            parent = parent.canonicalFile
            
            while( child ) {
                if( child.equals( parent ) ) {
                    return true
                }
                
                child = child.parentFile
            }
        }
        
        // Code based on an example from http://grooveek.blogspot.com/2009/09/adding-zipping-and-unzipping.html
        File.metaClass.unzip = { File destDir ->
            destDir = destDir.absoluteFile
            
            def archive = new ZipInputStream( new FileInputStream( delegate ) )
            
            archive.withStream {
                def entry
                while( entry = archive.nextEntry ) {
                    
                    def path = new File( destDir, entry.name )
                    if( !path.isChildOf( destDir ) ) {
                        throw new RuntimeException( "ZIP archive contains odd entry '${entry.name}' which would create the file ${path}" )
                    }
                    
                    if( entry.isDirectory() ) {
                        path.makedirs()
                    } else {
                        path.parentFile?.makedirs()
                        
                        def output = new FileOutputStream( path )
                        
                        output.withStream{
                            int len = 0;
                            byte[] buffer = new byte[10240]
                            while ((len = archive.read(buffer)) > 0){
                                output.write(buffer, 0, len);
                            }
                        }
                    }
                }
            }
        }
    }
    
    static void mopUntar() {
        File.metaClass.untar = { File destDir, File log ->
            destDir.makedirs()
            
            String uncompress = ''
            if( delegate.name.endsWith( '.gz' ) ) {
                uncompress = 'z'
            } else if( delegate.name.endsWith( '.bz2' ) ) {
                uncompress = 'j'
            }
            
            List args = [
                'tar', "-x${uncompress}",
                '--directory', destDir.absolutePath,
                '--file', delegate.absolutePath,
            ]*.toString()
            
            def builder = new ProcessBuilder( args )
            builder.redirectErrorStream( true )
            
            Process p = builder.start()
            
            OutputStream output = null
            if( log ) {
                output = log.newOutputStream()
            } else {
                output = System.out
            }
            
            try {
                ProcessGroovyMethods.consumeProcessOutputStream( p, output )
                
                int rc = p.waitFor()
                if( rc != 0 ) {
                    String logInfo = ''
                    if( log ) {
                        logInfo = ". See log file ${log.absolutePath}"
                    }
                    throw new RuntimeException( "Error unpacking archive with ${args}${logInfo}" )
                }
            } finally {
                if( log && output ) {
                    try { output.close() } catch( Exception e ) { log.warn( 'Error closing stream', e ) }
                }
            }
        }
    }
    
}
