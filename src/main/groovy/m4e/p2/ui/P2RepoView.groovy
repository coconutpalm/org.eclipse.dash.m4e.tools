/*******************************************************************************
 * Copyright (c) 23.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e.p2.ui

import javax.swing.AbstractAction
import javax.swing.Action;
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JTextField;
import javax.swing.JTree
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel
import javax.xml.stream.events.StartDocument;

import java.awt.BorderLayout as BL
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.util.Enumeration;

import groovy.swing.SwingBuilder
import groovy.swing.impl.DefaultAction;
import m4e.p2.DependencySet;
import m4e.p2.IP2Repo
import m4e.p2.P2Bundle
import m4e.p2.P2Feature
import m4e.p2.P2Other
import m4e.p2.P2Plugin
import m4e.p2.P2Unit

class P2RepoView {
    
    private IP2Repo repo
    private File workDir
    
    P2RepoView( IP2Repo repo, File workDir ) {
        this.repo = repo
        this.workDir = workDir
    }
    
    JFrame mainFrame
    
    void show() {
        def swing = new SwingBuilder()
        
        def model = new RepoTreeModel( repo )
        JTree repoTree
        JTextField filter = new JTextField()
        
        swing.edt {
            mainFrame = frame( title: 'P2 Repository View', defaultCloseOperation: JFrame.EXIT_ON_CLOSE, size: [ 800, 800 ], show: true) {
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
        
        l = new PopupAdapter( workDir: workDir, mainFrame: mainFrame )
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
//        println node
        if( node instanceof LabelNode ) {
            def data = node.data
            println data // Print selection
            
            if( data instanceof P2Unit ) {
                println data.xml // Print selection
            } else if( data instanceof P2Other ) {
                println data.xml // Print selection
            }
        }
    }
}

class PopupAdapter extends MouseAdapter {
    
    File workDir
    JFrame mainFrame
    
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
        
//        println repo
        
        def result = []
        if( repo ) {
            result << new DownloadAction( repo, selection, workDir, true, false )
            
            if( selection.source ) {
                result << new DownloadAction( repo, selection, workDir, false, true )
                result << new DownloadAction( repo, selection, workDir, true, true )
            }
            
            result << new DownloadWithDependenciesAction( repo, bundle, workDir, mainFrame )
        }
        
        return result
    }

    List<Action> getActions( JTree tree, TreePath path, Object selection ) {
        return []
    }
}

class DownloadAction extends AbstractAction {
    
    IP2Repo repo
    SwingBundle bundle
    File workDir
    boolean downloadBundle
    boolean downloadSources
    
    DownloadAction( IP2Repo repo, SwingBundle bundle, File workDir, boolean downloadBundle, boolean downloadSources ) {
        super(
            downloadBundle ? (
                downloadSources ? 'Download with source' : 'Download this'
            ) : 'Download source for this' )
        
        this.repo = repo
        this.bundle = bundle
        this.workDir = workDir
        this.downloadBundle = downloadBundle
        this.downloadSources = downloadSources
    }

    void actionPerformed( ActionEvent e ) {
        def deps = new DependencySet( repo: repo )
        if( downloadBundle ) {
            deps.add( bundle.bundle )
        }
        if( downloadSources ) {
            deps.add( bundle.source )
        }
//        println deps.bundles
        
        Thread.start {
            deps.download( workDir )
        }
    }
}

class DownloadWithDependenciesAction extends AbstractAction {
    
    IP2Repo repo
    P2Bundle bundle
    File workDir
    JFrame mainFrame
    
    DownloadWithDependenciesAction( IP2Repo repo, P2Bundle bundle, File workDir, JFrame mainFrame ) {
        super( 'Download with all dependencies...' )
        
        this.repo = repo
        this.bundle = bundle
        this.workDir = workDir
        this.mainFrame = mainFrame
    }

    void actionPerformed( ActionEvent e ) {
        def deps = new DependencySet( repo: repo )
        
        SwingBuilder.build {
            doOutside {
                deps.resolveDependencies( bundle.id, bundle.version )
                
                doLater {
                    new DownloadDialog( mainFrame, deps, workDir )
                }
            }
        }
    }
}

// Based on code from http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
class DownloadDialog extends JDialog implements PropertyChangeListener {
    
    JOptionPane optionPane
    DependencySet deps
    File workDir
    
    public DownloadDialog( JFrame frame, DependencySet deps, File workDir ) {
        super( frame, false )
        resizable = true
        title = "Confirm Download of ${deps.size()} bundles"
        
        this.deps = deps
        this.workDir = workDir
        
        def swing = new SwingBuilder()
        
        def textPane = swing.textPane( contentType: 'text/html' )
        textPane.text = text() 
        
        def scrollPane = new JScrollPane( textPane )

        optionPane = new JOptionPane( scrollPane, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION )
        contentPane = optionPane
        
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener ( new WindowAdapter() {
            void windowClosing( WindowEvent e ) {
                optionPane.value = JOptionPane.CLOSED_OPTION
            }
        } )
        
        optionPane.addPropertyChangeListener( this )
        
        pack()
        visible = true
    }
    
    void propertyChange( PropertyChangeEvent e ) {
        if( !isVisible() ) {
            return
        }
        
        if( e.source != optionPane ) {
            return
        }
        
        String prop = e.propertyName
        if( JOptionPane.VALUE_PROPERTY != prop && JOptionPane.INPUT_VALUE_PROPERTY != prop ) {
            return
        }
        
        def value = optionPane.value
        if( JOptionPane.UNINITIALIZED_VALUE == value ) {
            return
        }
        
        optionPane.value = JOptionPane.UNINITIALIZED_VALUE
        
//        println value
        if( JOptionPane.OK_OPTION == value ) {
            startDownload()
        }
        
        close()
    }
    
    void close() {
        visible = false
    }
    
    void startDownload() {
        Thread.start {
            deps.download( workDir )
        }
    }
    
    String text() {
        def text = new StringBuilder()
        
        text << "You are about to download <b>${deps.size()}</b> bundles from <tt>${deps.repo.url}</tt> to <tt>${workDir.absolutePath}</tt>:<p>\n\n<table>\n"
        text << '<tr><th>Type</th><th>Name</th><th>Version</th><th>ID</th></tr>'
        List<String> bundles = deps.bundles.collect { toHTML( it ) }
        bundles.sort().each {
            text << '<tr>' << it << '</tr>'
        }
        text << '\n</table>'
        
        def unknownIds = deps.unknownIds as ArrayList
        if( unknownIds ) {
            text << "<p>\n\nThe following <b>${unknownIds.size()}</b> dependencies couldn't be resolved from this repo:<p>\n\n"
            unknownIds.sort().each {
                text << '<tt>' << it << '</tt><br/>'
            }
        }

        return text.toString()
    }
    
    String toHTML( P2Feature item ) {
        return "<td>Feature</td><td>${item.name}</td><td>${item.version}</td><td>${item.id}</td>"
    }
    
    String toHTML( P2Plugin item ) {
        return "<td>Plug-in</td><td>${item.name}</td><td>${item.version}</td><td>${item.id}</td>"
    }
    
    String toHTML( Object item ) {
        return item.toString()
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
        
//        println segments
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
                println "Unable to find ${Arrays.toList( segments )}" // Warning
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