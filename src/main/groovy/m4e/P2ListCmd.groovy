package m4e

import java.io.PrintWriter;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import m4e.p2.MergedP2Repo;
import m4e.p2.P2Bundle;
import m4e.p2.P2Dependency;
import m4e.p2.P2Other;
import m4e.p2.P2Repo;
import m4e.p2.P2RepoLoader;
import m4e.p2.IP2Repo;
import m4e.p2.P2Unit;

import groovy.swing.SwingBuilder

class P2ListCmd extends AbstractCommand {

    final static String DESCRIPTION = '''\
URL
    - List the content of a P2 repository.'''
        
    void run( String... args ) {
        
        def url = findURL( args )
        log.info( 'Listing {}...', url )
        
        def loader = new P2RepoLoader( workDir: workDir, url: url )
        IP2Repo repo = loader.load()
        
        String ui = findUI( args )
        
        switch( ui ) {
            case 'text': listToConsole( repo ); break;
            case 'swing': listToSwing( repo ); break;
            default: throw new UserError( "Unsupported UI '${ui}'" )
        }
    }
    
    void listToSwing( IP2Repo repo ) {
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
    
    void listToConsole( IP2Repo repo ) {
        def writer = new PrintWriter( System.out )
        def out = new IndentPrinter( writer, '    ' )
        repo.list( out )
        
        writer.flush()
    }
    
    URL findURL( String[] args ) {
        String s = args.find { it.contains( '://' ) }
        if( s ) {
            return new URL( s )
        }
        
        throw new UserError( "Expected at least one argument: The URL of the p2 repository to list" )
    }
    
    String findUI( String[] args ) {
        int pos = Arrays.asList( args ).indexOf( '--ui' )
        if( -1 == pos ) {
            return 'text'
        }
        
        pos ++
        if( pos == args.size() ) {
            throw new UserError( "Missing argument to option --ui" )
        }
        
        String ui = args[ pos ]
        return ui
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

class RepoTreeModel implements TreeModel {
    
    private IP2Repo root
    private SwingRepo rootNode
    private List<TreeModelListener> listeners = []
    
    RepoTreeModel( IP2Repo root ) {
        this.root = root
        rootNode = new SwingRepo( root, root )
    }
    
    Object getRoot() {
        return rootNode
    }

    Object getChild(Object parent, int index) {
        if( parent instanceof ITreeNode ) {
            return parent.getChild( index )
        }

        return null
    }
    
    int getChildCount(Object parent) {
        if( parent instanceof ITreeNode ) {
            return parent.getChildCount()
        }

        return 0
    }
    
    boolean isLeaf(Object node) {
        if( node instanceof ITreeNode ) {
            return node.isLeaf()
        }

        return true
    }
    
    int getIndexOfChild(Object parent, Object child) {
        if( parent instanceof ITreeNode ) {
            return parent.indexOf( child )
        }
        
        return -1
    }
    
    void valueForPathChanged(TreePath path, Object value) {
        // Do nothing
    }
    
    private void fireTreeNodesChanged( TreePath parentPath, int[] indices, Object[] children ) {
        TreeModelEvent event = new TreeModelEvent( this, parentPath, indices, children )
        
        listeners.each { it.treeNodesChanged( event ) }
    }

    public void addTreeModelListener(TreeModelListener listener) {
        listeners << listener
    }

    public void removeTreeModelListener(TreeModelListener listener) {
        listeners.remove( listener )
    }
}

interface ITreeNode {
    boolean isLeaf()
    int getChildCount()
    Object getChild( int index )
    int indexOf( Object child )
}

abstract class LeafNode implements ITreeNode {
    
    @Override
    boolean isLeaf() {
        return true
    }
    
    @Override
    int getChildCount() {
        return 0
    }
    
    @Override
    Object getChild( int index ) {
        return null
    }
    
    @Override
    int indexOf( Object child ) {
        return -1
    }
}

class LabelNode extends LeafNode {
    String label
    Object data
    
    LabelNode( String label, Object data = null ) {
        this.label = label
        this.data = data
    }
    
    String toString() {
        return label
    }
} 

abstract class LazyNode implements ITreeNode {
    
    protected List children
    private List swingChildren
    private int childCount
    
    LazyNode( List children ) {
        this.childCount = children ? children.size() : 0
        this.children = children
    }

    LazyNode( int childCount ) {
        this.childCount = childCount
    }
        
    @Override
    boolean isLeaf() {
        return childCount == 0
    }
    
    @Override
    int getChildCount() {
        return childCount
    }
    
    @Override
    Object getChild( int index ) {
        if( null == swingChildren ) {
            swingChildren = createSwingChildren()
        }
        
        return swingChildren[ index ]
    }
    
    @Override
    int indexOf( Object child ) {
        int pos = 0
        
        return swingChildren.indexOf( child )
    }
    
    abstract List createSwingChildren();
}

class OtherList extends LazyNode {
    
    OtherList( List<P2Other> others ) {
        super( others )
    }
    
    @Override
    public List createSwingChildren ()
    {
        return children.collect { new LabelNode( "${it.id} ${it.version}: ${it.message}", it ) }
    }
    
    @Override
    String toString() {
        return "${children.size()} other nodes"
    }
}

class UnitList extends LazyNode {
    
    UnitList( List<P2Unit> others ) {
        super( others )
    }

    @Override
    public List createSwingChildren ()
    {
        return children.collect { new LabelNode( "${it.id} ${it.version}", it ) }
    }
    
    @Override
    String toString() {
        return "${children.size()} special units"
    }
}

class SwingBundle extends LazyNode {
    IP2Repo root
    P2Bundle bundle
    
    SwingBundle( IP2Repo root, P2Bundle bundle ) {
        super( bundle.dependencies.findAll { it.id != bundle.id } )
        
        this.root = root
        this.bundle = bundle
    }
    
    @Override
    public List createSwingChildren () {
        def result = children.collect { new SwingDependency( root, it ) }
        result.sort { it.toString() }
        return result;
    }
    
    @Override
    String toString() {
        if( bundle.name ) {
            return "${bundle.name} ${bundle.version} (${bundle.id})"
        }
        
        return "${bundle.id} ${bundle.version}"
    }

}

class SwingDependency implements ITreeNode {
    P2Dependency dependency
    SwingBundle bundle
    
    SwingDependency( IP2Repo root, P2Dependency dependency ) {
        this.dependency = dependency
        
        def bundle = root.latest( dependency.id, dependency.versionRange )
        if( bundle ) {
            this.bundle = new SwingBundle( root, bundle )
        }
    }

    @Override
    public boolean isLeaf ()
    {
        return bundle == null;
    }

    @Override
    public int getChildCount ()
    {
        return bundle == null ? 0 : 1;
    }

    @Override
    public Object getChild (int index)
    {
        return bundle;
    }

    @Override
    public int indexOf (Object child)
    {
        return 0;
    }
    
    @Override
    String toString() {
        return "Dependency ${dependency.id} ${dependency.versionRange}"
    }
}

class BundleList extends LazyNode {
    
    IP2Repo root
    String title
    
    BundleList( IP2Repo root, String title, List<P2Bundle> bundles ) {
        super( bundles )
        
        this.root = root
        this.title = title
    }
    
    @Override
    public List createSwingChildren () {
        def result = children.collect { new SwingBundle( root, it ) }
        result.sort { it.toString () }
        return result;
    }
    
    @Override
    String toString() {
        return "${children.size()} ${title}"
    }
}

class SwingRepo extends LazyNode {
    IP2Repo repo
    IP2Repo root
    
    SwingRepo( IP2Repo root, IP2Repo current ) {
        super( current instanceof MergedP2Repo ? current.repos.size() : 5 )
        
        this.root = root
        this.repo = current
    }
    
    @Override
    public List createSwingChildren ()
    {
        if( repo instanceof MergedP2Repo ) {
            return repo.repos.collect { new SwingRepo( root, it ) }
        }
        
        return [
            new BundleList( getRoot(), 'categories', repo.categories ),
            new BundleList( getRoot(), 'features', repo.features ),
            new BundleList( getRoot(), 'plugins', repo.plugins ),
            new UnitList( repo.units ),
            new OtherList( repo.others )
        ]
    }
    
    @Override
    String toString() {
        return repo.toString()
    }
}