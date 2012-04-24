package m4e.patch

import groovy.lang.Script;

/** Patch scripts are evaluated in the context of this class.
 * 
 *  <p>Methods of the class can be called directly from the script.
 *  
 *  <p>The methods will then collect the information in fields; the patch loader
 *  will futher process them later.
 */
abstract class PatchScript extends Script {
    ScriptedPatchSet patchSet = new ScriptedPatchSet()

    ReplaceDependencies replacer = new ReplaceDependencies()
    
    GlobalPatches globalPatches = new GlobalPatches()
    
    void defaultProfile( String name ) {
        replacer.defaultProfile = name
    }
    
    void profile( String name ) {
        replacer.profile = name
    }
    
    void replace( String _pattern, String with ) {
        
        PatchDependency pattern = PatchDependency.fromString( _pattern )
        PatchDependency replacement = PatchDependency.fromString( with )
        
        def rd = new ReplaceDependency( pattern: pattern, replacement: replacement )
        replacer.replacements << rd
    }
    
    void deleteDependency( String pattern ) {
        patchSet.patches << new DeleteDependency( key: pattern )
    }
    
    void deleteArtifact( String pattern ) {
        globalPatches.artifactsToDelete << pattern
    }
    
    /** Give some bundles a special version */
    void mapQualifier( String pattern, String version ) {
        globalPatches.qualifierPatches << new QualifierPatch( pattern, version )
    }
}
