/*************************************************************************
 *
 * $RCSfile: NewUnoProjectPage.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:48 $
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
package org.libreoffice.ide.eclipse.core.wizards.pages;

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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.gui.OOoConfigPanel;
import org.libreoffice.ide.eclipse.core.gui.rows.BooleanRow;
import org.libreoffice.ide.eclipse.core.gui.rows.ChoiceRow;
import org.libreoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.libreoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.libreoffice.ide.eclipse.core.gui.rows.LabeledRow;
import org.libreoffice.ide.eclipse.core.gui.rows.TextRow;
import org.libreoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.libreoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.UnoFactoryData;
import org.libreoffice.ide.eclipse.core.model.language.AbstractLanguage;
import org.libreoffice.ide.eclipse.core.model.language.LanguagesHelper;
import org.libreoffice.ide.eclipse.core.wizards.Messages;
import org.libreoffice.ide.eclipse.core.wizards.NewUnoProjectWizard;

/**
 * Uses the default Project wizard page and add some UNO-IDL special fields: SDK and OOo choices company prefix and
 * Output path.
 */
public class NewUnoProjectPage extends WizardNewProjectCreationPage implements IFieldChangedListener {

    /* Constants defining the field properties used to react to field change events */
    private static final String PREFIX = "__prefix"; //$NON-NLS-1$
    private static final String OUTPUT_EXT = "__output_ext"; //$NON-NLS-1$

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
     * Programming language to use for code generation.
     */
    private ChoiceRow mLanguageRow;

