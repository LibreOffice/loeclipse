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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.NullExtraOptionsProvider;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.openoffice.ide.eclipse.core.model.utils.SystemHelper;
import org.openoffice.ide.eclipse.core.utils.FilesFinder;
import org.openoffice.plugin.core.model.UnoPackage;

/**
 * OpenOffice launcher implementation.
 * 
 * @author cdan
 * 
 */
public class OfficeLaunchDelegate extends LaunchConfigurationDelegate {

    /**
     * Export the .oxt file, deploy it in openoffice, run openoffice.
     */
    private static final int TASK_UNITS = 3;

    /**
     * {@inheritDoc}
     */
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

            String prjName = pConfiguration.getAttribute(IOfficeLaunchConstants.PROJECT_NAME, ""); //$NON-NLS-1$
            boolean useCleanUserInstalation = pConfiguration.getAttribute(
                            IOfficeLaunchConstants.CLEAN_USER_INSTALLATION, false);

            IUnoidlProject prj = ProjectsManager.getProject(prjName);

            if (null != prj) {
                try {
                    IPath userInstallation = null;
                    if (useCleanUserInstalation) {
                        IFolder userInstallationFolder = prj.getOpenOfficeUserProfileFolder();
                        userInstallation = userInstallationFolder.getLocation();
                    }

                    File destFile = exportComponent(pMonitor, prj);
                    pMonitor.worked(1);

                    // Try to source ooenv if it exists
                    sourceOOEnv(prj);
                    
                    // Deploy the component
                    deployComponent(prj, userInstallation, destFile);
                    pMonitor.worked(1);

                    // Run an OpenOffice instance
                    if (ILaunchManager.DEBUG_MODE.equals(pMode)) {
                        prj.getLanguage().connectDebuggerToOpenOffice(prj, pLaunch, userInstallation, pMonitor);
                    } else {
                        prj.getOOo().runOpenOffice(prj, pLaunch, userInstallation, new NullExtraOptionsProvider(),
                                        pMonitor);
                    }
                    pMonitor.worked(1);
                } catch (Exception e) {
                    Display.getDefault().asyncExec(new Runnable() {

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
     * Tries to source ooenv if it exists and if we are on alinux OS.
     * 
     * @param pPrj the target project.
     * @throws IOException if we were unable to start the command
     */
    private void sourceOOEnv(IUnoidlProject pPrj) throws IOException {
        if (Platform.getOS().equals(Platform.OS_LINUX)) {
            IOOo oo = pPrj.getOOo();
            String home = oo.getHome();
            File homeFolder = new File(home);
            File programFolder = new File(homeFolder, "program");
            File oooenvFile = new File(programFolder, "ooenv");
            
            if (oooenvFile.isFile()) {
                String pathsep = System.getProperty("path.separator"); //$NON-NLS-1$
                String shellCommand = "source ooenv"; //$NON-NLS-1$
                String[] env = SystemHelper.getSystemEnvironement();
                env = SystemHelper.addEnv(env, "PATH", 
                                programFolder.getAbsolutePath(), pathsep); //$NON-NLS-1$
                
                PluginLogger.info("Sourcing: " + shellCommand);
                PluginLogger.info("Sourcing.env: " + Arrays.toString(env));
                
                Process process = SystemHelper.runTool(shellCommand, env, null);
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    PluginLogger.error("Interrupted while waiting for the source command to complete.", e);
                }
            }
        }
    }

    /**
     * Deploys the .oxt component in an OpenOffice installation.
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
     * Will bild and export the .oxt file.
     * 
     * @param pMonitor
     *            a monitor to report progress to.
     * @param pPrj
     *            te target project.
     * @return the file containing the .oxt file.
     * @throws Exception
     *             if something goes wrong.
     */
    private File exportComponent(IProgressMonitor pMonitor, IUnoidlProject pPrj) throws Exception {

        // TODO Repair this one!
//        ILanguageBuilder langBuilder = pPrj.getLanguage().getLanguageBuidler();
//        IPath libraryPath = langBuilder.createLibrary(pPrj);
//
//        IFolder distFolder = pPrj.getFolder(pPrj.getDistPath());
//
//        File destFile = distFolder.getFile(pPrj.getName() + ".oxt").getLocation().toFile();
//        UnoPackage pack = UnoidlProjectHelper.createMinimalUnoPackage(pPrj, destFile);
//        pack.addToClean(libraryPath);
//
//        // FIXME this code is duplicated.
//        IFile descrFile = pPrj.getFile(IUnoidlProject.DESCRIPTION_FILENAME);
//        if (descrFile.exists()) {
//            pack.addContent(descrFile);
//        }
//
//        // Select the XCU / XCS files by default
//        FilesFinder finder = new FilesFinder(
//                        new String[] { IUnoidlProject.XCU_EXTENSION, IUnoidlProject.XCS_EXTENSION });
//        finder.addExclude(pPrj.getDistFolder().getFullPath());
//        try {
//            ((UnoidlProject) pPrj).getProject().accept(finder);
//        } catch (CoreException e) {
//            // Nothing to log here
//        }
//        ArrayList<IFile> files = finder.getResults();
//        for (IFile aFile : files) {
//            pack.addContent(aFile);
//        }
//
//        pack.close(pMonitor);
//        return destFile;
        return null;
    }

}
