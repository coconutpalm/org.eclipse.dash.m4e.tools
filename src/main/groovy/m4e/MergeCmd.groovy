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

class MergeCmd extends AbstractCommand {
    
    final static String DESCRIPTION = '''directories... destination\n- Merge several Maven repositories into one.\n\nFor safety reasons, destination must not exist.'''
    
    void run( String... args ) {
        if( args.size() == 1 ) {
            throw new UserError( 'Missing directories to merge' )
        }
        if( args.size() < 3 ) {
            throw new UserError( 'Missing target directory' )
        }

        String[] sources = args[1..-2]
        def target = new File( args[-1] ).absoluteFile
        
        if( target.exists() ) {
            throw new UserError( "Target directory ${target} already exists. Cowardly refusing to continue." )
        }
        
        log.debug( "Sources: ${sources}" )
        log.debug( "Target: ${target}" )
        
        for( source in sources ) {
            log.info( 'Merging {}', source )
            merge( new File( source ).absoluteFile, target )
        }
    }
    
    void merge( File source, File target ) {
        
        target.makedirs()
        
        source.eachFile { File srcPath ->
            File targetPath = new File( target, srcPath.name )
            
            if( srcPath.isDirectory() ) {
                if( targetPath.exists() && !targetPath.isDirectory() ) {
                    throw new RuntimeException( "${srcPath} is a directory but ${targetPath} is a file" )
                }
                
                merge( srcPath, targetPath )
            } else {
                if( targetPath.isDirectory() ) {
                    throw new RuntimeException( "${srcPath} is a file but ${targetPath} is a directory" )
                }
                
                if( targetPath.exists() ) {
                    if( !filesAreEqual( srcPath, targetPath ) ) {
                        log.warn( "File ${srcPath} differs from ${targetPath}" )
                    }
                } else {
                    srcPath.copy( targetPath )
                }
            }
        }
    }
    
    boolean filesAreEqual( File source, File target ) {
        if( source.size() != target.size() ) {
            return false
        }
        
        byte[] buffer1 = new byte[10240]
        byte[] buffer2 = new byte[10240]
        
        try {
            source.withInputStream { InputStream input1 ->
                target.withInputStream { InputStream input2 ->
                    
                    while( true ) {
                        int len1 = input1.read( buffer1 )
                        int len2 = input2.read( buffer2 )
                        
                        if( len1 > -1 ) {
                            if( len1 != len2 ) {
                                throw new FileComparisonException();
                            }
                            
                            if( !Arrays.equals( buffer1, buffer2 ) ) {
                                throw new FileComparisonException();
                            }
                        } else if( len2 < 0 ) {
                            throw new FileComparisonException();
                        }
                    }
                    
                }
            }
        } catch( FileComparisonException e ) {
            return false;
        }
    }
}

class FileComparisonException extends RuntimeException {
    
}