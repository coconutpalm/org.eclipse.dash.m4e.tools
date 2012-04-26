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
    
    private IP2Repo repo
    private SwingRepo rootNode
    private List<TreeModelListener> listeners = []
    
    RepoTreeModel( IP2Repo repo ) {
        this.repo = repo
        rootNode = new SwingRepo( this, repo, 0 )
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
    
    String pattern = ''
    
    void filter( String pattern ) {
//        println "filter '${pattern}'"
        
        this.pattern = pattern
        
        rootNode = new SwingRepo( this, repo, 0 )
        
        def path = new TreePath( repo )
        TreeModelEvent event = new TreeModelEvent( this, path, null, null )
        listeners.each { it.treeStructureChanged( event ) }
    }
    
    boolean matches( String nodeLabel ) {
        if( pattern.size() == 0 ) {
            return true
        }
        
        boolean result = nodeLabel.contains( pattern )
//        println "${result} ${nodeLabel}"
        return result
    }
}

interface ITreeNode {
    boolean isLeaf()
    int getChildCount()
    Object getChild( int index )
    int indexOf( Object child )
    String id()
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
    
    @Override
    String id() {
        return toString()
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
    
    @Override
    String id() {
        return 'other';
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
    
    @Override
    String id() {
        return 'specialUnit';
    }
}

class SwingBundle extends LazyNode {
    RepoTreeModel model
    P2Bundle bundle
    P2Bundle source
    
    SwingBundle( RepoTreeModel model, P2Bundle bundle ) {
        super( bundle.dependencies.findAll { it.id != bundle.id && model.matches( "${it.id}" ) } )
        
        this.model = model
        this.bundle = bundle
    }
    
    @Override
    public List createSwingChildren () {
        return children.collect { new SwingDependency( model, it ) }.sort { it.toString() }
    }
    
    @Override
    String toString() {
        String source = source ? ' (+Source)' : ''
        
        if( bundle.name ) {
            if( bundle.name == bundle.id ) {
                return "${bundle.name} ${bundle.version}${source}"
            }
            
            return "${bundle.name} ${bundle.version} (${bundle.id})${source}"
        }
        
        return "${bundle.id} ${bundle.version}${source}"
    }

    @Override
    String id() {
        return "${bundle.id}:${bundle.version}";
    }
}

class SwingDependency implements ITreeNode {
    P2Dependency dependency
    SwingBundle bundle
    
    SwingDependency( RepoTreeModel model, P2Dependency dependency ) {
        this.dependency = dependency
        
        def bundle = model.repo.latest( dependency.id, dependency.versionRange )
        if( bundle ) {
            this.bundle = new SwingBundle( model, bundle )
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

    @Override
    String id() {
        return "${dependency.id}:${dependency.versionRange}";
    }
}

class BundleList extends LazyNode {
    
    RepoTreeModel model
    String title
    List<P2Bundle> bundles
    
    BundleList( RepoTreeModel model, String title, List<P2Bundle> bundles ) {
        super( bundles.findAll { !it.isSourceBundle() && model.matches( "${it.id}" ) } )
        
        this.bundles = bundles
        this.model = model
        this.title = title
    }
    
    @Override
    public List createSwingChildren () {
        Map<String, P2Bundle> sourceBundles = [:]
        bundles.findAll {
            it.isSourceBundle()
        }.each {
            sourceBundles[ it.id.removeEnd( '.source' ) ] = it
        }
        
        return children.collect {
            def swing = new SwingBundle( model, it )
            swing.source = sourceBundles[ it.id ]
            return swing
        }.sort { it.toString() }
    }
    
    @Override
    String toString() {
        return "${children.size()} ${title}"
    }
    
    @Override
    String id() {
        return title
    }
}

class SwingRepo extends LazyNode {
    RepoTreeModel model
    IP2Repo repo
    String id
    
    SwingRepo( RepoTreeModel model, IP2Repo current, int index ) {
        super( current instanceof MergedP2Repo ? current.repos.size() : 5 )
        
        this.model = model
        this.repo = current
        
        this.id = current.url.toString()
    }
    
    @Override
    public List createSwingChildren ()
    {
        if( repo instanceof MergedP2Repo ) {
            int index = 0
            return repo.repos.collect { new SwingRepo( model, it, index ++ ) }.sort { it.toString() }
        }
        
        return [
            new BundleList( model, 'categories', repo.categories ),
            new BundleList( model, 'features', repo.features ),
            new BundleList( model, 'plugins', repo.plugins ),
            new UnitList( repo.units ),
            new OtherList( repo.others )
        ]
    }
    
    @Override
    String toString() {
        return repo.toString()
    }
    
    @Override
    String id() {
        return id
    }
}
