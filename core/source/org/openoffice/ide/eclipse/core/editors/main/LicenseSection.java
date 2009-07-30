/**
 * 
 */
package org.openoffice.ide.eclipse.core.editors.main;

import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openoffice.ide.eclipse.core.gui.ProjectSelectionDialog;
import org.openoffice.ide.eclipse.core.model.description.DescriptionModel;

/**
 * License section class.
 * 
 * @author cbosdonnat
 *
 */
public class LicenseSection extends LocalizedSection {

    private static final int LAYOUT_COLS = 3;
    
    private Text mFileTxt;
    private Button mFileBrowseBtn;
    
    private Button mUserAcceptBtn;
    private Button mSuppressUpdateBtn;
    
    private DescriptionModel mModel;
    private IProject mProject;
    
    /**
     * @param pParent the parent composite where to add the section
     * @param pPage the parent page
     * @param pProject the project containing the description.xml file
     */
    public LicenseSection( Composite pParent, PackageOverviewFormPage pPage, IProject pProject ) {
        super( pParent, pPage, Section.TITLE_BAR );
        
        mProject = pProject;
        mModel = pPage.getModel();
        
        loadData( );
    }
   

    /**
     * Load the data from the model into the non-localized controls.
     */
    private void loadData() {
        mSuppressUpdateBtn.setSelection( mModel.mSuppressOnUpdate );
        mUserAcceptBtn.setSelection( mModel.mAcceptByUser );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void createControls(FormToolkit pToolkit, Composite pParent) {
        
        Section section = getSection();
        section.setLayoutData(new GridData( GridData.FILL_BOTH ));       
        section.setText( "License" );
        
        pParent.setLayout( new GridLayout( LAYOUT_COLS, false ) );
        
        // Create the checkboxes
        Label descrLbl = pToolkit.createLabel( pParent, 
                "License files are TXT files. They are localized and have to be selected here. If no file is selected for any locale, then no license will apply.", 
                SWT.WRAP );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = LAYOUT_COLS;
        descrLbl.setLayoutData( gd );

        
        createFileControls( pToolkit, pParent );
        
        mUserAcceptBtn = pToolkit.createButton( pParent, 
                "Every user have to accept the license or only the administrator.", 
                SWT.CHECK );
        gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = LAYOUT_COLS;
        mUserAcceptBtn.setLayoutData( gd );
        mUserAcceptBtn.addSelectionListener( new SelectionAdapter( ) {
            public void widgetSelected(SelectionEvent pE) {
                mModel.mAcceptByUser = mUserAcceptBtn.getSelection();
            } 
        });
        
        
        mSuppressUpdateBtn = pToolkit.createButton( pParent, 
                "Do not ask to accept the license during an update.", 
                SWT.CHECK );
        gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = LAYOUT_COLS;
        mSuppressUpdateBtn.setLayoutData( gd );
        mSuppressUpdateBtn.addSelectionListener( new SelectionAdapter( ) {
            public void widgetSelected(SelectionEvent pE) {
                mModel.mSuppressOnUpdate = mSuppressUpdateBtn.getSelection();
            } 
        });
    }

    /**
     * Create the file selection control.
     * 
     * @param pToolkit the toolkit used for the controls creation
     * @param pParent the parent composite where to create the controls
     */
    private void createFileControls(FormToolkit pToolkit, Composite pParent) {
        
        // Create the folder selection controls
        Label pathLbl = pToolkit.createLabel( pParent, "License file" );
        pathLbl.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING ) );
        
        mFileTxt = pToolkit.createText( pParent, new String( ) );
        mFileTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        
        mFileBrowseBtn = pToolkit.createButton( pParent, "...", SWT.PUSH );
        mFileBrowseBtn.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_END ) );
        mFileBrowseBtn.addSelectionListener( new SelectionAdapter( ) {
            @Override
            public void widgetSelected(SelectionEvent pE) {
                // Open the folder selection dialog
                ProjectSelectionDialog dlg = new ProjectSelectionDialog( mProject, 
                        "Choose the folder where to place the license files" );
                dlg.setShowOnlyFolders( true );
                
                if ( dlg.open() == ProjectSelectionDialog.OK ) {
                    IResource res = dlg.getSelected();
                    if ( res != null && res.getType() == IResource.FILE ) {
                        IFile file = (IFile)res;
                        String path = file.getProjectRelativePath().toString();
                        mFileTxt.setText( path );
                    }
                }
            } 
        });
    }

    /**
     * {@inheritDoc}
     */
    public void addLocale(Locale pLocale) {
        if ( !mModel.mLicenses.containsKey( pLocale ) ) {
            mModel.mLicenses.put( pLocale, new String( ) );
        }
        // enable the text and file
        mFileBrowseBtn.setEnabled( true );
        mFileTxt.setEnabled( true );
    }

    /**
     * {@inheritDoc}
     */
    public void deleteLocale(Locale pLocale) {
        mModel.mLicenses.remove( pLocale );
        if ( mModel.mLicenses.size() == 0 ) {
            // disable the text and file
            mFileBrowseBtn.setEnabled( false );
            mFileTxt.setEnabled( false );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void selectLocale(Locale pLocale) {
        if ( mCurrentLocale != null ) {
            mModel.mLicenses.put( mCurrentLocale, mFileTxt.getText( ) );
        }
        super.selectLocale(pLocale);
        String path = mModel.mLicenses.get( pLocale );
        mFileTxt.setText( path );
    }
}
