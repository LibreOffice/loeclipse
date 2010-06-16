/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Cédric Bosdonnat
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 * 
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2009 by Cédric Bosdonnat
 *
 * All Rights Reserved.
 * 
 ************************************************************************/
package org.openoffice.ide.eclipse.core.wizards.pages;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.dialogs.ResourceTreeAndListGroup;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.openoffice.ide.eclipse.core.model.pack.UnoPackage;
import org.openoffice.ide.eclipse.core.utils.FilesFinder;
import org.openoffice.ide.eclipse.core.wizards.Messages;

/**
 * First page of the new UNO extension export wizard.
 * 
 * @author Cédric Bosdonnat
 *
 */
@SuppressWarnings("restriction")
public class UnoPackageExportPage extends WizardPage {

    private static final int DESTINATION_PART_COLS = 3;

    private static final String OVERWRITE_FILES = "overwrite.files"; //$NON-NLS-1$
    private static final String AUTODEPLOY = "autodeploy"; //$NON-NLS-1$
    private static final String DESTINATION_HISTORY = "destination.history"; //$NON-NLS-1$
    
    private static final int MAX_DESTINATION_STORED = 5;

    private static final String XCS_EXTENSION = "xcs"; //$NON-NLS-1$
    private static final String XCU_EXTENSION = "xcu"; //$NON-NLS-1$
    
    private Combo mProjectsList;
    private ResourceTreeAndListGroup mResourceGroup;
    private Combo mDestinationCombo;
    private Button mOverwriteBox;
    private Button mAutodeployBox;
    
    private IUnoidlProject mSelectedProject;

    private ManifestExportPage mManifestPage;

    /**
     * Constructor.
     * 
     * @param pPageName the page id
     * @param pPrj the project to export
     * @param pManifestPage the manifest page of the wizard
     */
    public UnoPackageExportPage( String pPageName, IUnoidlProject pPrj, ManifestExportPage pManifestPage ) {
        super(pPageName);
        
        setTitle( Messages.getString("UnoPackageExportPage.Title") ); //$NON-NLS-1$
        setDescription( Messages.getString("UnoPackageExportPage.Description") ); //$NON-NLS-1$
        setImageDescriptor( OOEclipsePlugin.getImageDescriptor( ImagesConstants.PACKAGE_EXPORT_WIZ ) );
        
        mSelectedProject = pPrj;
        mManifestPage = pManifestPage;
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite pParent) {
        Composite body = new Composite( pParent, SWT.NONE );
        body.setLayout( new GridLayout( ) );
        body.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        setControl( body );
        
        createProjectSelection( );
        createResourcesGroup( );
        createDestinationGroup( );
        createOptionsGroup( );
        
        setPageComplete( checkPageCompletion() );
        
        // Load the data into the fields
        loadData( );
    }

    
    
    /**
     * Loads the data in the different controls of the page.
     */
    private void loadData() {
        // Select the project
        String[] items = mProjectsList.getItems();
        int i = 0;
        boolean selected = false;
        while ( mSelectedProject != null && i < items.length && !selected ) {
            if ( items[i].equals( mSelectedProject.getName() ) ) {
                mProjectsList.select( i );
                selected = true;
            }
            i++;
        }
        
        // Select the XCU / XCS files by default
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( mSelectedProject.getName() );
        FilesFinder finder = new FilesFinder( new String[] { XCU_EXTENSION, XCS_EXTENSION } );
        try {
            prj.accept( finder );
        } catch (CoreException e) {
            // Nothing to log here
        }
        
        ArrayList< IFile > files = finder.getResults();
        for (IFile file : files) {
            mResourceGroup.initialCheckListItem( file );
            mResourceGroup.initialCheckTreeItem( file );
        }
        
        restoreWidgetValues();
    }

