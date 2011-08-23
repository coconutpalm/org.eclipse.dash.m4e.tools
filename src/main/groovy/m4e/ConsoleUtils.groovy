/*******************************************************************************
 * Copyright (c) 23.08.2011 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/
package m4e;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleUtils {
    
    static final Logger log = LoggerFactory.getLogger( ConsoleUtils )

    static int consoleWidth() {
        int consoleWidth = 0;
        String columns = System.getenv( 'COLUMNS' )
        if( columns ) {
            try {
                consoleWidth = Integer.parseInt( columns )
            } catch( Exception e ) {
                log.warn( 'Unable to parse COLUMNS [{}]', columns )
            }
        }
        if( consoleWidth <= 0 ) {
            consoleWidth = 80;
        }

        return consoleWidth
    }
    
    static String wrapText( String text ) {
//        println "[${text}]"
        
        int consoleWidth = ConsoleUtils.consoleWidth();
        int indent = 4
        StringBuilder result = new StringBuilder()
        
        String delim = ''
        text.trim().replace( ' +', ' ' ).eachLine() { line ->
            
            line = line.trim()
            if( !line ) {
                result.append( '\n' )
                delim = '\n    '
            } else {
                List lines = wrapLines( line, consoleWidth, indent )
//                println "lines=${lines}"
                consoleWidth -= indent
                indent = 0
                
                lines.each {
                    result << delim << it
                    delim = '\n    '  
                }
            }
        }
        
        return result
    }

    static List<String> wrapLines( String line, int width, int indent ) {
        List<String> result = []
        
        StringBuilder buffer = new StringBuilder()
        String delim = ''
        
        line.trim().split( ' ' ).each() { word ->
            if( buffer.size() + word.size() + 1 < width ) {
                buffer.append( delim ).append( word )
                delim = ' '
            } else {
                result << buffer.toString()
                
                buffer.setLength( 0 )
                buffer.append( word )
                
                width -= indent
                indent = 0
            }
//            println "word=${word} width=${width} buffer[${buffer.size()}]=${buffer}"
        }
        
        if( buffer.size() > 0 ) {
            result << buffer.toString()
        }
        
        return result
    }
    
}
