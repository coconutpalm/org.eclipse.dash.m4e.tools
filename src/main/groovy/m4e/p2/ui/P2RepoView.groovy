package m4e.p2.ui

import javax.swing.AbstractAction
import javax.swing.Action;
import javax.swing.JFrame
import javax.swing.JPopupMenu
import javax.swing.JTextField;
import javax.swing.JTree
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel
import java.awt.BorderLayout as BL
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.Enumeration;

import groovy.swing.SwingBuilder
import groovy.swing.impl.DefaultAction;
import m4e.p2.DependencySet;
import m4e.p2.IP2Repo
import m4e.p2.P2Bundle
import m4e.p2.P2Other
import m4e.p2.P2Unit

class P2RepoView {
    
    private IP2Repo repo
    private File workDir
    
    P2RepoView( IP2Repo repo, File workDir ) {
        this.repo = repo
        this.workDir = workDir
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
        
        l = new PopupAdapter( workDir: workDir )
        repoTree.addMouseListener( l )
        
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

class PopupAdapter extends MouseAdapter {
    
    File workDir
    
    void mousePressed( MouseEvent e ) {
        if( !e.isPopupTrigger() ) {
            return
        }
        
        JTree tree = e.source
        def path = tree.getPathForLocation( e.x, e.y )
        if( !path ) {
            return
        }
        
        tree.selectionPath = path
        def actions = getActions( tree, path, path.lastPathComponent )
        if( !actions ) {
            return
        }
        
        def popup = new JPopupMenu()
        actions.each {
            popup.add( it )
        }
        
        popup.show( tree, e.x, e.y )
    }

    List<Action> getActions( JTree tree, TreePath path, SwingBundle selection ) {
        P2Bundle bundle = selection.bundle
        IP2Repo repo = null
        for( TreePath current = path.parentPath; current; current = current.parentPath ) {
            def obj = current.lastPathComponent
            if( obj instanceof SwingRepo ) {
                repo = obj.repo
                break
            }
        }
        
        println repo
        
        def result = []
        if( repo ) {
            result << new DownloadAction( repo, bundle, workDir )
        }
        
        return result
    }

    List<Action> getActions( JTree tree, TreePath path, Object selection ) {
        return []
    }
}

class DownloadAction extends AbstractAction {
    
    IP2Repo repo
    P2Bundle bundle
    File workDir
    
    DownloadAction( IP2Repo repo, P2Bundle bundle, File workDir ) {
        super( 'Download' )
        
        this.repo = repo
        this.bundle = bundle
        this.workDir = workDir
    }

    void actionPerformed( ActionEvent e ) {
        def deps = new DependencySet( repo: repo )
        deps.add( bundle )
        
        SwingBuilder.build {
            doOutside {
                deps.download( workDir )
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