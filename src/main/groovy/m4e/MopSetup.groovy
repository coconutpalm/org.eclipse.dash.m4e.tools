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

import java.util.zip.ZipEntry
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        mopZipFile()
    }

    static void mopString() {
        String.metaClass.removeStart = { String pattern ->
            String result = delegate

            if( pattern && result.startsWith( pattern ) ) {
                result = result.substring( pattern.size() )
            }

            return result
        }

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

        String.metaClass.substringAfterLast = { String pattern ->
            String result = delegate

            if( pattern ) {
                int pos = result.lastIndexOf( pattern )
                if( pos >= 0 ) {
                    return result.substring( pos + pattern.size() )
                }
            }

            return ""
        }

        String.metaClass.substringBefore = { String pattern ->
            String result = delegate

            if( pattern ) {
                int pos = result.indexOf( pattern )
                if( pos >= 0 ) {
                    return result[0..<pos]
                }
            }

            return result
        }

        String.metaClass.substringAfter = { String pattern ->
            String result = delegate

            if( pattern ) {
                int pos = result.indexOf( pattern )
                if( pos >= 0 ) {
                    return result.substring( pos + 1 )
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
        File.metaClass.makedirs = {
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

        File.metaClass.usefulDelete = {
            if( !delegate.exists() ) {
                return
            }

            if( !delegate.delete() ) {
                if( delegate.isDirectory() ) {
                    throw new IOException( "Error deleting ${delegate.absolutePath}; expected file but was a directory" )
                }

                throw new IOException( "Unknown error deleting file ${delegate.absolutePath}" )
            }
        }

        File.metaClass.usefulRename = { to ->
            if( !delegate.renameTo( to ) ) {
                throw new IOException( "Unknown error while renaming file ${delegate.absolutePath} to ${to}" )
            }
        }

        File.metaClass.normalize = {
            return PathUtils.normalize( delegate )
        }

        File.metaClass.pathRelativeTo = { File parent ->
            String file = delegate.canonicalFile.normalize().toString()
            String parentPath = parent.canonicalFile.normalize().toString() + '/'

            return file.removeStart( parentPath )
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
                
                target.setLastModified( delegate.lastModified() )
            }
        }
    }

    static void mopUnzip() {
        File.metaClass.isChildOf = { File parent ->
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
        File.metaClass.untar = { File destDir, File logFile ->
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
            if( logFile ) {
                output = logFile.newOutputStream()
            } else {
                output = System.out
            }

            try {
                ProcessGroovyMethods.consumeProcessOutputStream( p, output )

                int rc = p.waitFor()
                if( rc != 0 ) {
                    String logInfo = ''
                    if( logFile ) {
                        logInfo = ". See log file ${logFile.absolutePath}"
                    }
                    throw new RuntimeException( "Error unpacking archive with ${args}${logInfo}" )
                }
            } finally {
                if( logFile && output ) {
                    try {
                        output.close()
                    } catch( Exception e ) {
                        Logger log = LoggerFactory.getLogger( File.class )
                        log.warn( 'Error closing stream', e )
                    }
                }
            }
        }
    }

    static void mopZipFile() {
        ZipFile.metaClass.getAt = { String path ->
            ZipFile that = delegate
            
            return that.getEntry( path )
        }
        
        ZipFile.metaClass.eachEntry = { Closure c ->
            ZipFile that = delegate
            
            for( ZipEntry entry : that.entries() ) {
                c.call( entry )
            }
        }
        
        ZipFile.metaClass.withInputStream = { ZipEntry entry, Closure c ->
            ZipFile that = delegate
            
            def stream = that.getInputStream( entry )
            try {
                c.call( stream )
            } finally {
                stream.close()
            }
        }
    }
}