    /**
     * Creates the project selection part of the dialog.
     */
    private void createProjectSelection() {
        Composite body = (Composite)getControl();
        Composite selectionBody = new Composite( body, SWT.NONE );
        selectionBody.setLayout( new GridLayout( 2, false ) );
        selectionBody.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
        
        Label lbl = new Label( selectionBody, SWT.NORMAL );
        lbl.setText( Messages.getString("UnoPackageExportPage.Project") ); //$NON-NLS-1$
        lbl.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
        
        IUnoidlProject[] prjs = ProjectsManager.getProjects();
        String[] prjNames = new String[prjs.length];
        for (int i = 0; i < prjs.length; i++) {
            IUnoidlProject prj = prjs[i];
            prjNames[i] = prj.getName();
        }
        
        mProjectsList = new Combo( selectionBody, SWT.DROP_DOWN | SWT.READ_ONLY );
        mProjectsList.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        mProjectsList.setItems( prjNames );
        
        mProjectsList.addModifyListener( new ModifyListener() {
            
            public void modifyText(ModifyEvent pE) {
                int id = mProjectsList.getSelectionIndex();
                IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( mSelectedProject.getName() );
                if ( id != -1 ) {
                    String name = mProjectsList.getItem( id );
                    IUnoidlProject unoprj = ProjectsManager.getProject( name );
                    mSelectedProject = unoprj;
                    
                    // Change the project in the manifest page
                    mManifestPage.setProject( unoprj );
                    
                    mResourceGroup.setRoot( prj );
                }
                
                setPageComplete( checkPageCompletion() );
            }
        });
    }

    /**
     * Creates the project's resources selection part of the dialog.
     */
    private void createResourcesGroup() {
        Composite body = (Composite)getControl();
        Composite selectionBody = new Composite( body, SWT.NONE );
        selectionBody.setLayout( new GridLayout( 2, false ) );
        selectionBody.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
        
        mResourceGroup = new ResourceTreeAndListGroup(selectionBody, new ArrayList<Object>(),
                getResourceProvider(IResource.FOLDER | IResource.FILE),
                WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(),
                getResourceProvider(IResource.FILE), WorkbenchLabelProvider
                        .getDecoratingWorkbenchLabelProvider(), SWT.NONE,
                DialogUtil.inRegularFontMode(selectionBody));
    }
    
    /**
     * Creates the package destination part of the dialog.
     */
    private void createDestinationGroup() {
        Composite body = (Composite)getControl();
        Composite groupBody = new Composite( body, SWT.NONE );
        groupBody.setLayout( new GridLayout( ) );
        groupBody.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
        
        Label titleLbl = new Label( groupBody, SWT.NONE );
        titleLbl.setText( Messages.getString("UnoPackageExportPage.SelectDestination") ); //$NON-NLS-1$
        titleLbl.setLayoutData( new GridData( SWT.BEGINNING, SWT.BEGINNING, false, false ) );
        
        Composite rowBody = new Composite( groupBody, SWT.NONE );
        rowBody.setLayout( new GridLayout( DESTINATION_PART_COLS, false ) );
        rowBody.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
        
        Label lbl = new Label( rowBody, SWT.None );
        lbl.setText( Messages.getString("UnoPackageExportPage.OxtFile") ); //$NON-NLS-1$
        lbl.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
        
        mDestinationCombo = new Combo( rowBody, SWT.DROP_DOWN );
        mDestinationCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        mDestinationCombo.addModifyListener( new ModifyListener() {
            
            public void modifyText(ModifyEvent pE) {
                setPageComplete( checkPageCompletion() );
            }
        });
        
        Button btn = new Button( rowBody, SWT.PUSH );
        btn.setText( Messages.getString("UnoPackageExportPage.Browse") ); //$NON-NLS-1$
        btn.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
        btn.addSelectionListener( new SelectionListener() {
            
            public void widgetSelected(SelectionEvent pE) {
                FileDialog dlg = new FileDialog( getShell(), SWT.SAVE );
                String path = dlg.open();
                if ( path != null ) {
                    mDestinationCombo.setText( path );
                }
            }
            
            public void widgetDefaultSelected(SelectionEvent pE) {
                widgetSelected( pE );
            }
        });
    }
    
