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
package org.libreoffice.ide.eclipse.core.wizards.pages;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.builders.TypesBuilder;
import org.libreoffice.ide.eclipse.core.gui.PackageContentSelector;
import org.libreoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.wizards.Messages;
import org.libreoffice.plugin.core.model.UnoPackage;

/**
 * First page of the new UNO extension export wizard.
 */
public class UnoPackageExportPage extends WizardPage {

    private static final int DESTINATION_PART_COLS = 3;

    private static final String OVERWRITE_FILES = "overwrite.files"; //$NON-NLS-1$
    private static final String AUTODEPLOY = "autodeploy"; //$NON-NLS-1$
    private static final String DESTINATION_HISTORY = "destination.history"; //$NON-NLS-1$

    private static final int MAX_DESTINATION_STORED = 5;

    private Combo mProjectsList;
    private PackageContentSelector mContentSelector;
    private Combo mDestinationCombo;
    private Button mOverwriteBox;
    private Button mAutodeployBox;

    private IUnoidlProject mSelectedProject;

    private ManifestExportPage mManifestPage;

    /**
     * Constructor.
     *
     * @param pPageName
     *            the page id
     * @param pPrj
     *            the project to export
     * @param pManifestPage
     *            the manifest page of the wizard
     */
    public UnoPackageExportPage(String pPageName, IUnoidlProject pPrj, ManifestExportPage pManifestPage) {
        super(pPageName);

        setTitle(Messages.getString("UnoPackageExportPage.Title")); //$NON-NLS-1$
        setDescription(Messages.getString("UnoPackageExportPage.Description")); //$NON-NLS-1$
        setImageDescriptor(OOEclipsePlugin.getImageDescriptor(ImagesConstants.PACKAGE_EXPORT_WIZ));

        mSelectedProject = pPrj;
        mManifestPage = pManifestPage;
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

        createProjectSelection();
        mContentSelector = new PackageContentSelector(body, SWT.NONE);
        createDestinationGroup();
        createOptionsGroup();

        setPageComplete(checkPageCompletion());

        // Load the data into the fields
        loadData();
    }

    /**
     * Loads the data in the different controls of the page.
     */
    private void loadData() {
        // Select the project
        String[] items = mProjectsList.getItems();
        int i = 0;
        boolean selected = false;
        while (mSelectedProject != null && i < items.length && !selected) {
            if (items[i].equals(mSelectedProject.getName())) {
                mProjectsList.select(i);
                selected = true;
            }
            i++;
        }

        mContentSelector.loadDefaults();

        restoreWidgetValues();
    }

