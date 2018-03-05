/*************************************************************************
 *
 *$RCSfile: BuildScriptExportWizard.java,v $
 *
 * $Revision: 1.0 $
 *
 * last change: $Author: shobhanmandal $ $Date: 2018/03/03 18:36:29 $
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
package org.libreoffice.ide.eclipse.core.wizards;

import java.io.File;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.wizards.pages.BuildScriptExportWizardPage;
import org.libreoffice.plugin.core.model.UnoPackage;

/**
 * Class for the new Ant Script Generation wizard.
 */
public class BuildScriptExportWizard extends Wizard implements IExportWizard {

    private static final String DIALOG_SETTINGS_KEY = "oxt.export"; //$NON-NLS-1$

    private boolean mHasNewDialogSettings;

    private BuildScriptExportWizardPage mBuildScriptPage;

    /**
     * Constructor.
     */
    public BuildScriptExportWizard() {
        IDialogSettings workbenchSettings = OOEclipsePlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
        if (section == null) {
            mHasNewDialogSettings = true;
        } else {
            mHasNewDialogSettings = false;
            setDialogSettings(section);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IWorkbench pWorkbench, IStructuredSelection pSelection) {
        // Try hard to find a selected UNO project
        IUnoidlProject prj = null;
        Iterator<?> it = pSelection.iterator();
        while (it.hasNext() && prj == null) {
            Object o = it.next();
            if (o instanceof IAdaptable) {
                IResource res = ((IAdaptable) o).getAdapter(IResource.class);
                if (res != null) {
                    prj = ProjectsManager.getProject(res.getProject().getName());
                }
            }
        }
        setWindowTitle(Messages.getString("BuildScriptExportWizard.DialogTitle")); //$NON-NLS-1$

        mBuildScriptPage = new BuildScriptExportWizardPage("page1", prj); //$NON-NLS-1$
        addPage(mBuildScriptPage);

    }

    @Override
    public boolean performFinish() {
        boolean finished = false;
        String directory = mBuildScriptPage.getPath();
        String tempPath = directory + "/temporary/temp.oxt";
        UnoPackage model = mBuildScriptPage.getPackageModel(tempPath);
        if (model != null) {
            try {
                mBuildScriptPage.createBuildScripts(model);

                mBuildScriptPage.refreshProject();

                if (mHasNewDialogSettings) {
                    IDialogSettings workbenchSettings = OOEclipsePlugin.getDefault().getDialogSettings();
                    IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
                    section = workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
                    setDialogSettings(section);
                }

            } catch (Exception e) {
                PluginLogger.error("The Ant Script couldn't be built", e);
            }
        }

        File tmpDir = new File(directory + "/build.xml");
        finished = tmpDir.exists();

        return finished;
    }

}
