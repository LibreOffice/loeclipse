/*************************************************************************
 *
 * $RCSfile: NewInterfaceWizard.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/07/17 21:01:01 $
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.model.UnoFactory;
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.wizards.pages.NewInterfaceWizardPage;

/**
 * Interface creation wizard. This class uses a {@link NewInterfaceWizardPage}
 * 
 * @author cbosdonnat
 *
 */
public class NewInterfaceWizard extends BasicNewResourceWizard implements
		INewWizard {

	private NewInterfaceWizardPage mPage;
	
	/**
	 * Creates the wizard
	 */
	public NewInterfaceWizard() {
		super();
		
		mActivePage = OOEclipsePlugin.getActivePage();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		
		final UnoFactoryData data = mPage.fillData(new UnoFactoryData());
		
		Job serviceJob = new Job(Messages.getString("NewInterfaceWizard.JobName")) { //$NON-NLS-1$

			protected IStatus run(IProgressMonitor monitor) {
				
				monitor.beginTask(Messages.getString("NewInterfaceWizard.TaskName"), 1); //$NON-NLS-1$
				IStatus status = new Status(IStatus.OK,
						OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
						IStatus.OK, "", null); //$NON-NLS-1$
				try {
					IUnoidlProject prj = mPage.getProject();
					UnoFactory.createInterface(data, prj, mActivePage, monitor);
				
					// Releasing the data informations
					data.dispose();
					monitor.worked(1);
				} catch (Exception e) {
					 status = new Status(IStatus.CANCEL,
								OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
								IStatus.OK, 
								Messages.getString("NewInterfaceWizard.InterfaceCreationError"), e); //$NON-NLS-1$
					 monitor.setCanceled(true);
				}
				
				monitor.done();
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
		
		if  (selection.getFirstElement() instanceof IAdaptable) {
			
			IAdaptable adapter = (IAdaptable)selection.getFirstElement();
			IResource resource = (IResource)adapter.getAdapter(IResource.class);
			
			if (resource != null) {
				createPages(resource.getProject());
			}
		}
	}
	
	/**
	 * Creates the new interface page
	 * 
	 * @param project the project in which to create the interface
	 */
	private void createPages(IProject project){
		if (null != project){
			try {
				if (project.hasNature(OOEclipsePlugin.UNO_NATURE_ID)){
					UnoidlProject unoProject = (UnoidlProject)project.getNature(
							OOEclipsePlugin.UNO_NATURE_ID);
					
					mPage = new NewInterfaceWizardPage("newiface", unoProject); //$NON-NLS-1$
					
					addPage(mPage);
				}
			} catch (CoreException e){
				PluginLogger.debug(e.getMessage());
			}
		}
	}
	
	private IWorkbenchPage mActivePage;
	
	/**
	 * Method opening a file in an UNO-IDL editor
	 * 
	 * @param resource the file to open
	 */
	protected void openResource(final IFile resource) {
		
		if (mActivePage != null) {
			final Display display = getShell().getDisplay();
			if (display != null) {
				display.asyncExec(new Runnable() {
					public void run() {
						try {
							IDE.openEditor(mActivePage, resource, true);
						} catch (PartInitException e) {
							PluginLogger.debug(e.getMessage());
						}
					}
				});
			}
		}
	}
}
