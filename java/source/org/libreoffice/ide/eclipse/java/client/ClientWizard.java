/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Novell, Inc.
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
 * Copyright: 2009 by Novell, Inc.
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.java.client;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.utils.WorkbenchHelper;
import org.libreoffice.ide.eclipse.java.OOoJavaPlugin;
import org.libreoffice.ide.eclipse.java.utils.TemplatesHelper;

/**
 * Class for the Java UNO client wizard.
 *
 *
 */
public class ClientWizard extends BasicNewResourceWizard {

    public static final String JODCONNECTOR_LIBNAME = "jodconnector.jar"; //$NON-NLS-1$
    private static final String DEST_PACKAGE = "org.libreoffice.client"; //$NON-NLS-1$
    private static final String CLIENT_CLASS = "UnoClient"; //$NON-NLS-1$
    private static final String LICENSE_DIR = "third-party licenses"; //$NON-NLS-1$
    private static final String[] LICENSE_FILES = new String[] {
        "license-jodconnector.txt", //$NON-NLS-1$
    };

    private IWorkbenchPage mActivePage;

    private NewJavaProjectWizardPageOne mFirstPage;
    private NewJavaProjectWizardPageTwo mThirdPage;
    private UnoConnectionPage mCnxPage;

    /**
     * Default constructor.
     */
    public ClientWizard() {
        super();
        setWindowTitle(Messages.getString("ClientWizard.Title")); //$NON-NLS-1$
        mActivePage = WorkbenchHelper.getActivePage();
    }

    @Override
    public boolean performCancel() {
        boolean ret = super.performCancel();
        mThirdPage.performCancel();
        return ret;
    }

    @Override
    public boolean performFinish() {
        boolean res = true;

        Job job = new Job(Messages.getString("ClientWizard.CreationJobTitle")) { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor pMonitor) {

                Status status = new Status(IStatus.OK, OOoJavaPlugin.PLUGIN_ID,
                    Messages.getString("ClientWizard.ProjectCreated")); //$NON-NLS-1$

                try {
                    mThirdPage.performFinish(pMonitor);
                    setupClientProject(mThirdPage.getJavaProject(), pMonitor);
                } catch (Exception e) {
                    PluginLogger.error(Messages.getString("ClientWizard.ProjectCreationError"), e); //$NON-NLS-1$
                    status = new Status(IStatus.ERROR, OOoJavaPlugin.PLUGIN_ID,
                        Messages.getString("ClientWizard.ProjectCreationError")); //$NON-NLS-1$
                }
                return status;
            }
        };

        job.schedule();

        return res;
    }

    /**
     * Configure the Java project in order to have a Java UNO client project.
     *
     * @param pJavaProject the Java project to configure
     * @param pMonitor progress monitor to update
     *
     * @throws Exception if anything wrong happens
     */
    protected void setupClientProject(IJavaProject pJavaProject, IProgressMonitor pMonitor) throws Exception {

        // Generate the sample classes in org.libreoffice.connection
        IProject prj = pJavaProject.getProject();

        IClasspathEntry[] srcEntries = mFirstPage.getSourceClasspathEntries();
        IFolder srcFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(srcEntries[0].getPath());
        IPath srcPath = srcFolder.getProjectRelativePath();

        String path = srcPath.append(DEST_PACKAGE.replace('.', '/')).toString();
        TemplatesHelper.copyTemplate(prj, CLIENT_CLASS + TemplatesHelper.JAVA_EXT,
            ClientWizard.class, path, DEST_PACKAGE, mCnxPage.getConnectionCode());

        // Copy the third-party licenses
        IFolder licensesFolder = prj.getFolder(LICENSE_DIR);
        licensesFolder.create(true, true, pMonitor);
        IPath licPath = licensesFolder.getProjectRelativePath();
        for (String license : LICENSE_FILES) {
            TemplatesHelper.copyTemplate(prj, license, ClientWizard.class, licPath.toString());
        }

        // Refresh the project
        try {
            prj.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (Exception e) {
        }

        // Show the Uno client class
        IFolder srcDir = prj.getFolder(path);
        IFile javaClientFile = srcDir.getFile(CLIENT_CLASS + ".java"); //$NON-NLS-1$
        selectAndReveal(javaClientFile);
        WorkbenchHelper.showFile(javaClientFile, mActivePage);
    }

    @Override
    public void addPages() {

        mCnxPage = new UnoConnectionPage();
        mFirstPage = new NewJavaProjectWizardPageOne();
        addPage(mFirstPage);

        addPage(mCnxPage);

        mThirdPage = new ClientWizardPageTwo(mFirstPage, mCnxPage);
        addPage(mThirdPage);
    }
}
