package m4e.patch

import java.util.List;

class GlobalPatches {
    List<String> artifactsToDelete = []
    List<QualifierPatch> qualifierPatches = []
    List<String> orbitExclusions = []
    boolean renameOrbitBundles
    
    void merge( GlobalPatches other ) {
        artifactsToDelete.addAll( other.artifactsToDelete )
        qualifierPatches.addAll( other.qualifierPatches )
        orbitExclusions.addAll( other.orbitExclusions )
        
        renameOrbitBundles |= other.renameOrbitBundles
    }
}
