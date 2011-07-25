/*******************************************************************************
 * Copyright (c) 25.07.2011 Aaron Digulla.
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
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

class Tool {
    
    static final Logger log = LoggerFactory.getLogger( Tool )
    
    static String VERSION = "0.1 (05.07.2011)"
    
    File workDir = new File( "tmp" ).absoluteFile
    
    void run( String... args ) {
        log.debug( "mt4e ${VERSION}" )
        log.debug( "workDir={}", workDir )
        log.debug( "args[${args.size()}]: {}", args )
        
        setup()
        
        if( args.size() == 0 ) {
            // TODO print list of commands
            throw new UserError( 'Missing command' )
        }
        
        def cmd;
        
        switch( args[0] ) {
        case 'im':
        case 'import':
            cmd = new InstallCmd()
            break;
        
        case 'clean':
            cmd = new CleanCmd()
            break;
        }
        
        if( cmd == null ) {
            // TODO print list of commands
            throw new UserError( "Unknown command ${args[0]}" )
        }
        
        workDir.mkdirs()
        
        cmd.workDir = workDir
        cmd.run( args )
        
        log.debug( 'Done.' )
    }
    
    void setup() {
        mopUnzip()
        mopUntar()
        mopCopy()
        
        mopString()
    }
    
    void mopString() {
        String.metaClass.removeEnd = { String pattern ->
            String result = delegate
            
            if( result.endsWith( pattern ) ) {
                result = result.substring( 0, result.size() - pattern.size() )
            }
            
            return result
        }
        
        String.metaClass.endsWidthOneOf = { String... patterns ->
            for( String pattern : patterns ) {
                if( delegate.endsWith( pattern ) ) {
                    return true
                }
            }
            
            return false
        }
    }
    
    void mopCopy() {
        File.metaClass.copy = { File target ->
            if( delegate.isDirectory() ) {
                target.mkdirs()
                
                delegate.eachFile() { src ->
                    String name = src.name
                    File dest = new File( target, name )
                    
                    if( src.isDirectory() ) {
                        dest.mkdirs()
                    }
                    
                    src.copy( dest )
                }
            } else {
                if( target.exists() ) {
                    target.delete()
                }
                
                target.parentFile?.mkdirs()
                
                delegate.withInputStream() { it ->
                    target << it
                }
            }
        }
    }
    
    void mopUnzip() {
        // Code based on an example from http://grooveek.blogspot.com/2009/09/adding-zipping-and-unzipping.html
        File.metaClass.unzip = { File destDir ->
            def result = new ZipInputStream( new FileInputStream( delegate ) )
            
            result.withStream {
                def entry
                while( entry = result.nextEntry ) {
                    
                    def path = new File( destDir, entry.name )
                    // TODO Security: path must be a child of destDir
                    
                    if( entry.isDirectory() ) {
                        path.mkdirs()
                    } else {
                        path.parentFile?.mkdirs()
                        
                        def output = new FileOutputStream( path )
                        
                        output.withStream{
                            int len = 0;
                            byte[] buffer = new byte[10240]
                            while ((len = result.read(buffer)) > 0){
                                output.write(buffer, 0, len);
                            }
                        }
                    }
                }
            }
        }
    }
    
    void mopUntar() {
        File.metaClass.untar = { File destDir, File log ->
            destDir.mkdirs()
            
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
    
    static void main( String[] args ) {
        try {
            new Tool().run( args )
        } catch( UserError t ) {
            t.print()
        } catch( Throwable t ) {
            t.printStackTrace()
        }
    }
}

class CleanCmd {
    
    static final Logger log = LoggerFactory.getLogger( CleanCmd )
    
    File workDir
    
    void run( String... args ) {
        assert workDir != null
        
        log.info( 'Deleting {} and everything below', workDir )
        workDir.deleteDir()
    }
}