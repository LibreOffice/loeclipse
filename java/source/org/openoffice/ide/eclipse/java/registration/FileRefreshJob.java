package org.openoffice.ide.eclipse.java.registration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;

/**
 * Job refreshing the RegistrationHandler.classes files.
 * 
 * @author cedricbosdo
 *
 */
public class FileRefreshJob extends Job {

	private IFile mToRefresh;
	
	/**
	 * Job constructor
	 *  
	 * @param toRefresh the file to refresh
	 */
	public FileRefreshJob(IFile toRefresh) {
		super("Registration Handler class refresh job");
		mToRefresh = toRefresh;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		try {
			mToRefresh.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (CoreException e) {
			status  = new Status(
    				Status.WARNING, 
    				OOEclipsePlugin.getDefault().getBundle().getSymbolicName(),
    				Status.WARNING,
    				"Can't refresh the registration classes file",
    				e);
		}
		return status;
	}
}
