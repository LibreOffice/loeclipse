/*************************************************************************
 *
 * $RCSfile: ResourceChangesHandler.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/02/04 18:16:32 $
 *
 * The Contents of this file are made available subject to the terms of 
 * the GNU Lesser General Public License Version 2.1
 *
 * Sun Microsystems Inc., October, 2000
 *
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2000 by Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, USA
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
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 *
 * Copyright: 2002 by Sun Microsystems, Inc.
 *
 * All Rights Reserved.
 *
 * Contributor(s): Cedric Bosdonnat
 *
 *
 ************************************************************************/
package org.openoffice.ide.eclipse.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * This class is responsible for all the actions to perform on resources changes.
 * To be sure that this class is started early even if the OOIntegration hasn't
 * been activated, this class implement {@link IStartup}
 * 
 * @author cedricbosdo
 *
 */
public class ResourceChangesHandler implements IStartup, IResourceChangeListener{


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		
		if (IResourceChangeEvent.POST_CHANGE == event.getType()){
			// Handle the addition of folders in a UNO-IDL capable folder

			// Extract all the additions among the changes
			IResourceDelta delta = event.getDelta();
			IResourceDelta[] added = delta.getAffectedChildren();

			// In all the added resources, process the projects
			for (int i=0, length=added.length; i<length; i++){
				IResourceDelta addedi = added[i];

				// Get the project
				IResource resource = addedi.getResource();
				IProject project = resource.getProject();

				if (ProjectsManager.getProject(project.getName()) == null && project.isOpen()) {
					ProjectAdderJob job = new ProjectAdderJob(project);
					job.schedule();
				}
				IUnoidlProject unoproject = ProjectsManager.getProject(project.getName());

				if (unoproject != null){
					UnoidlProjectHelper.setIdlProperty(unoproject);
				}
			}
		} else if (IResourceChangeEvent.PRE_DELETE == event.getType()) {
			// detect UNO IDL project about to be deleted
			IResource removed = event.getResource();
			if (ProjectsManager.getProject(removed.getName()) != null) {
				ProjectsManager.removeProject(removed.getName());
			}
		} else if (IResourceChangeEvent.PRE_CLOSE == event.getType()) {
			IResource res = event.getResource();
			if (res != null && ProjectsManager.getProject(res.getName()) != null) {
				// Project about to be closed: remove for the available uno projects
				ProjectsManager.removeProject(res.getName());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		// Load the projects manager
		ProjectsManager.load();
		
		// Add a listener to the resources changes of the workspace
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		PluginLogger.info("Resources changes are now listened"); //$NON-NLS-1$
	}

	private class ProjectAdderJob extends WorkspaceJob {
		
		private IProject mPrj;
		
		public ProjectAdderJob(IProject prj) {
			super("Project opener");
			mPrj = prj;
			//setRule(mPrj);
		}

		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
			ProjectsManager.addProject(mPrj);
			return Status.OK_STATUS;
		}
	}
}
