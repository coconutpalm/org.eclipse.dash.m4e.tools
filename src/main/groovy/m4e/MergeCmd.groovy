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
import de.pdark.decentxml.XMLUtils;

class MergeCmd extends AbstractCommand {
    
    final static String DESCRIPTION = '''directories... destination\n- Merge several Maven repositories into one.\n\nFor safety reasons, destination must not exist.'''
    
    void doRun( String... args ) {
        if( args.size() < 2 ) {
            throw new UserError( 'Missing repositories to merge' )
        }
        if( args.size() < 3 ) {
            throw new UserError( 'Missing target repository' )
        }

        String[] sources = args[1..-2]
        def target = new File( args[-1] ).absoluteFile
        
        if( target.exists() ) {
            throw new UserError( "Target repository ${target} already exists. Cowardly refusing to continue." )
        }
        
        prepareErrorLog( target, 'merge' )
        
        mt4eFolder = new File( target, '.mt4e' )
        
        log.debug( "Sources: ${sources}" )
        log.debug( "Target: ${target}" )
        
        for( source in sources ) {
            log.info( 'Merging {}', source )
            File file = new File( source ).absoluteFile
            
            merge( file, target )
        }
        
        closeXmlFiles()
    }
    
    void closeXmlFiles() {
        def exc = null
        
        xmlFiles.values().each {
            try {
                it << '</merged>\n'
                it.close()
            } catch( Exception e ) {
                if( !exc ) {
                    exc = e
                }
            }
        }
        
        if( exc ) {
            throw e
        }
    }
    
    File mt4eFolder
    
    void merge( File source, File target ) {
        
        target.makedirs()
        
        source.eachFile { File srcPath ->
            File targetPath = new File( target, srcPath.name )
            
            if( targetPath.equals( mt4eFolder ) ) {
                mergeMt4eFiles( srcPath, mt4eFolder )
                return
            }
            
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
                        warn( Warning.BINARY_DIFFERENCE, "File ${srcPath} differs from ${targetPath}" )
                    }
                } else {
                    srcPath.copy( targetPath )
                }
            }
        }
    }
    
    void mergeMt4eFiles( File source, File target ) {
        source.eachFile { File srcPath ->
            String name = srcPath.name
            if( IMPORT_EXPORT_DB_FILE == name ) {
                return
            }
            
            File targetPath = new File( target, name )
            
            if( srcPath.isDirectory() ) {
                mergeMt4eFiles( srcPath, targetPath )
                return
            }

            if( name.endsWith( '.xml' ) ) {
                mergeXml( srcPath, targetPath )
            } else {
                warn( Warning.UNABLE_TO_MERGE_MT4E_FILE, "Unable to merge ${srcPath.absolutePath}", [ file: srcPath.absolutePath ] )
            }
        }
    }
    
    Map<String, Writer> xmlFiles = [:]
    
    void mergeXml( File source, File target ) {
        def writer = xmlFiles[ target.name ]
        if( !writer ) {
            if( target.exists() ) {
                warn( Warning.UNABLE_TO_MERGE_MT4E_FILE, "Unable to merge ${source.absolutePath}", [ file: source.absolutePath ] )
            }
            
            target.parentFile?.makedirs()
            
            writer = target.newWriter( UTF_8 )
            xmlFiles[ target.name ] = writer
            
            writer << '<merged>\n'
        }
        
        writer << '<source file="' << XMLUtils.escapeXMLText( source.absolutePath ).replace( '"', '&quot;' ) << '">\n'
        
        source.withReader( UTF_8 ) {
            writer << it
        }
        
        writer << '\n</source>\n'
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