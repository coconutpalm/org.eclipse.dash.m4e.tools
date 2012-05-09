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

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractCommand {

    Logger log = LoggerFactory.getLogger( getClass() )
    
    File workDir
    
    void run( List<String> args ) {
        run( args as String[] )
    }
    
    void run( String... args ) {
        prepare()
        
        doRun( args )
        
        destroy()
    }
    
    void prepare() {
        
    }
    
    void destroy() {
        
    }
    
    abstract void doRun( String... args );
    
    int errorCount = 0
    int warningCount = 0
    
    void warn( Warning warning, String msg ) {
        warningCount ++
        log.warn( msg + '\nFor details, see ' + warning.url() )
    }
    
    void error( Error error, String msg ) {
        errorCount ++
        log.error( msg + '\nFor details, see ' + error.url() )
    }
    
    void mergeCounters( AbstractCommand other ) {
        warningCount += other.warningCount
        errorCount += other.errorCount
    }
    
    void logSummary() {
        if( errorCount ) {
            log.error( "There were ${errorCount} errors and ${warningCount} warnings" )
        } else  if( warningCount ) {
            log.warn( "There were no errors but ${warningCount} warnings" )
        } else {
            log.info( "There were no errors or warnings" )
        }
    }
    
    File repoOption( String[] args, int index ) {
        if( args.size() < index ) {
            throw new UserError( 'Missing path to repository to analyze' )
        }
        
        File repo = new File( args[ index ] ).absoluteFile
        if( !repo.exists() ) {
            throw new UserError( "Directory ${repo} doesn't exist" )
        }

        return repo
    }
}
