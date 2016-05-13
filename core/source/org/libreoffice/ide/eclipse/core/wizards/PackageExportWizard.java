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
import org.libreoffice.ide.eclipse.core.wizards.pages.ManifestExportPage;
import org.libreoffice.ide.eclipse.core.wizards.pages.UnoPackageExportPage;
import org.libreoffice.plugin.core.model.UnoPackage;

/**
 * Class for the new OXT export wizard.
*/
public class PackageExportWizard extends Wizard implements IExportWizard {

    private static final String DIALOG_SETTINGS_KEY = "oxt.export"; //$NON-NLS-1$

    private UnoPackageExportPage mMainPage;
    private ManifestExportPage mManifestPage;

    private boolean mHasNewDialogSettings;

    /**
     * Constructor.
     */
    public PackageExportWizard() {
        IDialogSettings workbenchSettings = OOEclipsePlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
        if (section == null) {
            mHasNewDialogSettings = true;
        } else {
            mHasNewDialogSettings = false;
            setDialogSettings(section);
        }
    }

    @Override
    public boolean performFinish() {
        boolean finished = false;
        UnoPackage model = mMainPage.getPackageModel();
        if (model != null) {
            try {
                // Force a build on the project
                mMainPage.forceBuild();

                // Configure the manifest.xml for the model
                mManifestPage.configureManifest(model);

                // TODO Run the next operations in a job

                // Write the build scripts if needed
                mManifestPage.createBuildScripts(model);

                // Export the package
                File out = model.close();
                finished = out != null;

                mMainPage.refreshProject();

                if (mHasNewDialogSettings) {
                    IDialogSettings workbenchSettings = OOEclipsePlugin.getDefault().getDialogSettings();
                    IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
                    section = workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
                    setDialogSettings(section);
                }

                mMainPage.saveWidgetValues();
            } catch (Exception e) {
                PluginLogger.error("Project couldn't be built", e);
            }
        }

        return finished;
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

        setWindowTitle(Messages.getString("PackageExportWizard2.DialogTitle")); //$NON-NLS-1$

        mManifestPage = new ManifestExportPage("page2", prj); //$NON-NLS-1$
        mMainPage = new UnoPackageExportPage("page1", prj, //$NON-NLS-1$
            mManifestPage);
        addPage(mMainPage);
        addPage(mManifestPage);
    }
}
