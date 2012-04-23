package m4e

import java.io.PrintWriter;

import javax.swing.JFrame;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath;

import m4e.p2.MergedP2Repo;
import m4e.p2.P2Bundle;
import m4e.p2.P2Dependency;
import m4e.p2.P2Repo;
import m4e.p2.P2RepoLoader;
import m4e.p2.IP2Repo;

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
        
        swing.edt {
            frame( title: 'P2 Repository View', defaultCloseOperation: JFrame.EXIT_ON_CLOSE, size: [ 800, 800 ], show: true) {
                scrollPane {
                    tree( model: model )
                }
            }
        }
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
}

class RepoTreeModel implements TreeModel {
    
    private IP2Repo root
    private List listeners = []
    
    IP2Repo getRoot() {
        return root
    }
    
    RepoTreeModel( IP2Repo root ) {
        this.root = root
    }
    
    Object getChild(Object parent, int index) {
        if( parent instanceof MergedP2Repo ) {
            return parent.repos[ index ]
        }
        if( parent instanceof P2Repo ) {
            switch( index ) {
                case 0: return new BundleList( getRoot(), 'categories', parent.categories, index )
                case 1: return new BundleList( getRoot(), 'features', parent.features, index )
                case 2: return new BundleList( getRoot(), 'plugins', parent.plugins, index )
                // TODO other
            }
            
            return null
        }
        if( parent instanceof ITreeNode ) {
            return parent.getChild( index )
        }

        return null
    }
    
    int getChildCount(Object parent) {
        if( parent instanceof MergedP2Repo ) {
            return parent.size()
        }
        if( parent instanceof P2Repo ) {
            // TODO other
            return 3
        }
        if( parent instanceof ITreeNode ) {
            return parent.getChildCount()
        }

        return 0
    }
    
    boolean isLeaf(Object node) {
        if( node instanceof MergedP2Repo ) {
            return false
        }
        if( node instanceof P2Repo ) {
            return false
        }
        if( node instanceof ITreeNode ) {
            return node.isLeaf()
        }

        return true
    }
    
    int getIndexOfChild(Object parent, Object child) {
        if( parent instanceof MergedP2Repo ) {
            return parent.repos.indexOf( child )
        }
        if( parent instanceof P2Repo ) {
            return child.index
        }
        if( parent instanceof ITreeNode ) {
            return parent.indexOf( child )
        }
        
        return -1
    }
    
    void valueForPathChanged(TreePath path, Object value) {
        // Do nothing
    }
    
    private void fireTreeNodesChanged(TreePath parentPath, int[] indices, Object[] children) {
        TreeModelEvent event = new TreeModelEvent(this, parentPath, indices, children);
        Iterator iterator = listeners.iterator();
        TreeModelListener listener = null;
        while (iterator.hasNext()) {
            listener = (TreeModelListener) iterator.next();
            listener.treeNodesChanged(event);
        }
    }

    public void addTreeModelListener(TreeModelListener listener) {
        listeners.add(listener);
    }

    public void removeTreeModelListener(TreeModelListener listener) {
        listeners.remove(listener);
    }
}

interface ITreeNode {
    boolean isLeaf()
    int getChildCount()
    Object getChild( int index )
    int indexOf( Object child )
}

class SwingBundle implements ITreeNode {
    IP2Repo root
    P2Bundle bundle
    List<P2Dependency> deps
    List<SwingDependency> swingDeps
    
    SwingBundle( IP2Repo root, P2Bundle bundle ) {
        this.root = root
        this.bundle = bundle
        
        this.deps = bundle.dependencies.findAll { it.id != bundle.id }
    }
    
    @Override
    boolean isLeaf() {
        return deps.isEmpty()
    }
    
    @Override
    int getChildCount() {
        return deps.size()
    }
    
    @Override
    Object getChild( int index ) {
        if( null == swingDeps ) {
            swingDeps = deps.collect { new SwingDependency( root, it ) }
            swingDeps.sort { it.toString() }
        }
        
        return swingDeps[ index ]
    }
    
    @Override
    int indexOf( Object child ) {
        int pos = 0
        
        return swingDeps.indexOf( child )
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

class BundleList implements ITreeNode {
    
    String title
    int index
    List<SwingBundle> bundles
    
    BundleList( IP2Repo root, String title, List<P2Bundle> bundles, int index ) {
        this.title = title
        
        this.bundles = bundles.collect { new SwingBundle( root, it ) }
        this.bundles.sort { it.toString () }
        
        this.index = index
    }
    
    @Override
    boolean isLeaf() {
        return bundles.isEmpty()
    }
    
    @Override
    int getChildCount() {
        return bundles.size()
    }
    
    @Override
    Object getChild( int index ) {
        return bundles[ index ]
    }
    
    @Override
    int indexOf( Object child ) {
        return bundles.indexOf( child )
    }
    
    @Override
    String toString() {
        return "${bundles.size()} ${title}"
    }
}
