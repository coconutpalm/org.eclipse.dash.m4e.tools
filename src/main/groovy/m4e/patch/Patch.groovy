package m4e.patch

import m4e.Pom;

/** A tool to apply a patch to a POM */
abstract class Patch {
    abstract void apply( Pom pom )
}
