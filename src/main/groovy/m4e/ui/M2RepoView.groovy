/*******************************************************************************
 * Copyright (c) 26.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e.ui

import java.awt.Font;
import java.awt.Color;
import java.awt.BorderLayout as BL
import java.io.IOException;
import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowFilter.GeneralFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableRowSorter
import javax.swing.text.Style
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument
import org.codehaus.groovy.control.io.NullWriter;
import de.pdark.decentxml.Attribute;
import de.pdark.decentxml.Document;
import de.pdark.decentxml.Element
import de.pdark.decentxml.Text;
import de.pdark.decentxml.Node;
import de.pdark.decentxml.XMLWriter;
import m4e.MavenRepositoryTools;
import m4e.Pom;
import m4e.p2.ui.FilterChangeListener;

class M2RepoView {

    File repo
    
    JFrame mainFrame
    JTextPane pomView
    JTable artifactsWidget
    TableRowSorter sorter
    JTextField filterText = new JTextField()
    
    String title = 'Maven 2 Repository View'
    
    void show() {
        def swing = new SwingBuilder()
        
        swing.edt {
            mainFrame = frame( title: title, defaultCloseOperation: JFrame.EXIT_ON_CLOSE, size: [ 800, 800 ], show: true) {
                lookAndFeel( "system" )
                borderLayout()
                hbox( constraints: BL.NORTH ) {
                    label( text: 'Filter:', labelFor: filterText, displayedMnemonic: 'F' )
                    widget( filterText )
                    button( action: action( name: 'Reload', mnemonic: 'R', closure: { reload() } ) )
                }
                splitPane( orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 400, constraints: BL.CENTER ) {
                    scrollPane( constraints: 'top' ) {
                        artifactsWidget = table( fillsViewportHeight: true ) {
                            tableModel( list: [] ) {
                                propertyColumn( header: 'Group ID', propertyName: 'groupId' )
                                propertyColumn( header: 'Artifact ID', propertyName: 'artifactId' )
                                propertyColumn( header: 'Version', propertyName: 'version' )
                            }
                        }
                    }
                    scrollPane( constraints: 'bottom' ) {
                        pomView = textPane()
                    }
                }
            }
        }
        
        
        xmlContext.defineStyles( pomView.document )

        initTable()
        initFilter()        

        reload()
    }
    
    void clearText() {
        def doc = pomView.document
        doc.remove( 0, doc.length )
    }
    
    void initFilter() {
        def l = new FilterChangeListener() {
            void filterChanged( String value ) {
                applyFilter( value )
            }
        }
        filterText.document.addDocumentListener( l )
    }
    
    void initTable() {
        sorter = new TableRowSorter<DefaultTableModel>( artifactsWidget.model )
        artifactsWidget.rowSorter = sorter
        
        artifactsWidget.selectionMode = ListSelectionModel.SINGLE_SELECTION
        artifactsWidget.rowSelectionAllowed = true
        
        def l = { ListSelectionEvent e ->
            int[] selectedRows = artifactsWidget.selectedRows
            if( !selectedRows ) {
                return
            }
            
            int index = selectedRows[0]
            index = artifactsWidget.convertRowIndexToModel( index )
            PomTableEntry value = artifactsWidget.model.rows.get( index )
            selectPom( value.key() )
        } as ListSelectionListener
        artifactsWidget.selectionModel.addListSelectionListener( l )
    }
    
    void reload() {
        clearText()
        
        new SwingBuilder().build {
            doOutside {
                loadPoms()
            }
        }
    }
    
    void applyFilter( String value ) {
        sorter.rowFilter = new SubStringFilter( value )
    }
    
    XmlContext xmlContext = new XmlContext()
    
    String currentPom
    
    void selectPom( String key ) {
        
        if( key == currentPom ) {
            return
        }
        
        println "Loading ${key}"
        File file = MavenRepositoryTools.buildPath( repo, key, 'pom' )
        def pom = Pom.load( file )
        
        new XmlToStyledDocument( doc: pomView.document, context: xmlContext ).convert( pom.doc )
        
        pomView.caretPosition = 0
        currentPom = key
    }
    
    void loadPoms() {
        
        println "Loading artifacts from ${repo}..."
        
        currentPom = null
        
        def list = []
        
        MavenRepositoryTools.eachPom( repo ) { File file ->
            
            file = file.parentFile
            String version = file.name
            
            file = file.parentFile
            String artifactId = file.name
            
            file = file.parentFile
            def path = file.pathRelativeTo( repo )
            String groupId = path.replace( '/', '.' )
            
            list << new PomTableEntry( groupId: groupId, artifactId: artifactId, version: version )
        }
        
        list.sort { it.key() }
        
        println "Loaded ${list.size()} artifacts"
        
        new SwingBuilder().build {
            doLater {
                mainFrame.title = "${title} - ${list.size} artifacts"
                
                artifactsWidget.model.rowsModel.value = list
                artifactsWidget.model.fireTableDataChanged()
            }
        }
    }
}

class SubStringFilter extends RowFilter<Object,Object> {

    private String pattern
    
    SubStringFilter( String pattern ) {
        this.pattern = pattern
    }
    
    @Override
    public boolean include( javax.swing.RowFilter.Entry<? extends Object, ? extends Object> entry ) {
        return entry.getStringValue( 0 ).contains( pattern ) || entry.getStringValue( 1 ).contains( pattern )
    }
}

class PomTableEntry {
    String groupId
    String artifactId
    String version
    
    String key() {
        return "${groupId}:${artifactId}:${version}"
    }
}

class XmlToStyledDocument extends XMLWriter {
    StyledDocument doc
    XmlContext context
    int offset = 0
    
    XmlToStyledDocument() {
        super( new NullWriter() )
    }
    
    void convert( Document xml ) {
        doc.remove( 0, doc.length )
        xml.toXML( this )
    }
    
    @Override
    void write( Node node, String s ) throws IOException {
        append( node, s )
        offset += s.size()
    }
    
    void append( Element e, String s ) {
        doc.insertString( offset, s, context.STYLE_XML_ELEMENT )
    }
    
    void append( Text t, String s ) {
        doc.insertString( offset, s, context.STYLE_TEXT_NODE )
    }
    
    void append( Attribute t, String s ) {
        doc.insertString( offset, s, context.STYLE_XML_ATTRIBUTE )
    }
    
    void append( Object o, String s ) {
        doc.insertString( offset, s, context.STYLE_REGULAR )
    }
}

class XmlContext {
    
    Style STYLE_REGULAR
    Style STYLE_XML_ELEMENT
    Style STYLE_XML_ATTRIBUTE
    Style STYLE_TEXT_NODE
    
    void defineStyles( StyledDocument doc ) {
        
        def basic = StyleContext.defaultStyleContext.getStyle( StyleContext.DEFAULT_STYLE )
        
        STYLE_REGULAR = doc.addStyle( 'regular', basic )
        StyleConstants.setFontFamily( STYLE_REGULAR, Font.MONOSPACED )
        
        STYLE_XML_ELEMENT = doc.addStyle( 'xmlElement', STYLE_REGULAR )
        StyleConstants.setForeground( STYLE_XML_ELEMENT, new Color( 127, 0, 85 ) )
        
        STYLE_XML_ATTRIBUTE = doc.addStyle( 'xmlAttribute', STYLE_REGULAR )
        StyleConstants.setForeground( STYLE_XML_ATTRIBUTE, new Color( 63, 95, 191 ) )
        
        STYLE_TEXT_NODE = doc.addStyle( 'text', STYLE_REGULAR )
        StyleConstants.setBold( STYLE_TEXT_NODE, true )
    }

}