    /**
     * Creates the options part of the dialog (the one at the bottom).
     */
    private void createOptionsGroup() {
        Composite body = (Composite)getControl();
        Composite groupBody = new Composite( body, SWT.NONE );
        groupBody.setLayout( new GridLayout( ) );
        groupBody.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
        
        Label titleLbl = new Label( groupBody, SWT.NONE );
        titleLbl.setText( Messages.getString("UnoPackageExportPage.Options") ); //$NON-NLS-1$
        titleLbl.setLayoutData( new GridData( SWT.BEGINNING, SWT.BEGINNING, false, false ) );
        
        Composite rowsBody = new Composite( groupBody, SWT.NONE );
        rowsBody.setLayout( new GridLayout( ) );
        rowsBody.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
        
        mOverwriteBox = new Button( rowsBody, SWT.CHECK );
        mOverwriteBox.setText( Messages.getString("UnoPackageExportPage.OverwriteWithoutWarning") ); //$NON-NLS-1$
        mOverwriteBox.setLayoutData( new GridData( SWT.BEGINNING, SWT.BEGINNING, false, false ) );
        
        mAutodeployBox = new Button( rowsBody, SWT.CHECK );
        mAutodeployBox.setText( Messages.getString("UnoPackageExportPage.AutoDeploy") ); //$NON-NLS-1$
        mAutodeployBox.setLayoutData( new GridData( SWT.BEGINNING, SWT.BEGINNING, false, false ) );
    }
    
    /**
     * @return <code>true</code> if the page is complete, <code>false</code> otherwise.
     */
    private boolean checkPageCompletion() {
        return !(0 == mDestinationCombo.getText().length()) && mProjectsList.getSelectionIndex() != -1;
    }
    
    /*
     * Data handling and filtering methods
     */
    
    /**
     * @param pRes the resource to be checked
     * 
     * @return <code>true</code> if the resource is hidden in the lists, <code>false</code>
     *      otherwise.
     */
    private boolean isHiddenResource( IResource pRes ) {
        boolean hidden = false;
        
        // Hide the binaries: they are always included from somewhere else
        IUnoidlProject unoprj = ProjectsManager.getProject( pRes.getProject().getName() );
        hidden |= unoprj.getFolder( unoprj.getBuildPath() ).equals( pRes );
        
        IFolder[] bins = unoprj.getBinFolders();
        for (IFolder bin : bins) {
            hidden |= bin.equals( pRes );
        }
        
        // Hide the hidden files
        hidden |= pRes.getName().startsWith( "." ); //$NON-NLS-1$
        
        // Hide files which are always included in the package
        hidden |= pRes.getName().equals( IUnoidlProject.DESCRIPTION_FILENAME);
        hidden |= pRes.getName().equals( "MANIFEST.MF" ); //$NON-NLS-1$
        hidden |= pRes.getName().equals( "manifest.xml" ); //$NON-NLS-1$
        hidden |= pRes.getName().equals( "types.rdb" ); //$NON-NLS-1$
        
        return hidden;
    }

    /**
     * @param pResourceType the type of the resources to return by the provider.
     * 
     * @return a content provider for <code>IResource</code>s that returns 
     * only children of the given resource type.
     */
    private ITreeContentProvider getResourceProvider( final int pResourceType ) {
        return new WorkbenchContentProvider() {
            public Object[] getChildren( Object pObject ) {
                ArrayList<IResource> results = new ArrayList<IResource>();
                
                if (pObject instanceof ArrayList<?>) {
                    ArrayList<?> objs = (ArrayList<?>)pObject;
                    for (Object o : objs) {
                        if ( o instanceof IResource ) {
                            results.add( ( IResource ) o );
                        }
                    }
                } else if (pObject instanceof IContainer) {
                    IResource[] members = null;
                    try {
                        members = ((IContainer) pObject).members();

                        //filter out the desired resource types
                        for (int i = 0; i < members.length; i++) {
                            //And the test bits with the resource types to see if they are what we want
                            if ((members[i].getType() & pResourceType) > 0 && !isHiddenResource( members[i] ) ) {
                                results.add(members[i]);
                            }
                        }
                    } catch (CoreException e) {
                    }
                }
                return results.toArray( );
            }
        };
    }

