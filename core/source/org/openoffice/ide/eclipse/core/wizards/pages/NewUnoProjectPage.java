/*************************************************************************
 *
 * $RCSfile: NewUnoProjectPage.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:29 $
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
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
import org.openoffice.ide.eclipse.core.gui.rows.LabeledRow;
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
 * fields: SDK and OOo choices company prefix and Output path.
 * 
 * @author cedricbosdo
 *
 */
public class NewUnoProjectPage extends WizardNewProjectCreationPage 
                               implements IFieldChangedListener, IConfigListener {
    
    /* Constants defining the field properties used to react to field change events */
    private static final String PREFIX = "__prefix"; //$NON-NLS-1$
    private static final String OUTPUT_EXT = "__output_ext"; //$NON-NLS-1$
    private static final String SDK = "__sdk"; //$NON-NLS-1$
    private static final String OOO = "__ooo"; //$NON-NLS-1$
    private static final String LANGUAGE = "__language"; //$NON-NLS-1$
    
    private static final String CUSTOM_DIRS = "__custom_dirs"; //$NON-NLS-1$
    private static final String CUSTOM_SRC = "__custom_src"; //$NON-NLS-1$
    private static final String CUSTOM_IDL = "__custom_idl"; //$NON-NLS-1$
    private static final int TASK_UNITS = 2000;
    private static final int SUBTASK_UNIT = 1000;
    
    /**
     * Prefix field object.
     */
    private TextRow mPrefixRow;
    
    /**
     * Implementation extension field object.
     */
    private TextRow mOutputExt;
    
    /**
     * SDK used for the project selection row.
     */
    private ChoiceRow mSdkRow;
    
    /**
     * OOo used for the project selection row.
     */
    private ChoiceRow mOOoRow;
    
    /**
     * Programming language to use for code generation.
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
     * The list of the listened Text field of the super class.
     */
    private Vector<Text> mListenedTexts = new Vector<Text>();
    
    private IProject mNewProject;
    
    private IUnoidlProject mUnoProject = null;
    
    /**
     * Listener listening on the super class Text fields modifications.
     */
    private ModifyListener mModifListener = new ModifyListener() {

        public void modifyText(ModifyEvent pEvent) {
            checkWhiteSpaces();
            ((NewUnoProjectWizard)getWizard()).pageChanged(NewUnoProjectPage.this);
        }
    };
    
    /**
     * Default constructor.
     * 
     * @param pPageName the name of the wizard page
     */
    public NewUnoProjectPage(String pPageName) {
        super(pPageName);
        
        setTitle(Messages.getString("NewUnoProjectPage.Title")); //$NON-NLS-1$
        
        setDescription(Messages.getString("NewUnoProjectPage.Message")); //$NON-NLS-1$
        
        setImageDescriptor(OOEclipsePlugin.getImageDescriptor(
                ImagesConstants.NEWPROJECT_WIZ));
        
        OOoContainer.addListener(this);
        SDKContainer.addListener(this);
    }
    
    /**
     * {@inheritDoc}
     */
    public void dispose() {
        
        for (int i = 0, length = mListenedTexts.size(); i < length; i++) {
            Text field = mListenedTexts.get(i);
            if (!field.isDisposed()) {
                field.removeModifyListener(mModifListener);
            }
        }
        mListenedTexts.clear();
        
        super.dispose();
        
        OOoContainer.removeListener(this);
        SDKContainer.removeListener(this);
    }
    
    /**
     * @return company prefix entered
     */
    public String getPrefix() {
        String prefix = ""; //$NON-NLS-1$
        if (null != mPrefixRow) {
            prefix = mPrefixRow.getValue();
        }
        return prefix;
    }
    
    /**
     * @return output extension entered
     */
    public String getOutputExt() {
        String output = ""; //$NON-NLS-1$
        if (null != mOutputExt) {
            output = mOutputExt.getValue();
        }
        return output;
    }
    
    /**
     * @return SDK name selected
     */
    public String getSDKName() {
        String sdkName = ""; //$NON-NLS-1$
        if (null != mSdkRow) {
            sdkName = mSdkRow.getValue();
        }
        return sdkName;
    }
    
    /** 
     * @return OOo name selected
     */
    public String getOOoName() {
        String oooName = ""; //$NON-NLS-1$
        if (null != mOOoRow) {
            oooName = mOOoRow.getValue();
        }
        return oooName;
    }
    
    /**
     * @return the chosen implementation language.
     */
    public ILanguage getChosenLanguage() {
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
        if (mNewProject == null) {
            // get a project handle
            final IProject newProjectHandle = getProjectHandle();

            // get a project descriptor
            IPath location = null;
            if (!useDefaults()) {
                location = getLocationPath();
            }

            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            final IProjectDescription description = workspace.
            newProjectDescription(newProjectHandle.getName());
            description.setLocation(location);

            // create the new project operation
            WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
                protected void execute(IProgressMonitor pMonitor) throws CoreException {
                    createProject(description, newProjectHandle, pMonitor);
                }
            };

            // run the new project creation operation
            try {
                getContainer().run(true, true, op);
                mNewProject = newProjectHandle;
            } catch (InterruptedException e) {
                mNewProject = null;
            } catch (InvocationTargetException e) {
                // ie.- one of the steps resulted in a core exception
                Throwable t = e.getTargetException();
                PluginLogger.error(t.toString(), t);
                ErrorDialog.openError(getShell(), 
                        Messages.getString("NewUnoProjectPage.ProjectCreationError"), //$NON-NLS-1$
                        null, ((CoreException) t).getStatus());
                mNewProject = null;
            }
        }

        return mNewProject;
    }
    
    /**
     * Creates a project resource given the project handle and description.
     * 
     * @param pDescription
     *            the project description to create a project resource for
     * @param pProjectHandle
     *            the project handle to create a project resource for
     * @param pMonitor
     *            the progress monitor to show visual progress with
     * 
     * @exception CoreException
     *                if the operation fails
     * @exception OperationCanceledException
     *                if the operation is canceled
     */
    void createProject(IProjectDescription pDescription, IProject pProjectHandle,
            IProgressMonitor pMonitor) throws CoreException,
            OperationCanceledException {
        try {
            pMonitor.beginTask("", TASK_UNITS); //$NON-NLS-1$

            pProjectHandle.create(pDescription, new SubProgressMonitor(pMonitor, SUBTASK_UNIT));

            if (pMonitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            pProjectHandle.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(pMonitor, SUBTASK_UNIT));

        } finally {
            pMonitor.done();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void createControl(Composite pParent) {
        // Inherits the parents control
        super.createControl(pParent);
        
        Composite control = (Composite)getControl();
        
        // Listens to name and directory changes
        addTextListener(control);
        
        Composite body = new Composite(control, SWT.NONE);
        body.setLayout(new GridLayout(LabeledRow.LAYOUT_COLUMNS, false));
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
        mSdkRow.setBrowseSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent pEvent) {
                super.widgetSelected(pEvent);
                
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
        mOOoRow.setBrowseSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent pEvent) {
                super.widgetSelected(pEvent);
                
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
        for (int i = 0; i < languages.length; i++) {
            mLanguageRow.add(languages[i]);
        }
        mLanguageRow.select(0);
        mLanguageRow.setFieldChangedListener(this);
        
        
        addCustomDirsControls(body);
    }
    
    /**
     * Create the controls for the project directories customization.
     * 
     * @param pParent the parent composite where to create the controls. 
     */
    private void addCustomDirsControls(Composite pParent) {
        // Add the custom directories checkbox
        mCustomDirsRow = new BooleanRow(pParent, CUSTOM_DIRS, 
                "Use custom project directories");
        mCustomDirsRow.setFieldChangedListener(this);
        
        // Add the custom source directory chooser
        mSourceRow = new TextRow(pParent, CUSTOM_SRC, "Sources");
        mSourceRow.setValue(UnoidlProjectHelper.SOURCE_BASIS);
        mSourceRow.setEnabled(false);
        mSourceRow.setFieldChangedListener(this);
        
        // Add the custom idl directory chooser
        mIdlDirRow = new TextRow(pParent, CUSTOM_IDL, "IDL files");
        mIdlDirRow.setValue(UnoidlProjectHelper.IDL_BASIS);
        mIdlDirRow.setEnabled(false);
        mIdlDirRow.setFieldChangedListener(this);    
    }

    /**
     * Set the SDK names to the SDK list-box.
     */
    private void fillSDKRow () {
        
        if (null != mSdkRow) {
            // Adding the SDK names to the combo box 
            String[] sdks = new String[SDKContainer.getSDKCount()];
            Vector<String> sdkKeys = SDKContainer.getSDKKeys();
            for (int i = 0, length = SDKContainer.getSDKCount(); i < length; i++) {
                sdks[i] = sdkKeys.get(i);
            }
            
            mSdkRow.removeAll();
            mSdkRow.addAll(sdks);
            // The default SDK is randomly the first one
            mSdkRow.select(0);
        }
    }

    /**
     * Set the OOo names to the OOo list-box.
     */
    private void fillOOoRow() {
        
        if (null != mOOoRow) {
            
            // Adding the OOo names to the combo box 
            String[] ooos = new String[OOoContainer.getOOoCount()];
            Vector<String> oooKeys = OOoContainer.getOOoKeys();
            for (int i = 0, length = OOoContainer.getOOoCount(); i < length; i++) {
                ooos[i] = oooKeys.get(i);
            }
            
            mOOoRow.removeAll();
            mOOoRow.addAll(ooos);
            // The default OOo is randomly the first one
            mOOoRow.select(0);
        }
    }
    
    /**
     * Shows a warning if there are spaces in the project directory on Windows.
     */
    private void checkWhiteSpaces () {
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            if (getLocationPath().toOSString().contains(" ")) { //$NON-NLS-1$
                setMessage(Messages.getString("NewUnoProjectPage.WhiteSpacesWarning"), //$NON-NLS-1$
                    WARNING);
            }
        }
    }
    
    /**
     * Add the modify listener to all the Text children of the control.
     *  
     * @param pControl the control on which to add the listener.
     */
    private void addTextListener(Control pControl) {
        
        if (pControl instanceof Composite) {
            Control[] children = ((Composite)pControl).getChildren();
            for (int i = 0; i < children.length; i++) {
                Control child = children[i];
                addTextListener(child);
            }
        } else if (pControl instanceof Text) {
            Text text = (Text)pControl;
            if (!text.isDisposed()) {
                text.addModifyListener(mModifListener);
                mListenedTexts.add(text);
            }    
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void fieldChanged(FieldEvent pEvent) {
        
        setPageComplete(validatePage());

        // Check the prefix correctness
        if (pEvent.getProperty().equals(PREFIX)) {
            
            String newCompanyPrefix = pEvent.getValue();
            /**
             * <p>The company prefix is a package like name used by the project
             * to build the idl file path and the implementation path.</p>
             */
            if (!newCompanyPrefix.matches("([a-zA-Z][a-zA-Z0-9]*)(.[a-zA-Z][a-zA-Z0-9]*)*")) { //$NON-NLS-1$
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
        } else if (pEvent.getProperty().equals(OUTPUT_EXT)) {
            String newOuputExt = pEvent.getValue();
            /**
             * <p>The implementation extension is a single word which could 
             * contain numbers. It have to begin with a letter.</p> 
             */

            if (!newOuputExt.matches("[a-zA-Z][a-zA-Z0-9]*")) { //$NON-NLS-1$
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
        } else if (pEvent.getProperty().equals(CUSTOM_DIRS)) {
            boolean useCustom = mCustomDirsRow.getBooleanValue();
            mSourceRow.setEnabled(useCustom);
            mIdlDirRow.setEnabled(useCustom);
        }
        ((NewUnoProjectWizard)getWizard()).pageChanged(this);
    }
    
    /**
     * {@inheritDoc}
     */
    public void ConfigAdded(Object pElement) {
        if (pElement instanceof IOOo) {
            fillOOoRow();
        } else {
            fillSDKRow();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void ConfigRemoved(Object pElement) {
        
        if (null == pElement || pElement instanceof IOOo) {
            fillOOoRow();
        } 
        
        if (null == pElement || pElement instanceof ISdk) {
            fillSDKRow();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void ConfigUpdated(Object pElement) {
        if (pElement instanceof IOOo) {
            fillOOoRow();
        } else {
            fillSDKRow();
        }
    };
    
    /**
     * @param pData the data to fill.
     * @param pForce forces the project creation. Otherwise, the project handle won't be set
     * 
     * @return the given data with the completed properties, <code>null</code>
     *   if the provided data is <code>null</code>
     */
    public UnoFactoryData fillData(UnoFactoryData pData, boolean pForce) {
        
        if (pData != null) {
            if (pForce) {
                try {
                    pData.setProperty(IUnoFactoryConstants.PROJECT_HANDLE, createNewProject());
                } catch (Exception e) { }
            }
            

            pData.setProperty(IUnoFactoryConstants.PROJECT_PATH, getLocationPath());
            pData.setProperty(IUnoFactoryConstants.PROJECT_NAME, getProjectName());

            pData.setProperty(IUnoFactoryConstants.PROJECT_PREFIX, getPrefix());
            pData.setProperty(IUnoFactoryConstants.PROJECT_COMP, getOutputExt());
            pData.setProperty(IUnoFactoryConstants.PROJECT_LANGUAGE, getChosenLanguage());
            pData.setProperty(IUnoFactoryConstants.PROJECT_SDK, getSDKName());
            pData.setProperty(IUnoFactoryConstants.PROJECT_OOO, getOOoName());
            
            pData.setProperty(IUnoFactoryConstants.PROJECT_SRC_DIR, mSourceRow.getValue());
            pData.setProperty(IUnoFactoryConstants.PROJECT_IDL_DIR, mIdlDirRow.getValue());
        }
        
        return pData;
    }
    
    /**
     * @return the reference to the unoidl project
     */
    public IUnoidlProject getUnoidlProject() {
        return mUnoProject;
    }
    
    /**
     * Dialog for OOo and SDK configuration.
     * 
     * @author cedribosdo
     */
    private class TableDialog extends Dialog {
        
        private boolean mEditSdk = true;
        
        private Object mTable;
        
        /**
         * Constructor.
         * 
         * @param pParentShell the parent shell of the dialog.
         * @param pEditSDK <code>true</code> for SDK, <code>false</code> for OOo edition.
         */
        TableDialog (Shell pParentShell, boolean pEditSDK) {
            super(pParentShell);
            setShellStyle(getShellStyle() | SWT.RESIZE);
            mEditSdk = pEditSDK;
            
            // This dialog is a modal one
            setBlockOnOpen(true);
            if (pEditSDK) {
                setTitle(Messages.getString("NewUnoProjectPage.SdkBrowse")); //$NON-NLS-1$
            } else {
                setTitle(Messages.getString("NewUnoProjectPage.OOoBrowse")); //$NON-NLS-1$
            }
        }
        
        /**
         * {@inheritDoc}
         */
        protected Control createDialogArea(Composite pParent) {
            
            if (mEditSdk) {
                mTable = new SDKTable(pParent);
                ((SDKTable)mTable).getPreferences();
            } else {
                mTable = new OOoTable(pParent);
                ((OOoTable)mTable).getPreferences();
            }
                
            return pParent;
        }
        
        /**
         * {@inheritDoc}
         */
        protected void okPressed() {
            super.okPressed();
            
            if (mEditSdk) {
                ((SDKTable)mTable).savePreferences();
            } else {
                ((OOoTable)mTable).savePreferences();
            }
        }
    }
}
