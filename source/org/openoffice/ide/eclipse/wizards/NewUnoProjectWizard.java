/*************************************************************************
 *
 * $RCSfile: NewUnoProjectWizard.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/30 13:24:26 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the following licenses
 *
 *     - GNU Lesser General Public License Version 2.1
 *     - Sun Industry Standards Source License Version 1.1
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
 *
 * Sun Industry Standards Source License Version 1.1
 * =================================================
 * The contents of this file are subject to the Sun Industry Standards
 * Source License Version 1.1 (the "License"); You may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.openoffice.org/license.html.
 *
 * Software provided under this License is provided on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 * MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 * See the License for the specific provisions governing your rights and
 * obligations concerning the Software.
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
package org.openoffice.ide.eclipse.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.model.InterfaceService;
import org.openoffice.ide.eclipse.model.UnoidlFile;
import org.openoffice.ide.eclipse.model.UnoidlProject;

public class NewUnoProjectWizard extends BasicNewProjectResourceWizard implements INewWizard {
	
	private NewUnoProjectPage mainPage;
	private NewServiceWizardPage servicePage;
	
	private IWorkbenchPage activePage;

	public NewUnoProjectWizard() {
		
		super();
		activePage = OOEclipsePlugin.getActivePage();
		setForcePreviousAndNextButtons(false);
	}
	
	public void addPages() {
		mainPage = new NewUnoProjectPage();
		addPage(mainPage);
		
		servicePage = new NewServiceWizardPage("service", null);
		addPage(servicePage);
	}

	public IWizardPage getNextPage(IWizardPage page) {
		
		if (page.equals(mainPage) && mainPage.isPageComplete()) {
			
			UnoidlProject unoProject = mainPage.getUnoidlProject();
			
			servicePage.setUnoidlProject(unoProject);
			servicePage.setName(unoProject.getName(), false);
			servicePage.setPackage("", true);
		}
		
		return super.getNextPage(page);
	}
	
	public boolean performFinish() {
		
		final UnoidlProject unoProject = mainPage.getUnoidlProject();
		final String packageName = servicePage.getPackage();
		final String name = servicePage.getServiceName();
		final String ifaceName = servicePage.getInheritanceName();
		final boolean published = servicePage.isPublished();
		
		// Instantiation of a new thread with a Progress monitor to do the job.
		IRunnableWithProgress op = new IRunnableWithProgress (){

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				
				InterfaceService service = servicePage.createService(
						packageName, name, ifaceName, published);
				UnoidlFile unofile = service.getFile();
				
				// Reveal the project main service file	
				selectAndReveal(unofile.getFile());
				openResource(unofile.getFile());
				
				try {
					// Add the project builders
					unoProject.setBuilders();
					
					// Initial build of the project 
					unoProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
				} catch (CoreException e) {
					OOEclipsePlugin.logError(
							OOEclipsePlugin.getTranslationString(
									I18nConstants.NOT_UNO_PROJECT), e);
				}
			}
		};
		
		
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException e) {
			
			if (null != System.getProperty("DEBUG")){
				e.printStackTrace();
			}
			
			MessageDialog dialog = new MessageDialog(
					getShell(),
					OOEclipsePlugin.getTranslationString(I18nConstants.UNO_PLUGIN_ERROR),
					null,
					OOEclipsePlugin.getTranslationString(I18nConstants.PROJECT_CREATION_FAILED),
					MessageDialog.ERROR,
					new String[]{OOEclipsePlugin.getTranslationString(I18nConstants.OK)},
					0);
			dialog.setBlockOnOpen(true);
			dialog.create();
			dialog.open();
		
			try {
				unoProject.getProject().delete(true, true, null);
			} catch (CoreException ex){
				// Impossible to delete the project
			}
		} catch (InterruptedException e) {
			// Cancel pressed
		}
		
		return true;
	}
	
	protected void openResource(final IFile resource) {
		
		if (activePage != null) {
			final Display display = getShell().getDisplay();
			if (display != null) {
				display.asyncExec(new Runnable() {
					public void run() {
						try {
							IDE.openEditor(activePage, resource, true);
						} catch (PartInitException e) {
							if (null != System.getProperty("DEBUG")){
								e.printStackTrace();
							}
						}
					}
				});
			}
		}
	}
	
	public IWorkbench getWorkbench() {
		return OOEclipsePlugin.getDefault().getWorkbench();
	}
}
