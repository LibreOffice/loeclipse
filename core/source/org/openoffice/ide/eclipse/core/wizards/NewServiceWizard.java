/*************************************************************************
 *
 * $RCSfile: NewServiceWizard.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/08/20 11:55:52 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
package org.openoffice.ide.eclipse.core.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.model.UnoFactory;
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;

public class NewServiceWizard extends BasicNewResourceWizard implements INewWizard {

	private NewServiceWizardPage mPage;
	private IWorkbenchPage mActivePage;
	
	public NewServiceWizard() {
		super();
		mActivePage = OOEclipsePlugin.getActivePage();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		
		Job serviceJob = new Job(Messages.getString("NewServiceWizard.JobName")) { //$NON-NLS-1$

			protected IStatus run(IProgressMonitor monitor) {
				
				IStatus status = new Status(IStatus.OK,
						OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
						IStatus.OK, "", null); //$NON-NLS-1$
				try {
					IUnoidlProject prj = mPage.mUnoProject;
					UnoFactoryData data = mPage.fillData(new UnoFactoryData());
					UnoFactory.createService(data, prj, mActivePage, monitor);
				
					// Releasing the data informations
					data.dispose();
				} catch (Exception e) {
					 status = new Status(IStatus.CANCEL,
								OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
								IStatus.OK, 
								Messages.getString("NewServiceWizard.CreateServiceError") , e); //$NON-NLS-1$
				}
				
				return status;
			}
			
		};
		
		serviceJob.setPriority(Job.INTERACTIVE);
		serviceJob.schedule();
		
		return true;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		
		super.init(workbench, selection);
		
		if (selection.getFirstElement() instanceof IAdaptable) {
			
			IAdaptable adapter = (IAdaptable)selection.getFirstElement();
			IResource resource = (IResource)adapter.getAdapter(IResource.class);
			
			if (resource != null) {
				createPages(resource.getProject());
			}
		}
	}
	
	private void createPages(IProject project){
		if (null != project){
			try {
				if (project.hasNature(OOEclipsePlugin.UNO_NATURE_ID)){
					UnoidlProject unoProject = (UnoidlProject)project.getNature(
							OOEclipsePlugin.UNO_NATURE_ID);
					
					mPage = new NewServiceWizardPage("newservice", unoProject); //$NON-NLS-1$
					
					addPage(mPage);
				}
			} catch (CoreException e){
				PluginLogger.debug(e.getMessage());
			}
		}
	}
}
