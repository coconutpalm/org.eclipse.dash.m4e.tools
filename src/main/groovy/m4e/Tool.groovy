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
    
    static String VERSION = "0.14-SNAPSHOT (16.05.2012)"
    
    File workDir = new File( "tmp" ).absoluteFile
    
    List<CmdInfo> commands = [
        new CmdInfo( names: ['convert'], impl: ConvertCmd ),
        new CmdInfo( names: ['install', 'in', 'import', 'im', 'imp'], impl: InstallCmd ),
        new CmdInfo( names: ['merge', 'me'], impl: MergeCmd ),
        new CmdInfo( names: ['attach-sources', 'as', 'attach', 'sources'], impl: AttachSourcesCmd ),
        new CmdInfo( names: ['apply-patches', 'patch', 'ap'], impl: PatchCmd ),
        new CmdInfo( names: ['analyze', 'an'], impl: AnalyzeCmd ),
        new CmdInfo( names: ['dependency-management', 'dm'], impl: DependencyManagementCmd ),
        new CmdInfo( names: ['clean'], impl: CleanCmd ),
        new CmdInfo( names: ['p2list', 'p2ls'], impl: P2ListCmd ),
        new CmdInfo( names: ['show', 'view'], impl: ShowRepoCmd ),
        new CmdInfo( names: ['download', 'dl'], impl: DownloadCmd ),
        new CmdInfo( names: ['updateImportExportDB', 'upiedb'], impl: UpdateImportExportDatabaseCmd ),
    ]
    
    void run( String... args ) {
        log.info( "mt4e ${VERSION}" )
        log.debug( "workDir={}", workDir )
        log.debug( "args[${args.size()}]: {}", Arrays.toString( args ) )
        
        if( args.size() == 0 ) {
            throw new UserError( "Missing command. Valid commands are:\n${help()}" )
        }
        
        if( args[0] in ['help', '--help', '-h', '?', '/?', '-help', '-?']) {
            print( help() )
            return
        }
        
        AbstractCommand cmd
        
        outer: for( def ci in commands ) {
            for( name in ci.names ) {
                if( name == args[0] ) {
                    cmd = ci.impl.newInstance()
                    break outer
                }
            }
        }
        
        if( cmd == null ) {
            throw new UserError( "Unknown command ${args[0]}\n${help()}" )
        }
        
        workDir.makedirs()
        
        cmd.workDir = workDir
        cmd.run( args )
        cmd.logSummary()
        
        log.debug( 'Done.' )
    }
    
    void print( String text ) {
        println text // Print help
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
            
            String output = ConsoleUtils.wrapText( "${name} ${ci.impl.DESCRIPTION}" )
            list << output
        }
        return list.join( '\n' )
    }
    
    static void main( String[] args ) {
        try {
            MopSetup.setup();
            new Tool().run( args )
        } catch( UserError t ) {
            t.print()
            System.exit( 1 )
        } catch( Throwable t ) {
            t.printStackTrace()
            System.exit( 1 )
        }
    }
}

class CmdInfo {
    List<String> names
    Class impl
}

class CleanCmd extends AbstractCommand {
    
    final static String DESCRIPTION = '\n- Clean the work directory'
    
    void doRun( String... args ) {
        assert workDir != null
        
        log.info( 'Deleting {} and everything below', workDir )
        workDir.deleteDir()
    }
}