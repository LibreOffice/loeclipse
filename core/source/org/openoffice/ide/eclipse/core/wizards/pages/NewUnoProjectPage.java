/*************************************************************************
 *
 * $RCSfile: NewUnoProjectPage.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/10/11 18:06:17 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
 *
 * Sun Microsystems Inc., October, 2000
 *
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2000 by Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, USA
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
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 *
 * Copyright: 2002 by Sun Microsystems, Inc.
 *
 * All Rights Reserved.
 *
 * Contributor(s): Cedric Bosdonnat
 *
 *
 ************************************************************************/
package org.openoffice.ide.eclipse.core.wizards.pages;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.OOoTable;
import org.openoffice.ide.eclipse.core.gui.SDKTable;
import org.openoffice.ide.eclipse.core.gui.rows.BooleanRow;
import org.openoffice.ide.eclipse.core.gui.rows.ChoiceRow;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.core.gui.rows.TextRow;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.internal.helpers.LanguagesHelper;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.SDKContainer;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.model.language.ILanguage;
import org.openoffice.ide.eclipse.core.preferences.IConfigListener;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;
import org.openoffice.ide.eclipse.core.wizards.Messages;
import org.openoffice.ide.eclipse.core.wizards.NewUnoProjectWizard;

/**
 * Uses the default Project wizard page and add some UNO-IDL special
 * fields: SDK and OOo choices company prefix and Output path
 * 
 * @author cbosdonnat
 *
 */
