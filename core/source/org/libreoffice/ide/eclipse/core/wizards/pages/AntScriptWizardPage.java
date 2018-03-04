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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.language.LanguageExportPart;
import org.libreoffice.ide.eclipse.core.wizards.Messages;
import org.libreoffice.plugin.core.model.UnoPackage;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Combo;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.gui.PackageContentSelector;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;


/**
 * The page for the Ant Script Generation wizard.
 */
public class AntScriptWizardPage extends WizardPage {



	private IUnoidlProject mSelectedProject;
	private LanguageExportPart mLangPart;
	private Combo mProjectsList;
	private PackageContentSelector mContentSelector;
	private ManifestExportPage mManifestPage;
	
	private boolean checkAntSectionDisplay = false;

	/**
     * Constructor.
     *
     * @param pPageName
     *            the page name
     * @param pProject
     *            the project to export
     */
    public AntScriptWizardPage(String pPageName, IUnoidlProject pProject) {
        super(pPageName);

        setTitle(Messages.getString("AntScriptWizard.Title")); //$NON-NLS-1$
        setDescription(Messages.getString("AntScriptWizard.Description")); //$NON-NLS-1$

        mSelectedProject = pProject;
    }
    
    /**
     * Create the build scripts for the package model.
     *
     * @param pModel
     *            the model to export
     */
    public void createBuildScripts(UnoPackage pModel) {
        mLangPart.doFinish(pModel);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(Composite pParent) {
    	mManifestPage = new ManifestExportPage("No Page",mSelectedProject);

        Composite body = new Composite(pParent, SWT.NONE);
        body.setLayout(new GridLayout());
        body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(body);

        createProjectSelection();
        mContentSelector = new PackageContentSelector(body, SWT.NONE);
       

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

        if(selected) {
            mContentSelector.loadDefaults();
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
        lbl.setText(Messages.getString("AntScriptWizard.Project")); //$NON-NLS-1$
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
                    mManifestPage.setProject_Ant(unoprj);
                    mContentSelector.setProject(unoprj);
                    if(!checkAntSectionDisplay) {
                        reloadLanguagePart();
                    }
                }

                setPageComplete(checkPageCompletion());   
            }
        });
    }

    /**
     * @return <code>true</code> if the page is complete, <code>false</code> otherwise.
     */
    private boolean checkPageCompletion() {
        return (mProjectsList.getSelectionIndex() != -1); //<---- Can be Updated when the Ant script generation & input text fields are mentioned in same project   
    }

    /**
     * Change the language specific part from the selected project.
     */
    private void reloadLanguagePart() {
        if (mLangPart != null) {
            mLangPart.dispose();
        }

        // Add the language specific controls
        if (mSelectedProject != null) {
            mLangPart = mSelectedProject.getLanguage().getExportBuildPart();
            if (mLangPart != null) {
                mLangPart.setPage(mManifestPage);
                Composite body = (Composite) getControl();
                if (body != null && !checkAntSectionDisplay) {
                    // The body can be null before the page creation
                    mLangPart.createControls(body);
                    body.layout();
                    checkAntSectionDisplay = true;
                }
            }
        }
    }

    
    
    /**
     * @return the package model built from the data provided by the user or <code>null</code> if something blocked the
     *         process.
     */
    public UnoPackage getPackageModel(String tempPath) {
        UnoPackage pack = null;

        try {
            File destFile = new File(tempPath);
            pack = PackageContentSelector.createPackage(mSelectedProject, destFile, mContentSelector.getSelected());
       } catch (Exception e) {
            PluginLogger.error(Messages.getString("AntScriptWizard.LibraryCreationError"), e); //$NON-NLS-1$
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
     * Returns the path of the selected Project
     */
    public String getPath()
    {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(mSelectedProject.getName());
        File dir = project.getFile("build.xml").getLocation().toFile().getParentFile();
        return dir.toString();
    }
}
