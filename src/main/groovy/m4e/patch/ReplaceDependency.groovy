package m4e.patch

/** Bean that is used by the ReplaceDependencies tool to replace one dependency with another */
class ReplaceDependency {
    PatchDependency pattern
    PatchDependency replacement
    
    @Override
    public String toString() {
        return "ReplaceDependency( ${pattern} -> ${replacement} )"
    }

}
