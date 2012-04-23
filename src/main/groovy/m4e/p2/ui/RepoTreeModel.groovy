package m4e.p2.ui

import java.util.List;

import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import m4e.p2.IP2Repo;
import m4e.p2.MergedP2Repo
import m4e.p2.P2Bundle;
import m4e.p2.P2Dependency;
import m4e.p2.P2Other;
import m4e.p2.P2Plugin;
import m4e.p2.P2Unit;

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
        return children.collect { new LabelNode( "${it.id} ${it.version}: ${it.message}", it ) }.sort { it.toString() }
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
        return children.collect { new LabelNode( "${it.id} ${it.version}", it ) }.sort { it.toString() }
    }
    
    @Override
    String toString() {
        return "${children.size()} special units"
    }
}

class SwingBundle extends LazyNode {
    IP2Repo root
    P2Bundle bundle
    boolean hasSource
    
    SwingBundle( IP2Repo root, P2Bundle bundle ) {
        super( bundle.dependencies.findAll { it.id != bundle.id } )
        
        this.root = root
        this.bundle = bundle
    }
    
    @Override
    public List createSwingChildren () {
        return children.collect { new SwingDependency( root, it ) }.sort { it.toString() }
    }
    
    @Override
    String toString() {
        String source = hasSource ? ' (+Source)' : ''
        
        if( bundle.name ) {
            if( bundle.name == bundle.id ) {
                return "${bundle.name} ${bundle.version}${source}"
            }
            
            return "${bundle.name} ${bundle.version} (${bundle.id})${source}"
        }
        
        return "${bundle.id} ${bundle.version}${source}"
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
    List<P2Bundle> bundles
    
    BundleList( IP2Repo root, String title, List<P2Bundle> bundles ) {
        super( bundles.findAll { !it.isSourceBundle() } )
        
        this.bundles = bundles
        this.root = root
        this.title = title
    }
    
    @Override
    public List createSwingChildren () {
        Set<String> sourceBundles = new HashSet( bundles.findAll { it.isSourceBundle() }.collect { it.id.removeEnd( '.source' ) } )
        
        return children.collect {
            def swing = new SwingBundle( root, it )
            swing.hasSource = sourceBundles.contains( it.id )
            return swing
        }.sort { it.toString() }
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
            return repo.repos.collect { new SwingRepo( root, it ) }.sort { it.toString() }
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
