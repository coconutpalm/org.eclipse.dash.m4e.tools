package m4e.p2.ui

import javax.swing.JFrame
import javax.swing.JTree
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.TreeSelectionModel

import groovy.swing.SwingBuilder
import m4e.p2.IP2Repo
import m4e.p2.P2Other
import m4e.p2.P2Unit

class P2RepoView {
    
    private IP2Repo repo
    
    P2RepoView( IP2Repo repo ) {
        this.repo = repo
    }
    
    void show() {
        def swing = new SwingBuilder()
        
        def model = new RepoTreeModel( repo )
        JTree repoTree
        
        swing.edt {
            frame( title: 'P2 Repository View', defaultCloseOperation: JFrame.EXIT_ON_CLOSE, size: [ 800, 800 ], show: true) {
                scrollPane {
                    repoTree = tree( model: model )
                }
            }
        }
        
        repoTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        def l = {
            def node = repoTree.lastSelectedPathComponent
            selectionChanged( node )
        } as TreeSelectionListener
        repoTree.addTreeSelectionListener( l )
    }
    
    void selectionChanged( Object node ) {
        println node
        if( node instanceof LabelNode ) {
            def data = node.data
            println data
            
            if( data instanceof P2Unit ) {
                println data.xml
            } else if( data instanceof P2Other ) {
                println data.xml
            }
        }
    }
}
