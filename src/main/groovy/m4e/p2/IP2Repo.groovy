package m4e.p2

import groovy.util.IndentPrinter;

interface IP2Repo {
    P2Bundle latest( String id )
    P2Bundle latest( String id, VersionRange range )
    P2Bundle find( String id, Version version )
    void list( IndentPrinter out )
}
