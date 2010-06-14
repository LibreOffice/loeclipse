package org.openoffice.ide.eclipse.core.launch.office;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

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
			IUnoidlProject prj = ProjectsManager.getProject(prjName);

			if (null != prj) {
				try {
					// ILanguageBuilder langBuilder = prj.getLanguage()
					// .getLanguageBuidler();
					// langBuilder.createLibrary(prj);

					// Run an OpenOffice instance
					prj.getOOo().runOpenOffice(prj, pLaunch, null, pMonitor);
				} catch (Exception e) {
					Display.getDefault().asyncExec(new Runnable() {

						public void run() {
							MessageDialog.openError(Display.getDefault()
									.getActiveShell(), "Error Title", //$NON-NLS-1$
									"Error Message"); //$NON-NLS-1$    
						}
					});
				}
			}
		} finally {
			pMonitor.done();
		}
	}

}