public class NewUnoProjectPage extends WizardNewProjectCreationPage 
							   implements IFieldChangedListener, 
							      		  IConfigListener{
	
	/* Constants defining the field properties used to react to field change events */
	private static final String PREFIX = "__prefix"; //$NON-NLS-1$
	private static final String OUTPUT_EXT = "__output_ext"; //$NON-NLS-1$
	private static final String SDK = "__sdk"; //$NON-NLS-1$
	private static final String OOO = "__ooo"; //$NON-NLS-1$
	private static final String LANGUAGE = "__language"; //$NON-NLS-1$
	
	private static final String CUSTOM_DIRS = "__custom_dirs"; //$NON-NLS-1$
	private static final String CUSTOM_SRC = "__custom_src"; //$NON-NLS-1$
	private static final String CUSTOM_IDL = "__custom_idl"; //$NON-NLS-1$
	
	/**
	 * Prefix field object
	 */
	private TextRow mPrefixRow;
	
	/**
	 * Implementation extension field object
	 */
	private TextRow mOutputExt;
	
	/**
	 * SDK used for the project selection row
	 */
	private ChoiceRow mSdkRow;
	
	/**
	 * OOo used for the project selection row
	 */
	private ChoiceRow mOOoRow;
	
	/**
	 * Programming language to use for code generation 
	 */
	private ChoiceRow mLanguageRow;
	
	/**
	 * Checked to indicate the use of a custom project 
	 * directory structure.
	 */
	private BooleanRow mCustomDirsRow;
	
	private TextRow mSourceRow;
	
	private TextRow mIdlDirRow;
	
	/**
	 * Listener listening on the super class Text fields modifications
	 */
	private ModifyListener mModifListener = new ModifyListener() {

		public void modifyText(ModifyEvent e) {
			checkWhiteSpaces();
			((NewUnoProjectWizard)getWizard()).pageChanged(NewUnoProjectPage.this);
		}
	};
	
	/**
	 * The list of the listened Text field of the super class
	 */
	private Vector<Text> mListenedTexts = new Vector<Text>();
	
	private IProject newProject;
	
	/**
	 * Default constructor
	 */
	public NewUnoProjectPage(String pageName) {
		super(pageName);
		
		setTitle(Messages.getString("NewUnoProjectPage.Title")); //$NON-NLS-1$
		
		setDescription(Messages.getString("NewUnoProjectPage.Message")); //$NON-NLS-1$
		
		setImageDescriptor(OOEclipsePlugin.getImageDescriptor(
				ImagesConstants.NEWPROJECT_WIZ));
		
		OOoContainer.addListener(this);
		SDKContainer.addListener(this);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		
		for (int i=0, length=mListenedTexts.size(); i<length; i++) {
			Text field = mListenedTexts.get(i);
			if (!field.isDisposed()) field.removeModifyListener(mModifListener);
		}
		mListenedTexts.clear();
		
		super.dispose();
		
		OOoContainer.removeListener(this);
		SDKContainer.removeListener(this);
	}
	
	/**
	 * Returns the entered company prefix
	 * 
	 * @return company prefix entered
	 */
	public String getPrefix(){
		String prefix = ""; //$NON-NLS-1$
		if (null != mPrefixRow) {
			prefix = mPrefixRow.getValue();
		}
		return prefix;
	}
	
	/**
	 * Returns the entered ouput extension
	 * 
	 * @return ouput extension entered
	 */
	public String getOutputExt(){
		String output = ""; //$NON-NLS-1$
		if (null != mOutputExt) {
			output = mOutputExt.getValue();
		}
		return output;
	}
	
	/**
	 * Returns the selected SDK Name
	 * 
	 * @return SDK name selected
	 */
	public String getSDKName(){
		String sdkName = ""; //$NON-NLS-1$
		if (null != mSdkRow) {
			sdkName = mSdkRow.getValue();
		}
		return sdkName;
	}
	
	/**
	 * Returns the selected OOo Name
	 * 
	 * @return OOo name selected
	 */
	public String getOOoName(){
		String oooName = ""; //$NON-NLS-1$
		if (null != mOOoRow) {
			oooName = mOOoRow.getValue();
		}
		return oooName;
	}
	
	/**
	 * Returns the chosen implementation language
	 */
	public ILanguage getChosenLanguage(){
		ILanguage language = null;
		if (mLanguageRow != null) {
			String value = mLanguageRow.getValue();
			language = LanguagesHelper.getLanguageFromName(value);
		}
		return language;
	}
	
    /**
     * Creates a new project resource with the selected name.
     * <p>
     * In normal usage, this method is invoked after the user has pressed Finish
     * on the wizard; the enablement of the Finish button implies that all
     * controls on the pages currently contain valid values.
     * </p>
     * <p>
     * Note that this wizard caches the new project once it has been
     * successfully created; subsequent invocations of this method will answer
     * the same project resource without attempting to create it again.
     * </p>
     * 
     * @return the created project resource, or <code>null</code> if the
     *         project was not created
     */
    private IProject createNewProject() {
        if (newProject != null) {
			return newProject;
		}

        // get a project handle
        final IProject newProjectHandle = getProjectHandle();

        // get a project descriptor
        IPath location = null;
        if (!useDefaults()) {
        	location = getLocationPath();
        }
        
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProjectDescription description= workspace.
        	newProjectDescription(newProjectHandle.getName());
        description.setLocation(location);
        
        // create the new project operation
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor)
                    throws CoreException {
                createProject(description, newProjectHandle, monitor);
            }
        };

        // run the new project creation operation
        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            // ie.- one of the steps resulted in a core exception
            Throwable t = e.getTargetException();
            PluginLogger.error(t.toString(), t);
            ErrorDialog.openError(getShell(), 
            		Messages.getString("NewUnoProjectPage.ProjectCreationError"), //$NON-NLS-1$
            		null, // no special message
            		((CoreException) t).getStatus());
            return null;
        }

        newProject = newProjectHandle;

        return newProject;
    }
    
    /**
     * Creates a project resource given the project handle and description.
     * 
     * @param description
     *            the project description to create a project resource for
     * @param projectHandle
     *            the project handle to create a project resource for
     * @param monitor
     *            the progress monitor to show visual progress with
     * 
     * @exception CoreException
     *                if the operation fails
     * @exception OperationCanceledException
     *                if the operation is canceled
     */
    void createProject(IProjectDescription description, IProject projectHandle,
            IProgressMonitor monitor) throws CoreException,
            OperationCanceledException {
        try {
            monitor.beginTask("", 2000);//$NON-NLS-1$

            projectHandle.create(description, new SubProgressMonitor(monitor,
                    1000));

            if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

            projectHandle.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1000));

        } finally {
            monitor.done();
        }
    }
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		// Inherits the parents control
		super.createControl(parent);
		
		Composite control = (Composite)getControl();
		
		// Listens to name and directory changes
		addTextListener(control);
		
		Composite body = new Composite(control, SWT.NONE);
		body.setLayout(new GridLayout(3, false));
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Add the company prefix field
		mPrefixRow = new TextRow(body, PREFIX, 
						Messages.getString("NewUnoProjectPage.RootPackage")); //$NON-NLS-1$
		mPrefixRow.setValue("org.openoffice.example"); // Setting default value //$NON-NLS-1$
		mPrefixRow.setFieldChangedListener(this);
		mPrefixRow.setTooltip(Messages.getString("NewUnoProjectPage.RootPackageTooltip")); //$NON-NLS-1$
		
		// Add the output directory field
		mOutputExt = new TextRow(body, OUTPUT_EXT,
						Messages.getString("NewUnoProjectPage.CompExtension")); //$NON-NLS-1$
		mOutputExt.setValue("comp"); // Setting default value //$NON-NLS-1$
		mOutputExt.setFieldChangedListener(this);
		mOutputExt.setTooltip(Messages.getString("NewUnoProjectPage.CompExtensionTooltip")); //$NON-NLS-1$
		
		// Add the SDK choice field
		mSdkRow = new ChoiceRow(body, SDK,
						Messages.getString("NewUnoProjectPage.UsedSdk"), //$NON-NLS-1$
						Messages.getString("NewUnoProjectPage.SdkBrowse")); //$NON-NLS-1$
		mSdkRow.setFieldChangedListener(this);
		mSdkRow.setBrowseSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				
				// Open the SDK Configuration page
				TableDialog dialog = new TableDialog(getShell(), true);
				dialog.create();
				dialog.open();
				
			}
		});
		
		fillSDKRow();
		mSdkRow.setTooltip(Messages.getString("NewUnoProjectPage.SdkTooltip")); //$NON-NLS-1$
		
		
		// Add the OOo choice field
		mOOoRow = new ChoiceRow(body, OOO,
						Messages.getString("NewUnoProjectPage.UsedOOo"), //$NON-NLS-1$
						Messages.getString("NewUnoProjectPage.OOoBrowse")); //$NON-NLS-1$
		mOOoRow.setFieldChangedListener(this);
		mOOoRow.setBrowseSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				
				// Open the OOo Configuration page
				TableDialog dialog = new TableDialog(getShell(), false);
				dialog.create();
				dialog.open();
			}
		});
		
		fillOOoRow();
		mOOoRow.setTooltip(Messages.getString("NewUnoProjectPage.OOoTooltip")); //$NON-NLS-1$
		
		
		// Adding the programming language row 
		mLanguageRow = new ChoiceRow(body, LANGUAGE,
						Messages.getString("NewUnoProjectPage.Language")); //$NON-NLS-1$
		mLanguageRow.setTooltip(Messages.getString("NewUnoProjectPage.LanguageTooltip")); //$NON-NLS-1$
		
		// Sets the available programming languages
		String[] languages = LanguagesHelper.getAvailableLanguageNames();
		for (int i=0; i<languages.length; i++) {
			mLanguageRow.add(languages[i]);
		}
		mLanguageRow.select(0);
		mLanguageRow.setFieldChangedListener(this);
		
		
		// TODO add an horizontal separator
		
		// Add the custom directories checkbox
		mCustomDirsRow = new BooleanRow(body, CUSTOM_DIRS, 
				"Use custom project directories");
		mCustomDirsRow.setFieldChangedListener(this);
		
		// Add the custom source directory chooser
		mSourceRow = new TextRow(body, CUSTOM_SRC, "Sources");
		mSourceRow.setValue(UnoidlProjectHelper.SOURCE_BASIS);
		mSourceRow.setEnabled(false);
		mSourceRow.setFieldChangedListener(this);
		
		// Add the custom idl directory chooser
		mIdlDirRow = new TextRow(body, CUSTOM_IDL, "IDL files");
		mIdlDirRow.setValue(UnoidlProjectHelper.IDL_BASIS);
		mIdlDirRow.setEnabled(false);
		mIdlDirRow.setFieldChangedListener(this);
	}
	
	private void fillSDKRow (){
		
		if (null != mSdkRow){
			// Adding the SDK names to the combo box 
			String[] sdks = new String[SDKContainer.getSDKCount()];
			Vector<String> sdkKeys = SDKContainer.getSDKKeys();
			for (int i=0, length=SDKContainer.getSDKCount(); i<length; i++){
				sdks[i] = sdkKeys.get(i);
			}
			
			mSdkRow.removeAll();
			mSdkRow.addAll(sdks);
			mSdkRow.select(0);   // The default SDK is randomly the first one
		}
	}

	private void fillOOoRow(){
		
		if (null != mOOoRow){
			
			// Adding the OOo names to the combo box 
			String[] ooos = new String[OOoContainer.getOOoCount()];
			Vector<String> oooKeys = OOoContainer.getOOoKeys();
			for (int i=0, length=OOoContainer.getOOoCount(); i<length; i++){
				ooos[i] = oooKeys.get(i);
			}
			
			mOOoRow.removeAll();
			mOOoRow.addAll(ooos);
			mOOoRow.select(0);   // The default OOo is randomly the first one
		}
	}
	
	private void checkWhiteSpaces () {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			if (getLocationPath().toOSString().contains(" ")) { //$NON-NLS-1$
				setMessage(Messages.getString("NewUnoProjectPage.WhiteSpacesWarning"), //$NON-NLS-1$
					WARNING);
			}
		}
	}
	
	private void addTextListener(Control control) {
		
		if (control instanceof Composite) {
			Control[] children = ((Composite)control).getChildren();
			for (int i=0; i<children.length; i++) {
				Control child = children[i];
				addTextListener(child);
			}
		} else if (control instanceof Text) {
			Text text = (Text)control;
			if (!text.isDisposed()) {
				text.addModifyListener(mModifListener);
				mListenedTexts.add(text);
			}	
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.rows.IFieldChangedListener#fieldChanged(org.openoffice.ide.eclipse.gui.rows.FieldEvent)
	 */
	public void fieldChanged(FieldEvent e) {
		
		setPageComplete(validatePage());

		// Check the prefix correctness
		if (e.getProperty().equals(PREFIX)){
			
			String newCompanyPrefix = e.getValue();
			/**
			 * <p>The company prefix is a package like name used by the project
			 * to build the idl file path and the implementation path.</p>
			 */
			if (!newCompanyPrefix.matches(
			"([a-zA-Z][a-zA-Z0-9]*)(.[a-zA-Z][a-zA-Z0-9]*)*")){ //$NON-NLS-1$
				/**
				 * <p>If the new company prefix is invalid, an error message
				 * is set.</p>
				 */
				setErrorMessage(Messages.getString("NewUnoProjectPage.InvalidPrefixError")); //$NON-NLS-1$

				setPageComplete(false);
			} else {
				setErrorMessage(null);
				checkWhiteSpaces();
			}
		} else if (e.getProperty().equals(OUTPUT_EXT)){
			String newOuputExt = e.getValue();
			/**
			 * <p>The implementation extension is a single word which could 
			 * contain numbers. It have to begin with a letter.</p> 
			 */

			if (!newOuputExt.matches("[a-zA-Z][a-zA-Z0-9]*")){ //$NON-NLS-1$
				/**
				 * <p>If the new implementation extension is invalid, it is set to
				 * the empty string with an error message.</p>
				 */

				setErrorMessage(Messages.getString("NewUnoProjectPage.InvalidCompError")); //$NON-NLS-1$
				setPageComplete(false);
			} else {
				setErrorMessage(null);
				if (Platform.getOS().equals(Platform.OS_WIN32)) {
					setMessage(Messages.getString("NewUnoProjectPage.WhiteSpacesWarning"), //$NON-NLS-1$
							WARNING);
				}
			}
		} else if(e.getProperty().equals(CUSTOM_DIRS)) {
			boolean useCustom = mCustomDirsRow.getBooleanValue();
			mSourceRow.setEnabled(useCustom);
			mIdlDirRow.setEnabled(useCustom);
		}
		((NewUnoProjectWizard)getWizard()).pageChanged(this);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IConfigListener#ConfigAdded(java.lang.Object)
	 */
	public void ConfigAdded(Object element) {
		if (element instanceof IOOo){
			fillOOoRow();
		} else {
			fillSDKRow();
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IConfigListener#ConfigRemoved(java.lang.Object)
	 */
	public void ConfigRemoved(Object element) {
		
		if (null == element || element instanceof IOOo){
			fillOOoRow();
		} 
		
		if (null == element || element instanceof ISdk) {
			fillSDKRow();
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IConfigListener#ConfigUpdated(java.lang.Object)
	 */
	public void ConfigUpdated(Object element) {
		if (element instanceof IOOo){
			fillOOoRow();
		} else {
			fillSDKRow();
		}
	};
	
	/**
	 * @param force forces the project creation. 
	 * 	 Otherwise, project handle won't be set
	 * 
	 * @return the given data with the completed properties, <code>null</code>
	 *   if the provided data is <code>null</code>
	 */
	public UnoFactoryData fillData(UnoFactoryData data, boolean force) {
		
		if (data != null) {
			if (force) {
				try {
					data.setProperty(IUnoFactoryConstants.PROJECT_HANDLE, createNewProject());
				} catch (Exception e) { }
			}
			

			data.setProperty(IUnoFactoryConstants.PROJECT_PATH, getLocationPath());
			data.setProperty(IUnoFactoryConstants.PROJECT_NAME, getProjectName());

			data.setProperty(IUnoFactoryConstants.PROJECT_PREFIX, getPrefix());
			data.setProperty(IUnoFactoryConstants.PROJECT_COMP, getOutputExt());
			data.setProperty(IUnoFactoryConstants.PROJECT_LANGUAGE, getChosenLanguage());
			data.setProperty(IUnoFactoryConstants.PROJECT_SDK, getSDKName());
			data.setProperty(IUnoFactoryConstants.PROJECT_OOO, getOOoName());
			
			data.setProperty(IUnoFactoryConstants.PROJECT_SRC_DIR, mSourceRow.getValue());
			data.setProperty(IUnoFactoryConstants.PROJECT_IDL_DIR, mIdlDirRow.getValue());
		}
		
		return data;
	}
	
	private IUnoidlProject mUnoProject = null;
	
	/**
	 * Returns the reference to the unoidl project
	 * @return the underlying UnoIdl project
	 */
	public IUnoidlProject getUnoidlProject() {
		return mUnoProject;
	}
	
	/**
	 * Dialog for OOo and SDK configuration
	 * 
	 * @author cbosdonnat
	 */
	private class TableDialog extends Dialog {
		
		private boolean mEditSdk = true;
		
		private Object mTable;
		
		TableDialog (Shell parentShell, boolean editSDK){
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
			mEditSdk = editSDK;
			
			setBlockOnOpen(true); // This dialog is a modal one
			if (editSDK) {
				setTitle(Messages.getString("NewUnoProjectPage.SdkBrowse")); //$NON-NLS-1$
			} else {
				setTitle(Messages.getString("NewUnoProjectPage.OOoBrowse")); //$NON-NLS-1$
			}
		}
		
		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			
			if (mEditSdk){
				mTable = new SDKTable(parent);
				((SDKTable)mTable).getPreferences();
			} else {
				mTable = new OOoTable(parent);
				((OOoTable)mTable).getPreferences();
			}
				
			return parent;
		}
		
		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
		 */
		protected void okPressed() {
			super.okPressed();
			
			if (mEditSdk){
				((SDKTable)mTable).savePreferences();
			} else {
				((OOoTable)mTable).savePreferences();
			}
		}
	}
}
