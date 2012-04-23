package m4e.p2.ui

import javax.swing.JFrame
import javax.swing.JTextField;
import javax.swing.JTree
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel
import java.awt.BorderLayout as BL
import java.util.Enumeration;

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
        JTextField filter = new JTextField()
        
        swing.edt {
            frame( title: 'P2 Repository View', defaultCloseOperation: JFrame.EXIT_ON_CLOSE, size: [ 800, 800 ], show: true) {
                borderLayout()
                hbox( constraints: BL.NORTH ) {
                    label( text: 'Filter:', labelFor: filter, displayedMnemonic: 'F' )
                    widget( filter )
                }
                scrollPane( constraints: BL.CENTER ) {
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
        
        l = new FilterChangeListener() {
            void filterChanged( String value ) {
                def state = new SavedExpandedState( repoTree )
                state.save()
                
                model.filter( value )
                
                state.restore()
            }
        }
        filter.document.addDocumentListener( l )
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

class SavedExpandedState {
    private JTree tree
    private List<String[]> openNodes = []
    
    SavedExpandedState( JTree tree ) {
        this.tree = tree
    }
    
    void save() {
        TreePath root = new TreePath( tree.model.getRoot () )
        
        saveRecursive( tree.getExpandedDescendants( root ) )
    }
    
    private void saveRecursive( Enumeration<TreePath> e ) {
        for( TreePath path : e ) {
            savePath( path )
        }
    }
    
    private void savePath( TreePath path ) {
        List<String> segments = []
        for( int i=0; i<path.getPathCount(); i++ ) {
            segments << path.getPathComponent (i).id()
        }
        
        println segments
        openNodes << segments.toArray()
    }
    
    void restore() {
        
        TreePath root = new TreePath( tree.model.getRoot () )
        tree.expandPath( root )
        
        for( String[] segments : openNodes ) {
            open( segments )
        }
    }
    
    private void open( String[] segments ) {
        TreePath path = toPath( segments )
        if( path ) {
            tree.expandPath( path )
        }
    }
    
    private TreePath toPath( String[] segments ) {
        ITreeNode parent = tree.model.root
        TreePath path = new TreePath( parent )
        
        for( int i=1; i<segments.size(); i++ ) {
            String id = segments[i]
            TreePath next = null
            
            for( int j=0; j<parent.getChildCount(); j++ ) {
                Object child = parent.getChild( j )
                
                if( child.id() == id ) {
                    next = new TreePath( path, child )
                    parent = child
                    break
                }
            }
            
            if( !next ) {
                println "Unable to find ${Arrays.toList( segments )}"
                return null
            }
            
            path = next
        }
        
        return path
    }
}

class FilterChangeListener implements DocumentListener {
    
    private String oldValue = ''

    public void insertUpdate( DocumentEvent e ) {
        String newValue = e.document.getText( 0, e.document.length )
        if( oldValue == newValue ) {
            return
        }
        
        oldValue = newValue
        filterChanged( newValue )
    }

    public void removeUpdate (DocumentEvent e)
    {
        insertUpdate( e )
    }

    public void changedUpdate (DocumentEvent e)
    {
        insertUpdate( e )
    }
    
    void filterChanged( String value ) {
        // Do nothing
    }
}