    /**
     * Creates the project selection part of the dialog.
     */
    private void createProjectSelection() {
        Composite body = (Composite) getControl();
        Composite selectionBody = new Composite(body, SWT.NONE);
        selectionBody.setLayout(new GridLayout(2, false));
        selectionBody.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        Label lbl = new Label(selectionBody, SWT.NORMAL);
        lbl.setText(Messages.getString("UnoPackageExportPage.Project")); //$NON-NLS-1$
        lbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        IUnoidlProject[] prjs = ProjectsManager.getProjects();
        String[] prjNames = new String[prjs.length];
        for (int i = 0; i < prjs.length; i++) {
            IUnoidlProject prj = prjs[i];
            prjNames[i] = prj.getName();
        }

        mProjectsList = new Combo(selectionBody, SWT.DROP_DOWN | SWT.READ_ONLY);
        mProjectsList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mProjectsList.setItems(prjNames);

        mProjectsList.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent pE) {
                int id = mProjectsList.getSelectionIndex();
                if (id != -1) {
                    String name = mProjectsList.getItem(id);
                    IUnoidlProject unoprj = ProjectsManager.getProject(name);
                    mSelectedProject = unoprj;

                    // Change the project in the manifest page
                    mManifestPage.setProject(unoprj);
                    mContentSelector.setProject(unoprj);
                }

                setPageComplete(checkPageCompletion());
            }
        });
    }

    /**
     * Creates the package destination part of the dialog.
     */
    private void createDestinationGroup() {
        Composite body = (Composite) getControl();
        Composite groupBody = new Composite(body, SWT.NONE);
        groupBody.setLayout(new GridLayout());
        groupBody.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        Label titleLbl = new Label(groupBody, SWT.NONE);
        titleLbl.setText(Messages.getString("UnoPackageExportPage.SelectDestination")); //$NON-NLS-1$
        titleLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

        Composite rowBody = new Composite(groupBody, SWT.NONE);
        rowBody.setLayout(new GridLayout(DESTINATION_PART_COLS, false));
        rowBody.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        Label lbl = new Label(rowBody, SWT.None);
        lbl.setText(Messages.getString("UnoPackageExportPage.OxtFile")); //$NON-NLS-1$
        lbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        mDestinationCombo = new Combo(rowBody, SWT.DROP_DOWN);
        mDestinationCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mDestinationCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent pE) {
                setPageComplete(checkPageCompletion());
            }
        });

        Button btn = new Button(rowBody, SWT.PUSH);
        btn.setText(Messages.getString("UnoPackageExportPage.Browse")); //$NON-NLS-1$
        btn.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        btn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent pE) {
                FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
                dlg.setFilterExtensions(new String[] { "*.oxt"});
                String path = dlg.open();
                if (path != null) {
                    if(!path.substring(path.length()-4).equalsIgnoreCase(".oxt")){
                        path += ".oxt";
                    }
                    mDestinationCombo.setText(path);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent pE) {
                widgetSelected(pE);
            }
        });
    }

    /**
     * Creates the options part of the dialog (the one at the bottom).
     */
    private void createOptionsGroup() {
        Composite body = (Composite) getControl();
        Composite groupBody = new Composite(body, SWT.NONE);
        groupBody.setLayout(new GridLayout());
        groupBody.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        Label titleLbl = new Label(groupBody, SWT.NONE);
        titleLbl.setText(Messages.getString("UnoPackageExportPage.Options")); //$NON-NLS-1$
        titleLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

        Composite rowsBody = new Composite(groupBody, SWT.NONE);
        rowsBody.setLayout(new GridLayout());
        rowsBody.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        mOverwriteBox = new Button(rowsBody, SWT.CHECK);
        mOverwriteBox.setText(Messages.getString("UnoPackageExportPage.OverwriteWithoutWarning")); //$NON-NLS-1$
        mOverwriteBox.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

        mAutodeployBox = new Button(rowsBody, SWT.CHECK);
        mAutodeployBox.setText(Messages.getString("UnoPackageExportPage.AutoDeploy")); //$NON-NLS-1$
        mAutodeployBox.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
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
     * Stores the controls values for the next instance of the page.
     */
    public void saveWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            settings.put(OVERWRITE_FILES, mOverwriteBox.getSelection());
            settings.put(AUTODEPLOY, mAutodeployBox.getSelection());

            String[] topItems = new String[MAX_DESTINATION_STORED];
            String firstItem = mDestinationCombo.getText().trim();
            topItems[0] = firstItem;
            int items_i = 0;
            int top_i = 0;
            int count = mDestinationCombo.getItemCount();
            while (top_i < MAX_DESTINATION_STORED - 1 && items_i < count) {
                String item = mDestinationCombo.getItem(items_i).trim();
                if (mDestinationCombo.getSelectionIndex() != items_i) {
                    topItems[top_i + 1] = item;
                    top_i++;
                }
                items_i++;
            }
            settings.put(DESTINATION_HISTORY, topItems);
        }
    }

    /**
     * Loads the saved values of the controls states.
     */
    public void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            mOverwriteBox.setSelection(settings.getBoolean(OVERWRITE_FILES));
            mAutodeployBox.setSelection(settings.getBoolean(AUTODEPLOY));
            String[] items = settings.getArray(DESTINATION_HISTORY);
            for (String item : items) {
                if (item != null && !(0 == item.length())) {
                    mDestinationCombo.add(item);
                }
            }
        }
    }

    /**
     * @return the package model built from the data provided by the user or <code>null</code> if something blocked the
     *         process.
     */
    public UnoPackage getPackageModel() {
        UnoPackage pack = null;

        try {
            // Test the existence of the destination: warning may be needed
            boolean doit = true;
            File destFile = new File(mDestinationCombo.getText());
            if (destFile.exists() && !mOverwriteBox.getSelection()) {
                String msg = MessageFormat.format(Messages.getString("UnoPackageExportPage.OverwriteQuestion"), //$NON-NLS-1$
                    destFile.getPath());
                doit = MessageDialog.openQuestion(getShell(), getTitle(), msg);
            }

            if (doit) {
                pack = PackageContentSelector.createPackage(mSelectedProject, destFile, mContentSelector.getSelected());

                // Run the deployer
                if (mAutodeployBox.getSelection()) {
                    DeployerJob job = new DeployerJob(mSelectedProject.getOOo(), destFile);

                    Display.getDefault().asyncExec(job);
                }
            }
        } catch (Exception e) {
            PluginLogger.error(Messages.getString("UnoPackageExportPage.LibraryCreationError"), e); //$NON-NLS-1$
        }

        return pack;
    }

    /**
     * Refresh the selected project.
     */
    public void refreshProject() {
        try {
            // Refresh the project and return the status
            String prjName = mSelectedProject.getName();
            IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(prjName);
            prj.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
        }
    }

    /**
     * Thread performing the package deployment into LibreOffice.
     */
    class DeployerJob implements Runnable {

        private IOOo mOOo;
        private File mDest;

        /**
         * Constructor.
         *
         * @param pOoo
         *            the LibreOffice where to deploy
         * @param pDest
         *            the package to deploy
         */
        DeployerJob(IOOo pOoo, File pDest) {
            mOOo = pOoo;
            mDest = pDest;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            if (mOOo.canManagePackages()) {
                mOOo.updatePackage(mDest, null);
            }
        }
    }

    /**
     * Force a build of the selected project.
     *
     * @throws Exception
     *             if the project couldn't be built.
     */
    public void forceBuild() throws Exception {
        String prjName = mSelectedProject.getName();
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(prjName);
        TypesBuilder.build(prj, null);
    }
}
