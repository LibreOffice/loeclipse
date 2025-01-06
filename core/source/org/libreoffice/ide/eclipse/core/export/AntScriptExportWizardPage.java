/*************************************************************************
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
package org.libreoffice.ide.eclipse.core.export;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.gui.PackageContentSelector;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.language.LanguageExportPart;
import org.libreoffice.ide.eclipse.core.Messages;
import org.libreoffice.plugin.core.model.UnoPackage;

public class AntScriptExportWizardPage extends WizardPage {

    private IUnoidlProject mSelectedProject;
    private LanguageExportPart mLangPart;
    private Combo mProjectsList;
    private boolean mCheckAntSectionDisplay = false;

    /**
     * Constructor.
     *
     * @param pageName
     *            the page name
     * @param project
     *            the project to export
     */
    public AntScriptExportWizardPage(String pageName, IUnoidlProject project) {
        super(pageName);

        setTitle(Messages.getString("AntScriptExportWizard.Title")); //$NON-NLS-1$
        setDescription(Messages.getString("AntScriptExportWizard.Description")); //$NON-NLS-1$

        mSelectedProject = project;
    }

    /**
     * Create the build scripts for the package model.
     *
     * @param model
     *            the model to be used to build script
     */
    public void createBuildScripts(UnoPackage model) {
        mLangPart.doFinish(model);
        mCheckAntSectionDisplay = false;
        ProjectExportPart.setCheckAntSectionDisplay(mCheckAntSectionDisplay);
    }

    /**
     * @return the UNO project to be used for building the ant script
     */
    public IUnoidlProject getProject() {
        return mSelectedProject;
    }

    /**
     * @param pProject
     *            the UNO project selected for the wizard.
     */
    public void setProject(IUnoidlProject pProject) {
        mSelectedProject = pProject;
        reloadLanguagePart();
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

        setPageComplete(checkPageCompletion());

        // Load the data into the fields
        loadData();
    }

    /**
     * Loads the data in the controls of the page.
     */
    private void loadData() {
        // Select the project
        String[] items = mProjectsList.getItems();
        int i = 0;
        while (mSelectedProject != null && i < items.length) {
            if (items[i].equals(mSelectedProject.getName())) {
                mProjectsList.select(i);
            }
            i++;
        }
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
        lbl.setText(Messages.getString("AntScriptExportWizard.Project")); //$NON-NLS-1$
        lbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        IUnoidlProject[] prjs = ProjectsManager.getProjects();
        ArrayList<String> tempPrjNames = new ArrayList<>();
        for (int i = 0; i < prjs.length; i++) {
            //The Dropdown for Ant Script should only have Java Uno Projects
            String language = prjs[i].getLanguage().getName();
            if (language.equalsIgnoreCase("Java") || language.equalsIgnoreCase("Python")) {
                IUnoidlProject prj = prjs[i];
                tempPrjNames.add(prj.getName());
            }
        }
        String[] prjNames = tempPrjNames.toArray(new String[tempPrjNames.size()]);

        mProjectsList = new Combo(selectionBody, SWT.DROP_DOWN | SWT.READ_ONLY);
        mProjectsList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mProjectsList.setItems(prjNames);

        mProjectsList.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                int id = mProjectsList.getSelectionIndex();
                if (id != -1) {
                    String name = mProjectsList.getItem(id);
                    IUnoidlProject unoprj = ProjectsManager.getProject(name);
                    mSelectedProject = unoprj;

                    // Change the project in the AntScriptExportPage
                    setProject(unoprj);
                }
                setPageComplete(checkPageCompletion());
            }
        });
    }

    /**
     * @return <code>true</code> if the page is complete, <code>false</code> otherwise.
     */
    private boolean checkPageCompletion() {
        return mProjectsList.getSelectionIndex() != -1;
    }

    /**
     * Change the language specific part from the selected project.
     */
    private void reloadLanguagePart() {

        // Add the language specific controls
        if (mSelectedProject != null) {
            mLangPart = mSelectedProject.getLanguage().getExportBuildPart();
            if (mLangPart != null) {
                // sLangPart.setPage(this); <- Can be used When the class LanguageExportPart is using
                // the Object Class rather than ManifestExportPage
                ProjectExportPart.setAntScriptExportPage(this);
                Composite body = (Composite) getControl();

                if (body != null) {
                    // The body can be null before the page creation
                    mLangPart.createControls(body);
                    body.layout();
                    mCheckAntSectionDisplay = true;
                    ProjectExportPart.setCheckAntSectionDisplay(mCheckAntSectionDisplay);
                }
            }
        }
    }

    /**
     * @param pTempPath
     *            the t.
     * @return the package model built from the data provided by the user or <code>null</code> if something blocked the
     *         process.
     */
    public UnoPackage getPackageModel(String pTempPath) {
        UnoPackage pack = null;

        try {
            File destFile = new File(pTempPath);
            pack = PackageContentSelector.createPackage(mSelectedProject, destFile, new ArrayList<Object>());
        } catch (Exception e) {
            PluginLogger.error(Messages.getString("AntScriptExportWizard.LibraryCreationError"), e); //$NON-NLS-1$
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
     * Returns the path of the selected Project.
     *
     * @return the path of the selected Project.
     */
    public String getPath() {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(mSelectedProject.getName());
        File dir = project.getFile("package.properties").getLocation().toFile().getParentFile();
        return dir.toString();
    }

}