    /**
     * Stores the controls values for the next instance of the page.
     */
    public void saveWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if ( settings != null ) {
            settings.put( OVERWRITE_FILES, mOverwriteBox.getSelection() );
            settings.put( AUTODEPLOY, mAutodeployBox.getSelection() );
            
            String[] topItems = new String[ MAX_DESTINATION_STORED ];
            String firstItem = mDestinationCombo.getText().trim();
            topItems[0] = firstItem;
            int items_i = 0;
            int top_i = 0;
            int count = mDestinationCombo.getItemCount();
            while ( top_i < MAX_DESTINATION_STORED - 1 && items_i < count ) {
                String item = mDestinationCombo.getItem( items_i ).trim( );
                if ( mDestinationCombo.getSelectionIndex() != items_i ) {
                    topItems[ top_i + 1 ] = item;
                    top_i++;
                }
                items_i++;
            }
            settings.put( DESTINATION_HISTORY, topItems );
        }
    }
    
    /**
     * Loads the saved values of the controls states.
     */
    public void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if ( settings != null ) {
            mOverwriteBox.setSelection( settings.getBoolean( OVERWRITE_FILES ) );
            mAutodeployBox.setSelection( settings.getBoolean( AUTODEPLOY ) );
            String[] items = settings.getArray( DESTINATION_HISTORY );
            for (String item : items) {
                if ( item != null && !(0 == item.length()) ) {
                    mDestinationCombo.add( item );
                }
            }
        }
    }

    /**
     * @return the package model built from the data provided by the user or <code>null</code> if 
     *          something blocked the process.
     */
    public UnoPackage getPackageModel() {
        UnoPackage pack = null;
        
        try {
            // Test the existence of the destination: warning may be needed
            boolean doit = true;
            File destFile = new File( mDestinationCombo.getText() );
            if ( destFile.exists() && !mOverwriteBox.getSelection() ) {
                String msg = MessageFormat.format( 
                        Messages.getString("UnoPackageExportPage.OverwriteQuestion"), //$NON-NLS-1$ 
                        destFile.getPath() );
                doit = MessageDialog.openQuestion( getShell(), getTitle(), msg);
            }

            if ( doit ) {
                // Export the library
                IPath libraryPath = null;
                ILanguageBuilder langBuilder = mSelectedProject.getLanguage().getLanguageBuidler();
                libraryPath = langBuilder.createLibrary( mSelectedProject );

                // Create the package model
                pack = UnoidlProjectHelper.createMinimalUnoPackage( mSelectedProject, destFile );
                pack.addToClean( libraryPath );
                
                IFile descrFile = mSelectedProject.getFile( IUnoidlProject.DESCRIPTION_FILENAME );
                if ( descrFile.exists() ) {
                    pack.addContent( descrFile );
                }

                // Add the additional content to the package
                List<?> items = mResourceGroup.getAllWhiteCheckedItems();
                for (Object item : items) {
                    if ( item instanceof IResource ) {
                        IResource res = (IResource)item;
                        pack.addContent( res );
                    }
                }
                
                // Create the deployer instance
                if ( mAutodeployBox.getSelection() ) {
                    DeployerJob job = new DeployerJob( mSelectedProject.getOOo(), destFile );
                    pack.setDeployJob( job );
                }
            }
        } catch ( Exception e ) {
            PluginLogger.error( Messages.getString("UnoPackageExportPage.LibraryCreationError"), e ); //$NON-NLS-1$
        }
        
        return pack;
    }
    
    /**
     * Thread performing the package deployment into OpenOffice.org.
     * 
     * @author Cédric Bosdonnat
     *
     */
    class DeployerJob implements Runnable {
        
        private IOOo mOOo;
        private File mDest;
        
        /**
         * Constructor.
         * 
         * @param pOoo the OpenOffice.org where to deploy
         * @param pDest the package to deploy
         */
        DeployerJob(IOOo pOoo, File pDest) {
            mOOo = pOoo;
            mDest = pDest;
        }
        
        /**
         * {@inheritDoc}
         */
        public void run() {
            if (mOOo.canManagePackages()) {
                mOOo.updatePackage(mDest);
            }
        }
    }
}
