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
    
    static String VERSION = "0.2 (25.07.2011)"
    
    File workDir = new File( "tmp" ).absoluteFile
    
    List<CmdInfo> commands = [
        new CmdInfo( names: ['install', 'in'], impl: InstallCmd, description: '''archives...\n\t- Extract the specified archives and convert the Eclipse plug-ins inside into Maven artifacts'''),
        new CmdInfo( names: ['merge', 'me'], impl: MergeCmd, description: '''directories... destination\n\t- Merge several Maven repositories into one.\n\n\tFor safety reasons, destination must not exist.'''),
        new CmdInfo( names: ['clean'], description: '\n\t- Clean the work directory', impl: CleanCmd),
    ]
    
    void run( String... args ) {
        log.debug( "mt4e ${VERSION}" )
        log.debug( "workDir={}", workDir )
        log.debug( "args[${args.size()}]: {}", args )
        
        setup()
        
        if( args.size() == 0 ) {
            throw new UserError( "Missing command. Valid commands are:\n${help()}" )
        }
        
        def cmd;
        
        outer: for( def ci in commands ) {
            for( name in ci.names ) {
                if( name == args[0] ) {
                    cmd = ci.impl.newInstance();
                    break outer;
                }
            }
        }
        
        if( cmd == null ) {
            throw new UserError( "Unknown command ${args[0]}\n${help()}" )
        }
        
        workDir.makedirs()
        
        cmd.workDir = workDir
        cmd.run( args )
        
        log.debug( 'Done.' )
    }
    
    String help() {
        def list = []
        for( def ci in commands ) {
            String name
            if( ci.names.size() == 1 ) {
                name = "${ci.names[0]}"
            } else {
                name = "[ ${ci.names.join( ' | ' )} ]"
            }
            
            list << "${name} ${ci.description}"
        }
        return list.join( '\n' )
    }
    
    void setup() {
        mopUnzip()
        mopUntar()
        mopCopy()
        
        mopString()
        mopFile()
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
    
    void mopFile() {
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
    
    void mopCopy() {
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
                        path.makedirs()
                    } else {
                        path.parentFile?.makedirs()
                        
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

class CmdInfo {
    List<String> names
    String description
    Class impl
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