    /**
     * Checked to indicate the use of a custom project directory structure.
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

        @Override
        public void modifyText(ModifyEvent pEvent) {
            checkWhiteSpaces();
            checkWhiteSpacesinProjectName();
            ((NewUnoProjectWizard) getWizard()).pageChanged(NewUnoProjectPage.this);
        }
    };
    private OOoConfigPanel mOOoConfigPanel;

    /**
     * Default constructor.
     *
     * @param pPageName
     *            the name of the wizard page
     */
    public NewUnoProjectPage(String pPageName) {
        super(pPageName);

        setTitle(Messages.getString("NewUnoProjectPage.Title")); //$NON-NLS-1$

        setDescription(Messages.getString("NewUnoProjectPage.Message")); //$NON-NLS-1$

        setImageDescriptor(OOEclipsePlugin.getImageDescriptor(ImagesConstants.NEWPROJECT_WIZ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {

        for (int i = 0, length = mListenedTexts.size(); i < length; i++) {
            Text field = mListenedTexts.get(i);
            if (!field.isDisposed()) {
                field.removeModifyListener(mModifListener);
            }
        }
        mListenedTexts.clear();

        super.dispose();
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
     * @return the chosen implementation language.
     */
    public AbstractLanguage getChosenLanguage() {
        AbstractLanguage language = null;
        if (mLanguageRow != null) {
            String value = mLanguageRow.getValue();
            language = LanguagesHelper.getLanguageFromName(value);
        }
        return language;
    }

    /**
     * @return the selected OOo name
     */
    public String getOOoName() {
        return mOOoConfigPanel.getOOoName();
    }

    /**
     * Creates a new project resource with the selected name.
     * <p>
     * In normal usage, this method is invoked after the user has pressed Finish on the wizard; the enablement of the
     * Finish button implies that all controls on the pages currently contain valid values.
     * </p>
     * <p>
     * Note that this wizard caches the new project once it has been successfully created; subsequent invocations of
     * this method will answer the same project resource without attempting to create it again.
     * </p>
     *
     * @return the created project resource, or <code>null</code> if the project was not created
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
            final IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
            description.setLocation(location);

            // create the new project operation
            WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
                @Override
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
                ErrorDialog.openError(getShell(), Messages.getString("NewUnoProjectPage.ProjectCreationError"), //$NON-NLS-1$
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
    void createProject(IProjectDescription pDescription, IProject pProjectHandle, IProgressMonitor pMonitor)
        throws CoreException, OperationCanceledException {
        try {
            pMonitor.beginTask("", TASK_UNITS); //$NON-NLS-1$

            pProjectHandle.create(pDescription, SubMonitor.convert(pMonitor, SUBTASK_UNIT));

            if (pMonitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            pProjectHandle.open(IResource.BACKGROUND_REFRESH, SubMonitor.convert(pMonitor, SUBTASK_UNIT));

        } finally {
            pMonitor.done();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(Composite pParent) {
        // Inherits the parents control
        super.createControl(pParent);

        initializeDialogUnits(pParent);

        Composite control = (Composite) getControl();

        Composite body = new Composite(control, SWT.None);
        body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        body.setLayout(new GridLayout());

        // Listens to name and directory changes
        addTextListener(control);

        Group prjGroup = new Group(body, SWT.NONE);
        prjGroup.setText(Messages.getString("NewUnoProjectPage.UnoGroupTitle")); //$NON-NLS-1$
        prjGroup.setFont(pParent.getFont());
        prjGroup.setLayout(new GridLayout(LabeledRow.LAYOUT_COLUMNS, false));
        prjGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Add the company prefix field
        mPrefixRow = new TextRow(prjGroup, PREFIX, Messages.getString("NewUnoProjectPage.RootPackage")); //$NON-NLS-1$
        mPrefixRow.setValue("org.libreoffice.example"); // Setting default value //$NON-NLS-1$
        mPrefixRow.setFieldChangedListener(this);
        mPrefixRow.setTooltip(Messages.getString("NewUnoProjectPage.RootPackageTooltip")); //$NON-NLS-1$

        // Add the output directory field
        mOutputExt = new TextRow(prjGroup, OUTPUT_EXT, Messages.getString("NewUnoProjectPage.CompExtension")); //$NON-NLS-1$
        mOutputExt.setValue("comp"); // Setting default value //$NON-NLS-1$
        mOutputExt.setFieldChangedListener(this);
        mOutputExt.setTooltip(Messages.getString("NewUnoProjectPage.CompExtensionTooltip")); //$NON-NLS-1$

        // Adding the programming language row
        mLanguageRow = new ChoiceRow(prjGroup, LANGUAGE, Messages.getString("NewUnoProjectPage.Language"), null, false); //$NON-NLS-1$
        mLanguageRow.setTooltip(Messages.getString("NewUnoProjectPage.LanguageTooltip")); //$NON-NLS-1$

        // Sets the available programming languages
        String[] languages = LanguagesHelper.getAvailableLanguageNames();
        for (int i = 0; i < languages.length; i++) {
            mLanguageRow.add(languages[i]);
        }
        mLanguageRow.select(0);
        mLanguageRow.setFieldChangedListener(this);

        mOOoConfigPanel = new OOoConfigPanel(body);

        addCustomDirsControls(body);
    }

    /**
     * Create the controls for the project directories customization.
     *
     * @param pParent
     *            the parent composite where to create the controls.
     */
    private void addCustomDirsControls(Composite pParent) {

        Group group = new Group(pParent, SWT.NONE);
        group.setText(Messages.getString("NewUnoProjectPage.LayoutGroupTitle")); //$NON-NLS-1$
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        // Add the custom directories checkbox
        mCustomDirsRow = new BooleanRow(group, CUSTOM_DIRS, Messages.getString("NewUnoProjectPage.CustomDirsLabel")); //$NON-NLS-1$
        mCustomDirsRow.setFieldChangedListener(this);

        // Add the custom source directory chooser
        mSourceRow = new TextRow(group, CUSTOM_SRC, Messages.getString("NewUnoProjectPage.CustomSourcesLabel")); //$NON-NLS-1$
        mSourceRow.setValue(UnoidlProjectHelper.SOURCE_BASIS);
        mSourceRow.setEnabled(false);
        mSourceRow.setFieldChangedListener(this);

        // Add the custom idl directory chooser
        mIdlDirRow = new TextRow(group, CUSTOM_IDL, Messages.getString("NewUnoProjectPage.CustomIdlLabel")); //$NON-NLS-1$
        mIdlDirRow.setValue(UnoidlProjectHelper.IDL_BASIS);
        mIdlDirRow.setEnabled(false);
        mIdlDirRow.setFieldChangedListener(this);
    }

    /**
     * Shows a warning if there are spaces in the project directory on Windows.
     */
    private void checkWhiteSpaces() {
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            if (getLocationPath().toOSString().contains(" ")) { //$NON-NLS-1$
                setMessage(Messages.getString("NewUnoProjectPage.WhiteSpacesWarning"), //$NON-NLS-1$
                    WARNING);
            }
        }
    }

    /**
     * Shows a error if there are spaces in the project name.
     */
    private void checkWhiteSpacesinProjectName() {

        if (getProjectName().contains(" ")) { //$NON-NLS-1$
            setMessage(Messages.getString("NewUnoProjectPage.WhiteSpacesInProjectNameError"), //$NON-NLS-1$
                ERROR);
            setPageComplete(false);
        }

    }

    /**
     * Add the modify listener to all the Text children of the control.
     *
     * @param pControl
     *            the control on which to add the listener.
     */
    private void addTextListener(Control pControl) {

        if (pControl instanceof Composite) {
            Control[] children = ((Composite) pControl).getChildren();
            for (int i = 0; i < children.length; i++) {
                Control child = children[i];
                addTextListener(child);
            }
        } else if (pControl instanceof Text) {
            Text text = (Text) pControl;
            if (!text.isDisposed()) {
                text.addModifyListener(mModifListener);
                mListenedTexts.add(text);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fieldChanged(FieldEvent pEvent) {

        setPageComplete(validatePage());

        // Check the prefix correctness
        if (pEvent.getProperty().equals(PREFIX)) {

            String newCompanyPrefix = pEvent.getValue();
            /**
             * <p>
             * The company prefix is a package like name used by the project to build the idl file path and the
             * implementation path.
             * </p>
             */
            if (!newCompanyPrefix.matches("([a-zA-Z][a-zA-Z0-9]*)(.[a-zA-Z][a-zA-Z0-9]*)*")) { //$NON-NLS-1$
                /**
                 * <p>
                 * If the new company prefix is invalid, an error message is set.
                 * </p>
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
             * <p>
             * The implementation extension is a single word which could contain numbers. It have to begin with a
             * letter.
             * </p>
             */

            if (!newOuputExt.matches("[a-zA-Z][a-zA-Z0-9]*")) { //$NON-NLS-1$
                /**
                 * <p>
                 * If the new implementation extension is invalid, it is set to the empty string with an error message.
                 * </p>
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
        ((NewUnoProjectWizard) getWizard()).pageChanged(this);
    }

    /**
     * @param pData
     *            the data to fill.
     * @param pForce
     *            forces the project creation. Otherwise, the project handle won't be set
     *
     * @return the given data with the completed properties, <code>null</code> if the provided data is <code>null</code>
     */
    public UnoFactoryData fillData(UnoFactoryData pData, boolean pForce) {

        if (pData != null) {
            if (pForce) {
                try {
                    pData.setProperty(IUnoFactoryConstants.PROJECT_HANDLE, createNewProject());
                } catch (Exception e) {
                }
            }

            pData.setProperty(IUnoFactoryConstants.PROJECT_PATH, getLocationPath());
            pData.setProperty(IUnoFactoryConstants.PROJECT_NAME, getProjectName());

            pData.setProperty(IUnoFactoryConstants.PROJECT_PREFIX, getPrefix());
            pData.setProperty(IUnoFactoryConstants.PROJECT_COMP, getOutputExt());
            pData.setProperty(IUnoFactoryConstants.PROJECT_LANGUAGE, getChosenLanguage());
            pData.setProperty(IUnoFactoryConstants.PROJECT_SDK, mOOoConfigPanel.getSDKName());
            pData.setProperty(IUnoFactoryConstants.PROJECT_OOO, mOOoConfigPanel.getOOoName());

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
}
