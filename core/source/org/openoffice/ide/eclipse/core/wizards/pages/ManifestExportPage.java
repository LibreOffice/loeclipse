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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.language.LanguageExportPart;
import org.openoffice.ide.eclipse.core.model.utils.SystemHelper;
import org.openoffice.ide.eclipse.core.wizards.Messages;
import org.openoffice.plugin.core.model.UnoPackage;

/**
 * Second page of the new OXT package export wizard.
 *
 * @author Cédric Bosdonnat
 *
 */
public class ManifestExportPage extends WizardPage {

    public static final int HORIZONTAL_INDENT = 20;

    public static final String MANIFEST_FILENAME = "manifest.xml"; //$NON-NLS-1$

    private static final int LAYOUT_COLS = 3;

    private IUnoidlProject mProject;
    private LanguageExportPart mLangPart;

    private Button mGenerateManifestBtn;
    private Button mReuseManifestBtn;

    private Button mSaveManifestBtn;
    private Label mSaveRowLbl;
    private Text mSaveRowTxt;
    private Button mSaveRowBtn;
    private Label mLoadRowLbl;
    private Text mLoadRowTxt;
    private Button mLoadRowBtn;

    private ManifestExportPageController mController;

    /**
     * Constructor.
     *
     * @param pPageName
     *            the page name
     * @param pProject
     *            the project to export
     */
    public ManifestExportPage(String pPageName, IUnoidlProject pProject) {
        super(pPageName);

        mController = new ManifestExportPageController();

        setTitle(Messages.getString("ManifestExportPage.Title")); //$NON-NLS-1$
        setDescription(Messages.getString("ManifestExportPage.Description")); //$NON-NLS-1$

        mProject = pProject;
    }

    /**
     * @param pProject
     *            the UNO project selected for the wizard.
     */
    public void setProject(IUnoidlProject pProject) {
        mProject = pProject;
        reloadLanguagePart();
    }

    /**
     * @return the UNO project to export as a package
     */
    public IUnoidlProject getProject() {
        return mProject;
    }

    /**
     * Set the proper manifest.xml file to the package model from the user selection.
     *
     * @param pModel
     *            the model to change
     */
    public void configureManifest(UnoPackage pModel) {
        IFile saveFile = getSaveManifestFile();
        if (saveFile != null) {
            pModel.setCopyManifestFileTo(SystemHelper.getFile(saveFile));
        }

        IFile readFile = getReadManifestFile();
        if (readFile != null) {
            pModel.setReadManifestFile(SystemHelper.getFile(readFile));
        }
    }

    /**
     * Create the build scripts for the package model if required by the user.
     *
     * @param pModel
     *            the model to export
     */
    public void createBuildScripts(UnoPackage pModel) {
        mLangPart.doFinish(pModel);
    }

