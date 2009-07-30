/**
 * 
 */
package org.openoffice.ide.eclipse.core.editors.main;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.model.description.DescriptionModel;

/**
 * Section showing the update-informations part of the description.xml file.
 * 
 * @author cbosdonnat
 *
 */
public class MirrorsSection extends SectionPart {

    private static final int COLUMN_WIDTH = 200;

    private PackageOverviewFormPage mPage;
    
    private DescriptionModel mModel;
    
    private TableViewer mTable;
    private Text mUrlTxt;
    private Button mAddBtn;
    private MenuItem mDeleteAction;
    
    /**
     * @param pParent the parent composite where to add the section
     * @param pPage the parent page
     */
    public MirrorsSection( Composite pParent, PackageOverviewFormPage pPage ) {
        super( pParent, pPage.getManagedForm().getToolkit(), Section.TITLE_BAR );
        mPage = pPage;
        
        createContent( );
        mModel = pPage.getModel();
        mTable.setInput( mModel.mUpdateInfos );
    }
    
    /**
     * Creates the sections controls.
     */
    private void createContent() {
        Section section = getSection();
        section.setText( "Update mirrors" ); //$NON-NLS-1$
        
        section.setLayoutData( new GridData( GridData.FILL_BOTH ));
        
        FormToolkit toolkit = mPage.getManagedForm().getToolkit();
        Composite clientArea = toolkit.createComposite(section);
        clientArea.setLayout( new GridLayout( 2, false ) );
        
        
        Label descrLbl = toolkit.createLabel( clientArea, "List the URLs of the update sites " +
                            "to use for this extension. The first one is the primary mirror.", 
                            SWT.WRAP);
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 2;
        descrLbl.setLayoutData( gd );
        
        // Create the list control
        createTable( clientArea );
        
        // Create the add controls
        Label addLbl = toolkit.createLabel( clientArea, "Specify a new mirror URL" );
        gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 2;
        addLbl.setLayoutData( gd );
        
        mUrlTxt = toolkit.createText( clientArea, new String( ) );
        mUrlTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        mUrlTxt.addModifyListener( new ModifyListener () {

            public void modifyText(ModifyEvent pE) {
                mAddBtn.setEnabled( !mUrlTxt.getText().trim().isEmpty() );
            }            
        });
        
        mAddBtn = toolkit.createButton( clientArea, "Add", SWT.PUSH );
        mAddBtn.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_END ) );
        mAddBtn.addSelectionListener( new SelectionAdapter( ) {
            @Override
            public void widgetSelected(SelectionEvent pE) {
                String text = mUrlTxt.getText();
                mModel.mUpdateInfos.add( text );
                mTable.add( text );
                mUrlTxt.setText( new String( ) );
            }
        } );
        
        toolkit.paintBordersFor( clientArea );
        section.setClient(clientArea);
    }
    
    /**
     * Create the URLs table control.
     * 
     * @param pParent the parent composite where to create the table.
     */
    private void createTable( Composite pParent ) {
        Table table = new Table( pParent, SWT.SINGLE | SWT.FULL_SELECTION );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.horizontalSpan = 2;
        table.setLayoutData( gd );
        mTable = new TableViewer( table );
        mTable.setContentProvider( new ArrayContentProvider( ) );
        mTable.setLabelProvider( new UrlLabelProvider( ) );
        mTable.setColumnProperties( new String[]{ "url" } ); //$NON-NLS-1$
        mTable.setCellEditors( new CellEditor[] {
            new TextCellEditor( table )
        });
        mTable.setCellModifier( new UrlCellModifier( ) );
        
        TableColumn column = new TableColumn( table, SWT.LEFT );
        column.setMoveable( false );
        column.setWidth( COLUMN_WIDTH );
        
        // Create the table context menu
        Menu menu = new Menu( table );
        mDeleteAction = new MenuItem( menu, SWT.PUSH );
        mDeleteAction.setText( "Remove" );
        mDeleteAction.setImage( OOEclipsePlugin.getImage( ImagesConstants.DELETE ) );
        mDeleteAction.setEnabled( false );
        mDeleteAction.addSelectionListener( new SelectionAdapter( ) {
            @Override
            public void widgetSelected(SelectionEvent pE) {
                IStructuredSelection sel = (IStructuredSelection)mTable.getSelection();
                Object selected = sel.getFirstElement();
                mTable.remove( selected );
                mModel.mUpdateInfos.remove( selected );
            } 
        });
        
        mTable.addSelectionChangedListener( new ISelectionChangedListener( ) {

            public void selectionChanged(SelectionChangedEvent pEvent) {
                mDeleteAction.setEnabled( !pEvent.getSelection().isEmpty() );
            }
        });
        
        table.setMenu( menu );
    }
    
    /**
     * Label provider for the urls table.
     * 
     * @author cbosdonnat
     *
     */
    private class UrlLabelProvider extends LabelProvider implements ITableLabelProvider {

        /**
         * {@inheritDoc}
         */
        public Image getColumnImage(Object pElement, int pColumnIndex) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public String getColumnText(Object pElement, int pColumnIndex) {
            return pElement.toString( );
        }
    }
    
    /**
     * Class allowing changes from the Urls table viewer on the model.
     * 
     * @author cbosdonnat
     *
     */
    private class UrlCellModifier implements ICellModifier {

        /**
         * {@inheritDoc}
         */
        public boolean canModify(Object pElement, String pProperty) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public Object getValue(Object pElement, String pProperty) {
            return pElement;
        }

        /**
         * {@inheritDoc}
         */
        public void modify(Object pElement, String pProperty, Object pValue) {
            if (pElement instanceof TableItem) {
                Object o = ((TableItem)pElement).getData();
                String oldValue = o.toString( );
                
                int pos = mModel.mUpdateInfos.indexOf( oldValue );
                mModel.mUpdateInfos.set( pos, pValue.toString() );
                mTable.replace( pValue, pos );
                mTable.refresh( o );
            }
        }
    }
}
