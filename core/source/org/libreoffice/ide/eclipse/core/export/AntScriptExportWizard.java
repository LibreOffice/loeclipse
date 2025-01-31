/*************************************************************************
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
package org.libreoffice.ide.eclipse.core.export;

import java.io.File;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
import org.libreoffice.ide.eclipse.core.Messages;

/**
 * Class for the new Ant Script Generation wizard.
 */
public class AntScriptExportWizard extends Wizard implements IExportWizard {

    private static final String ANT_EXPORT_SETTINGS_KEY = "oxt.export"; //$NON-NLS-1$

    private boolean mHasNewDialogSettings;

    private AntScriptExportWizardPage mAntScriptPage;

    /**
     * Constructor.
     */
    public AntScriptExportWizard() {
        IDialogSettings workbenchSettings = OOEclipsePlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(ANT_EXPORT_SETTINGS_KEY);
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
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // Try hard to find a selected UNO project
        IUnoidlProject prj = null;
        Iterator<?> it = selection.iterator();
        try {
            while (it.hasNext() && prj == null) {
                Object o = it.next();
                if (o instanceof IAdaptable) {
                    IResource res = ((IAdaptable) o).getAdapter(IResource.class);
                    if (res != null &&
                        res.getProject().hasNature(OOEclipsePlugin.UNO_NATURE_ID) &&
                        ProjectsManager.getProject(res.getProject().getName()).getLanguage() != null) {
                        // TODO: We need to check if the project is configured correctly and notify
                        prj = ProjectsManager.getProject(res.getProject().getName());
                    }
                }
            }
        } catch (CoreException e) { }
        setWindowTitle(Messages.getString("AntScriptExportWizard.DialogTitle")); //$NON-NLS-1$

        mAntScriptPage = new AntScriptExportWizardPage("page1", prj); //$NON-NLS-1$
        addPage(mAntScriptPage);

    }

    @Override
    public boolean performFinish() {
        boolean finished = false;
        String directory = mAntScriptPage.getPath();
        try {
            mAntScriptPage.createBuildScripts();

            mAntScriptPage.refreshProject();

            if (mHasNewDialogSettings) {
                IDialogSettings workbenchSettings = OOEclipsePlugin.getDefault().getDialogSettings();
                IDialogSettings section = workbenchSettings.getSection(ANT_EXPORT_SETTINGS_KEY);
                section = workbenchSettings.addNewSection(ANT_EXPORT_SETTINGS_KEY);
                setDialogSettings(section);
            }

        } catch (Exception e) {
            PluginLogger.error("The Ant Script couldn't be built", e);
        }

        File antFile = new File(directory + "/build.xml");
        finished = antFile.exists();

        return finished;
    }

}
