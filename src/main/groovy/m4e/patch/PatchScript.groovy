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
    
    /** The default profile to use */
    void defaultProfile( String name ) {
        replacer.defaultProfile = name
    }
    
    /** The other/non-default profile */
    void profile( String name ) {
        replacer.profile = name
    }
    
    /** Replace a certain dependency with another
     * 
     *  <p>The original dependency will appear in the default profile, the new one
     *  in the other/non-default profile.
     *  
     *  <p>The main use for this is to map Orbit bundles to official Maven artifacts
     */
    void replace( String _pattern, String with ) {
        
        PatchDependency pattern = PatchDependency.fromString( _pattern )
        PatchDependency replacement = PatchDependency.fromString( with )
        
        def rd = new ReplaceDependency( pattern: pattern, replacement: replacement )
        replacer.replacements << rd
    }
    
    /** A dependency to delete everywhere */
    void deleteDependency( String pattern ) {
        patchSet.patches << new DeleteDependency( key: pattern )
    }
    
    /** An artifact that should be deleted */
    void deleteArtifact( String pattern ) {
        globalPatches.artifactsToDelete << pattern
    }
    
    /** Give some bundles a special version */
    void mapQualifier( String pattern, String version ) {
        globalPatches.qualifierPatches << new QualifierPatch( pattern, version )
    }
    
    /** Rename Orbit bundles */
    void orbitRename() {
        globalPatches.renameOrbitBundles = true
    }
    
    /** Exclude this bundle when doing the Orbit rename */
    void orbitExclusion( String bundleName ) {
        globalPatches.orbitExclusions << bundleName
    }
}
