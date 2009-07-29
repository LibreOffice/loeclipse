/**
 * 
 */
package org.openoffice.ide.eclipse.core.editors.main;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openoffice.ide.eclipse.core.editors.Messages;

/**
 * Section showing the compatibility parts of the description.xml file.
 * 
 * @author cbosdonnat
 *
 */
public class IntegrationSection extends SectionPart {

    static final String SEPARATOR = ","; //$NON-NLS-1$
    static final String[] PLATFORMS = {
        "all", //$NON-NLS-1$
        "freebsd_x86", //$NON-NLS-1$
        "freebsd_x86_64", //$NON-NLS-1$
        "linux_arm_eabi", //$NON-NLS-1$
        "linux_arm_oabi", //$NON-NLS-1$
        "linux_ia64", //$NON-NLS-1$
        "linux_mips_eb", //$NON-NLS-1$
        "linux_mips_el", //$NON-NLS-1$
        "linux_powerpc", //$NON-NLS-1$
        "linux_powerpc64", //$NON-NLS-1$
        "linux_s390", //$NON-NLS-1$
        "linux_s390x", //$NON-NLS-1$
        "linux_sparc", //$NON-NLS-1$
        "linux_x86", //$NON-NLS-1$
        "linux_x86_64", //$NON-NLS-1$
        "macosx_powerpc", //$NON-NLS-1$
        "macosx_x86", //$NON-NLS-1$
        "os2_x86", //$NON-NLS-1$
        "solaris_sparc", //$NON-NLS-1$
        "solaris_x86", //$NON-NLS-1$
        "windows_x86" //$NON-NLS-1$
    };
    private static final int GRID_COLUMS = 3;
    
    private PackageOverviewFormPage mPage;
    
    private Text mMinOOoTxt;
    private Text mMaxOOoTxt;
    private Text mPlatformTxt;
    
    /**
     * @param pParent the parent composite where to add the section
     * @param pPage the parent page
     */
    public IntegrationSection( Composite pParent, PackageOverviewFormPage pPage ) {
        super( pParent, pPage.getManagedForm().getToolkit(), Section.TITLE_BAR );
        mPage = pPage;
        
        createContent( );
    }

    /**
     * Creates the sections controls.
     */
    private void createContent() {
        Section section = getSection();
        section.setText( Messages.getString("IntegrationSection.Title") ); //$NON-NLS-1$
        
        section.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
        
        FormToolkit toolkit = mPage.getManagedForm().getToolkit();
        Composite clientArea = toolkit.createComposite(section);
        clientArea.setLayout( new GridLayout( GRID_COLUMS, false ) );
        
        Label descrLbl = toolkit.createLabel( clientArea, 
                "Define the dependencies of the extension: OpenOffice.org version and compatible system.", 
                SWT.WRAP );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = GRID_COLUMS;
        descrLbl.setLayoutData( gd );
        
        // Min OOo version controls
        toolkit.createLabel( clientArea, Messages.getString("IntegrationSection.MinOOoVersion") ); //$NON-NLS-1$
        mMinOOoTxt = toolkit.createText( clientArea, "" ); //$NON-NLS-1$
        gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = GRID_COLUMS - 1;
        mMinOOoTxt.setLayoutData( gd );
        
        // Max OOo version controls
        toolkit.createLabel( clientArea, Messages.getString("IntegrationSection.MaxOOoVersion") ); //$NON-NLS-1$
        mMaxOOoTxt = toolkit.createText( clientArea, "" ); //$NON-NLS-1$
        gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = GRID_COLUMS - 1;
        mMaxOOoTxt.setLayoutData( gd );
        
        // Platforms controls
        toolkit.createLabel( clientArea, Messages.getString("IntegrationSection.Platforms") ); //$NON-NLS-1$
        mPlatformTxt = toolkit.createText( clientArea, "all" ); //$NON-NLS-1$
        mPlatformTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        
        Button platformBtn = toolkit.createButton( clientArea, "...", SWT.PUSH | SWT.FLAT ); //$NON-NLS-1$
        platformBtn.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_END ) );
        platformBtn.addSelectionListener( new SelectionAdapter ( ) {
            
            @Override
            public void widgetSelected(SelectionEvent pE) {
                PlatformDialog dlg = new PlatformDialog( );
                if ( dlg.open() == PlatformDialog.OK ) {
                    mPlatformTxt.setText( dlg.getSelected( ) );
                }
            } 
        });
        
        toolkit.paintBordersFor( clientArea );
        
        section.setClient(clientArea);
    }
    
    /**
     * Dialog used to select platforms.
     * 
     * @author cbosdonnat
     *
     */
    private class PlatformDialog extends Dialog {
        
        private CheckboxTableViewer mList;
        private ArrayList<String> mSelected;
        
        /**
         * Dialog constructor.
         */
        public PlatformDialog ( ) {
            super( new Shell( Display.getDefault() ) );
            
            setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL);
            
            String textValue = mPlatformTxt.getText().replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
            String[ ] selection = textValue.split( SEPARATOR );
            mSelected = new ArrayList<String>( Arrays.asList( selection ) );
        }
        
        /**
         * @return the selected platforms in a comma-separated string.
         */
        public String getSelected( ) {
            String selection = ""; //$NON-NLS-1$
            for (String selected : mSelected) {
                selection += selected + SEPARATOR;
            }
            return selection.substring( 0, selection.length() - SEPARATOR.length() );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected Control createDialogArea(Composite pParent) {
            Composite body = (Composite)super.createDialogArea(pParent);
            body.setLayout( new GridLayout( ) );
            body.setLayoutData( new GridData( GridData.FILL_BOTH ) );
            
            Table table = new Table( body, SWT.MULTI | SWT.CHECK );
            mList = new CheckboxTableViewer( table );
            mList.setContentProvider( new ArrayContentProvider ( ) );
            mList.setLabelProvider( new LabelProvider ( ) );
            
            mList.setInput( PLATFORMS );
            mList.setCheckedElements( mSelected.toArray() );
            
            mList.addCheckStateListener( new ICheckStateListener ( ) {

                public void checkStateChanged(CheckStateChangedEvent pEvent) {
                    Object[] values = mList.getCheckedElements();
                    
                    mSelected.clear();
                    for (Object value : values) {
                        mSelected.add( value.toString() );
                    }
                };
            });
            
            return body;
        }
    }
}
