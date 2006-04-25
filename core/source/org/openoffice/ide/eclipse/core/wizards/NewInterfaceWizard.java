/*************************************************************************
 *
 * $RCSfile: NewInterfaceWizard.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/25 19:10:00 $
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
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;

/**
 * TODOC 
 * 
 * @author cbosdonnat
 *
 */
public class NewInterfaceWizard extends BasicNewResourceWizard implements
		INewWizard {

	private NewInterfaceWizardPage page;
	
	public NewInterfaceWizard() {
		super();
		
		activePage = OOEclipsePlugin.getActivePage();
	}

	public boolean performFinish() {
		IFile file = page.createInterface();
		
		// Reveal the project main service file	
		selectAndReveal(file);
		openResource(file);
		
		return true;
	}
	
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
	
	private void createPages(IProject project){
		if (null != project){
			try {
				if (project.hasNature(OOEclipsePlugin.UNO_NATURE_ID)){
					UnoidlProject unoProject = (UnoidlProject)project.getNature(
							OOEclipsePlugin.UNO_NATURE_ID);
					
					page = new NewInterfaceWizardPage("newiface", unoProject);
					
					addPage(page);
				}
			} catch (CoreException e){
				PluginLogger.getInstance().debug(e.getMessage());
			}
		}
	}
	
	private IWorkbenchPage activePage;
	
	protected void openResource(final IFile resource) {
		
		if (activePage != null) {
			final Display display = getShell().getDisplay();
			if (display != null) {
				display.asyncExec(new Runnable() {
					public void run() {
						try {
							IDE.openEditor(activePage, resource, true);
						} catch (PartInitException e) {
							PluginLogger.getInstance().debug(e.getMessage());
						}
					}
				});
			}
		}
	}
}
