/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2010 by Dan Corneanu
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
 * The Initial Developer of the Original Code is: Dan Corneanu.
 *
 * Copyright: 2010 by Dan Corneanu
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.openoffice.ide.eclipse.core.launch.office;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.builders.TypesBuilder;
import org.openoffice.ide.eclipse.core.gui.PackageContentSelector;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.NullExtraOptionsProvider;
import org.openoffice.plugin.core.model.UnoPackage;

/**
 * LibreOffice launcher implementation.
 *
 * @author cdan
 *
 */
public class OfficeLaunchDelegate extends LaunchConfigurationDelegate {

    /**
     * Export the .oxt file, deploy it in LibreOffice, run LibreOffice.
     */
    private static final int TASK_UNITS = 3;

    /**
     * {@inheritDoc}
     */
    @Override
    public void launch(ILaunchConfiguration pConfiguration, String pMode, ILaunch pLaunch, IProgressMonitor pMonitor)
                    throws CoreException {
        if (pMonitor == null) {
            pMonitor = new NullProgressMonitor();
        }

        try {
            pMonitor.beginTask(MessageFormat.format("{0}...", //$NON-NLS-1$
                            new Object[] { pConfiguration.getName() }), TASK_UNITS);
            // check for cancellation
            if (pMonitor.isCanceled()) {
                return;
            }

            String prjName = pConfiguration.getAttribute(IOfficeLaunchConstants.PROJECT_NAME, new String() );
            boolean useCleanUserInstalation = pConfiguration.getAttribute(
                            IOfficeLaunchConstants.CLEAN_USER_INSTALLATION, false);

            IUnoidlProject unoprj = ProjectsManager.getProject(prjName);

            if (null != unoprj) {
                try {
                    IPath userInstallation = null;
                    if (useCleanUserInstalation) {
                        IFolder userInstallationFolder = unoprj.getOfficeUserProfileFolder();
                        userInstallation = userInstallationFolder.getLocation();
                    }

                    // Force the build
                    IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( prjName );
                    TypesBuilder.build( prj, pMonitor );

                    List<IResource> resources = PackageConfigTab.getResources( pConfiguration );
                    File destFile = exportComponent( unoprj, resources );
                    pMonitor.worked(1);

                    // Deploy the component
                    deployComponent(unoprj, userInstallation, destFile);

                    //remove lock file not cleaned by unopkg gui
                    File lockFile = new File(userInstallation.toFile(), ".lock");
                    if (lockFile.exists()) {
                        lockFile.delete();
                    }
                    pMonitor.worked(1);

                    // Run an LibreOffice instance
                    if (ILaunchManager.DEBUG_MODE.equals(pMode)) {
                        unoprj.getLanguage().connectDebuggerToOffice(unoprj, pLaunch, userInstallation, pMonitor);
                    } else {
                        unoprj.getOOo().runOffice(unoprj, pLaunch, userInstallation, new NullExtraOptionsProvider(),
                                        pMonitor);
                    }
                    pMonitor.worked(1);
                } catch (Exception e) {
                    PluginLogger.error(Messages.OfficeLaunchDelegate_LaunchError, e);
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            MessageDialog.openError(Display.getDefault().getActiveShell(),
                                            Messages.OfficeLaunchDelegate_LaunchErrorTitle,
                                            Messages.OfficeLaunchDelegate_LaunchError);
                        }
                    });
                }
            }
        } finally {
            pMonitor.done();
        }
    }

    /**
     * Deploys the .oxt component in a LibreOffice installation.
     *
     * @param pPrj
     *            target project
     * @param pUserInstallation
     *            user profile to use
     * @param pOxtFile
     *            the .oxt file
     */
    private void deployComponent(IUnoidlProject pPrj, IPath pUserInstallation, File pOxtFile) {
        IOOo mOOo = pPrj.getOOo();
        if (mOOo.canManagePackages()) {
            mOOo.updatePackage(pOxtFile, pUserInstallation);
        }
    }

    /**
     * Will build and export the .oxt file.
     *
     * @param pPrj
     *            the target project.
     * @param pResources
     *            the resources to add to the package
     *
     * @return the file containing the .oxt file.
     * @throws Exception
     *             if something goes wrong.
     */
    private File exportComponent(IUnoidlProject pPrj, List<IResource> pResources) throws Exception {

        IFolder distFolder = pPrj.getFolder(pPrj.getDistPath());
        File destFile = distFolder.getFile(pPrj.getName() + ".oxt").getLocation().toFile();

        UnoPackage pack = PackageContentSelector.createPackage( pPrj, destFile, pResources );

        pack.close( );
        return destFile;
    }

}