    /**
     * Define the manifest file to generate and force the dialog to this value.
     *
     * @param pFile
     *            the file to set or <code>null</code> to remove the current existing value
     */
    public void setManifestPath(IFile pFile) {

        mController.setGenerateManifest(true);
        mController.setSaveManifest(true);
        mController.setSaveManifestPath(pFile.getFullPath().toString());

        updateControls();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(Composite pParent) {
        Composite body = new Composite(pParent, SWT.NONE);
        body.setLayout(new GridLayout());
        body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(body);

        // Add the controls
        Label title = new Label(body, SWT.NONE);
        title.setText(Messages.getString("ManifestExportPage.DefineManifestText")); //$NON-NLS-1$

        createOptionsGroup(body);

        // Add the language specific controls
        reloadLanguagePart();

        // Load the default values
        updateControls();
    }

    /**
     * Update the controls state and values according to the controller.
     */
    private void updateControls() {
        mGenerateManifestBtn.setSelection(mController.getGenerateManifest());

        mSaveManifestBtn.setSelection(mController.getSaveManifest());
        mSaveManifestBtn.setEnabled(mController.isSaveManifestEnabled());

        mSaveRowTxt.setText(mController.getSaveManifestPath());
        mSaveRowLbl.setEnabled(mController.isSaveManifestPathEnabled());
        mSaveRowTxt.setEnabled(mController.isSaveManifestPathEnabled());
        mSaveRowBtn.setEnabled(mController.isSaveManifestPathEnabled());

        mReuseManifestBtn.setSelection(!mController.getGenerateManifest());

        mLoadRowTxt.setText(mController.getLoadManifestPath());
        mLoadRowLbl.setEnabled(mController.isLoadManifestPathEnabled());
        mLoadRowTxt.setEnabled(mController.isLoadManifestPathEnabled());
        mLoadRowBtn.setEnabled(mController.isLoadManifestPathEnabled());
    }

    /**
     * Create the manifest save/reuse options.
     *
     * @param pParent
     *            the parent composite where to create the controls
     */
    private void createOptionsGroup(Composite pParent) {
        Composite body = new Composite(pParent, SWT.NONE);
        body.setLayout(new GridLayout());
        GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gd.horizontalIndent = HORIZONTAL_INDENT;
        body.setLayoutData(gd);

        mGenerateManifestBtn = new Button(body, SWT.RADIO);
        mGenerateManifestBtn.setText(Messages.getString("ManifestExportPage.GenerateManifestText")); //$NON-NLS-1$
        mGenerateManifestBtn.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        mGenerateManifestBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent pE) {
                mController.setGenerateManifest(mGenerateManifestBtn.getSelection());
                updateControls();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent pE) {
                widgetSelected(pE);
            }
        });

        // Create the controls for the manifest generation
        Composite saveOptions = new Composite(body, SWT.NONE);
        saveOptions.setLayout(new GridLayout(LAYOUT_COLS, false));
        gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gd.horizontalIndent = HORIZONTAL_INDENT;
        saveOptions.setLayoutData(gd);

        createManifestSaveOptions(saveOptions);

        mReuseManifestBtn = new Button(body, SWT.RADIO);
        mReuseManifestBtn.setText(Messages.getString("ManifestExportPage.UserManifestText")); //$NON-NLS-1$
        mReuseManifestBtn.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        mReuseManifestBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent pE) {
                mController.setGenerateManifest(!mReuseManifestBtn.getSelection());
                updateControls();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent pE) {
                widgetSelected(pE);
            }
        });

        // Create the controls for the manifest file selection
        Composite loadOptions = new Composite(body, SWT.NONE);
        loadOptions.setLayout(new GridLayout(LAYOUT_COLS, false));
        gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gd.horizontalIndent = HORIZONTAL_INDENT;
        loadOptions.setLayoutData(gd);

        createManifestLoadRow(loadOptions);
    }

    /**
     * Create the manifest file save row.
     *
     * @param pParent
     *            the parent composite where to create the controls
     */
    private void createManifestSaveOptions(Composite pParent) {
        mSaveManifestBtn = new Button(pParent, SWT.CHECK);
        mSaveManifestBtn.setText(Messages.getString("ManifestExportPage.SaveManifestText")); //$NON-NLS-1$

        GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gd.horizontalSpan = LAYOUT_COLS;
        mSaveManifestBtn.setLayoutData(gd);
        mSaveManifestBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent pE) {
                mController.setSaveManifest(mSaveManifestBtn.getSelection());
                updateControls();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent pE) {
                widgetSelected(pE);
            }
        });

        mSaveRowLbl = new Label(pParent, SWT.NONE);
        mSaveRowLbl.setText(Messages.getString("ManifestExportPage.SaveRowLabel")); //$NON-NLS-1$
        mSaveRowLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        mSaveRowTxt = new Text(pParent, SWT.SINGLE | SWT.BORDER);
        mSaveRowTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mSaveRowTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent pE) {
                mController.setSaveManifestPath(mSaveRowTxt.getText());
            }
        });

        mSaveRowBtn = new Button(pParent, SWT.PUSH);
        mSaveRowBtn.setText(Messages.getString("ManifestExportPage.Browse")); //$NON-NLS-1$
        mSaveRowBtn.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        mSaveRowBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent pE) {
                SaveAsDialog dlg = new SaveAsDialog(getShell());
                dlg.setOriginalName(MANIFEST_FILENAME);
                dlg.setOriginalFile(mProject.getFile(MANIFEST_FILENAME));
                dlg.create();
                dlg.getShell().setText(Messages.getString("ManifestExportPage.SaveDialogTitle")); //$NON-NLS-1$
                dlg.setMessage(Messages.getString("ManifestExportPage.SaveDialogMessage")); //$NON-NLS-1$

                if (dlg.open() == Window.OK) {
                    mController.setSaveManifestPath(dlg.getResult().toString());
                    updateControls();
                }
            }
        });
    }

    /**
     * Create the manifest file load row.
     *
     * @param pParent
     *            the parent composite where to create the controls
     */
    private void createManifestLoadRow(Composite pParent) {
        mLoadRowLbl = new Label(pParent, SWT.NONE);
        mLoadRowLbl.setText(Messages.getString("ManifestExportPage.LoadRowLabel")); //$NON-NLS-1$
        mLoadRowLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        mLoadRowTxt = new Text(pParent, SWT.SINGLE | SWT.BORDER);
        mLoadRowTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mLoadRowTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent pE) {
                mController.setLoadManifestPath(mLoadRowTxt.getText());
            }
        });

        mLoadRowBtn = new Button(pParent, SWT.PUSH);
        mLoadRowBtn.setText(Messages.getString("ManifestExportPage.Browse")); //$NON-NLS-1$
        mLoadRowBtn.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        mLoadRowBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent pE) {
                // Create a workspace file selection dialog
                ElementTreeSelectionDialog dlg = new ElementTreeSelectionDialog(getShell(),
                    new WorkbenchLabelProvider(), new WorkbenchContentProvider());
                dlg.setAllowMultiple(false);
                dlg.setValidator(new ISelectionStatusValidator() {

                    @Override
                    public IStatus validate(Object[] pSelection) {
                        Status status = new Status(IStatus.ERROR, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, new String());
                        // only single selection
                        if (pSelection.length == 1 && pSelection[0] instanceof IFile) {
                            status = new Status(IStatus.OK, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, new String());
                        }
                        return status;
                    }
                });

                dlg.addFilter(new ViewerFilter() {

                    @Override
                    public boolean select(Viewer pViewer, Object pParentElement, Object pElement) {
                        boolean select = true;
                        if (pElement instanceof IResource) {
                            IResource res = (IResource) pElement;
                            select &= !res.getName().startsWith("."); //$NON-NLS-1$
                            select &= res instanceof IContainer || res.getName().equals(MANIFEST_FILENAME);
                        }
                        return select;
                    }
                });
                dlg.setTitle(Messages.getString("ManifestExportPage.LoadDialogTitle")); //$NON-NLS-1$
                dlg.setMessage(Messages.getString("ManifestExportPage.LoadDialogMessage")); //$NON-NLS-1$
                dlg.setStatusLineAboveButtons(true);
                dlg.setInput(ResourcesPlugin.getWorkspace().getRoot());
                dlg.setInitialSelection(getProject().getFile(MANIFEST_FILENAME));
                if (dlg.open() == Window.OK) {
                    Object result = dlg.getFirstResult();
                    IFile file = (IFile) result;
                    mController.setLoadManifestPath(file.getFullPath().toString());
                    updateControls();
                }
            }
        });
    }

    /**
     * Change the language specific part from the selected project.
     */
    private void reloadLanguagePart() {
        if (mLangPart != null) {
            mLangPart.dispose();
        }

        // Add the language specific controls
        if (mProject != null) {
            mLangPart = mProject.getLanguage().getExportBuildPart();
            if (mLangPart != null) {
                mLangPart.setPage(this);
                Composite body = (Composite) getControl();
                if (body != null) {
                    // The body can be null before the page creation
                    mLangPart.createControls(body);
                    body.layout();
                }
            }
        }
    }

    /**
     * @return the selected file to reuse or <code>null</code>
     */
    private IFile getReadManifestFile() {
        IFile file = null;
        if (!mController.getGenerateManifest()) {
            IPath path = new Path(mController.getLoadManifestPath().trim());
            file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        }

        return file;
    }

    /**
     * @return the selected file to generate or <code>null</code>
     */
    private IFile getSaveManifestFile() {
        IFile file = null;
        if (mController.isSaveManifestPathEnabled()) {
            IPath path = new Path(mController.getSaveManifestPath().trim());
            file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        }
        return file;
    }
}
