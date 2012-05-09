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
import m4e.maven.ErrorLog
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractCommand implements CommonConstants {

    Logger log = LoggerFactory.getLogger( getClass() )
    
    File workDir
    ErrorLog errorLog
    
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
        if( errorLog ) {
            errorLog.close()
        }
    }
    
    void prepareErrorLog( File repo, String command ) {
        if( errorLog ) {
            errorLog.close()
        }
        
        errorLog = new ErrorLog( repo: repo, command: command )
    }
    
    abstract void doRun( String... args );
    
    int errorCount = 0
    int warningCount = 0
    
    void warn( Warning warning, String msg, Map xml = null ) {
        warningCount ++
        log.warn( msg + '\nFor details, see ' + warning.url() )
        
        appendToErrorLog( 'warning', warning.code(), msg, xml )
    }
    
    void appendToErrorLog( String nodeName, String code, String msg, Map xml ) {
        if( ! errorLog ) {
            return
        }
        
        Map map = new LinkedHashMap()
            
        map['code'] = code
        if( xml ) {
            map.putAll( xml )
        }
            
        errorLog.write().invokeMethod( nodeName, [ map, msg ] )
    }
    
    void error( Error error, String msg, Map xml = null ) {
        errorCount ++
        log.error( msg + '\nFor details, see ' + error.url() )
        
        appendToErrorLog( 'error', error.code(), msg, xml )
    }
    
    void error( Error error, String msg, Exception e, Map xml = null ) {
        errorCount ++
        log.error( msg + '\nFor details, see ' + error.url(), e )
        
        if( ! xml ) {
            xml = [:]
        }
        
        xml[ 'exception' ] = e.message
        
        appendToErrorLog( 'error', error.code(), msg, xml )
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
