/*************************************************************************
 *
 * $RCSfile: NewUnoProjectWizard.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/02 20:13:13 $
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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.ILanguage;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;

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
	
	public boolean performFinish() {
		
		// First create the UNO project, then create the service.
		
		final String packageName = servicePage.getPackage();
		final String name = servicePage.getElementName();
		final String ifaceName = servicePage.getInheritanceName();
		final boolean published = servicePage.isPublished();
		
		final IProject project = mainPage.getProjectHandle();
		final String prefix = mainPage.getPrefix();
		final String outputExt = mainPage.getOutputExt();
		final ILanguage language = mainPage.getChosenLanguage();
		final String sdkname = mainPage.getSDKName();
		final String oooname = mainPage.getOOoName();
		
		// Instantiation of a new thread with a Progress monitor to do the job.
		IRunnableWithProgress op = new IRunnableWithProgress (){

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				
				mainPage.createUnoidlProject(
						project, prefix, outputExt, language, 
						sdkname, oooname, monitor);
				
				IUnoidlProject unoProject = mainPage.getUnoidlProject();
				
				servicePage.setUnoidlProject(unoProject);
				
				IFile file = servicePage.createService(
						packageName, name, ifaceName.replace(".", "::"), published);
				
				// Reveal the project main service file	
				selectAndReveal(file);
				openResource(file);

				UnoidlProjectHelper.setProjectBuilders(unoProject, monitor);
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
					OOEclipsePlugin.getTranslationString(
							I18nConstants.UNO_PLUGIN_ERROR),
					null,
					OOEclipsePlugin.getTranslationString(
							I18nConstants.PROJECT_CREATION_FAILED),
					MessageDialog.ERROR,
					new String[]{OOEclipsePlugin.getTranslationString(
							I18nConstants.OK)},
					0);
			dialog.setBlockOnOpen(true);
			dialog.create();
			dialog.open();
		
			try {
				mainPage.getProjectHandle().delete(true, true, null);
			} catch (CoreException ex) {
				// Impossible to delete
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
