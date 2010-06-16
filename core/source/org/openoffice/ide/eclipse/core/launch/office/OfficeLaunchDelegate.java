package org.openoffice.ide.eclipse.core.launch.office;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;

public class OfficeLaunchDelegate extends LaunchConfigurationDelegate {

    /**
     * Export the .oxt file, deploy it in openoffice, run openoffice.
     */
    private static final int TASK_UNITS = 3;

    /**
     * {@inheritDoc}
     */
    public void launch(ILaunchConfiguration pConfiguration, String pMode,
            ILaunch pLaunch, IProgressMonitor pMonitor) throws CoreException {
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

            String prjName = pConfiguration.getAttribute(
                    IOfficeLaunchConstants.PROJECT_NAME, ""); //$NON-NLS-1$
            boolean useCleanUserInstalation = pConfiguration.getAttribute(
                    IOfficeLaunchConstants.CLEAN_USER_INSTALLATION, false);
            
            IUnoidlProject prj = ProjectsManager.getProject(prjName);

            if (null != prj) {
                try {
                    ILanguageBuilder langBuilder = prj.getLanguage()
                            .getLanguageBuidler();
                    langBuilder.createLibrary(prj);

                    // Run an OpenOffice instance
                    IPath userInstallation = null;
                    if (useCleanUserInstalation) {
                        IFolder userInstallationFolder = prj.getFolder(prj
                                .getOpenOfficeUserProfilePath());
                        //TODO find  better way to make sure the folder exists.
                        if (!userInstallationFolder.exists()) {
                            ((IFolder)userInstallationFolder.getParent()).create(true, true, null);
                            userInstallationFolder.create(true, true, null);
                        }
                        userInstallation = userInstallationFolder.getLocation();
                    }
                            
                    prj.getOOo().runOpenOffice(prj, pLaunch, userInstallation,
                            pMonitor);
                } catch (Exception e) {
                    Display.getDefault().asyncExec(new Runnable() {

                        public void run() {
                            MessageDialog.openError(Display.getDefault()
                                    .getActiveShell(), Messages.OfficeLaunchDelegate_LaunchErrorTitle, 
                                    Messages.OfficeLaunchDelegate_LaunchError);     
                        }
                    });
                }
            }
        } finally {
            pMonitor.done();
        }
    }

}
