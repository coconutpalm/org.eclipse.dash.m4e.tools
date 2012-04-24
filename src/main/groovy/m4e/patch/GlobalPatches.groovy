package m4e.patch

import java.util.List;

class GlobalPatches {
    List<String> artifactsToDelete = []
    List<QualifierPatch> qualifierPatches = []
    List<String> orbitExceptions = []
    
    void merge( GlobalPatches other ) {
        artifactsToDelete.addAll( other.artifactsToDelete )
        qualifierPatches.addAll( other.qualifierPatches )
        orbitExceptions.addAll( other.orbitExceptions )
    }
}
