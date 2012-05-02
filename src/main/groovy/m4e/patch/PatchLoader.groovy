/*******************************************************************************
 * Copyright (c) 24.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e.patch

import java.io.File;
import m4e.UserError
import org.codehaus.groovy.control.CompilerConfiguration

class PatchLoader {
    
    File file
    String text
    GlobalPatches globalPatches
    
    PatchLoader( File file, GlobalPatches globalPatches = null ) {
        if( !file.exists() ) {
            throw new UserError( "Can't find patch ${file}" )
        }
        
        this.file = file
        this.globalPatches = globalPatches
    }

    /** For unit tests */
    PatchLoader( String text ) {
        this.text = text
        this.globalPatches = new GlobalPatches()
    }
    
    ScriptedPatchSet patchSet
    ReplaceDependencies replacer
    
    PatchSet load() {
        def config = new CompilerConfiguration()
        config.setScriptBaseClass( 'm4e.patch.PatchScript' )
        
        def shell = new GroovyShell(this.class.classLoader, new Binding(), config)
        
        def text = this.text ? this.text : file.getText( 'utf-8' )
        text += "\n\nthis"
        String source = file ? file.absolutePath : 'JUnit test'
        PatchScript inst = shell.evaluate( text, source )
        
        patchSet = inst.patchSet
        patchSet.source = source
        globalPatches.merge( inst.globalPatches )
        
        replacer = inst.replacer
        
        if( replacer.replacements ) {
            replacer.globalPatches = globalPatches
            
            patchSet.patches << replacer
        }
        
        check()
        
        return patchSet
    }
    
    void check() {
        findDuplicateReplacements()
    }
    
    void findDuplicateReplacements() {
        
        Set<String> keys = []
        
        replacer.replacements.each {
            if( it instanceof ReplaceDependency ) {
                def key = it.pattern.key()
                
                if( !keys.add( key ) ) {
                    throw new UserError( "Found duplicate replace ${key} in patch '{set.source}'" )
                }
            }
        }
    }
}
