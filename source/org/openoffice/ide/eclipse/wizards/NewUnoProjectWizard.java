/*************************************************************************
 *
 * $RCSfile: NewUnoProjectWizard.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/10 12:07:15 $
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.model.UnoidlProject;
import org.openoffice.ide.eclipse.preferences.ooo.OOoContainer;
import org.openoffice.ide.eclipse.preferences.sdk.SDKContainer;

public class NewUnoProjectWizard extends Wizard implements INewWizard {
	
	private NewUnoProjectPage page;

	public NewUnoProjectWizard() {
		super();
		
		page = new NewUnoProjectPage();
		addPage(page);
		setForcePreviousAndNextButtons(false);
	}

	public boolean performFinish() {
		
		final IProject project = page.getProjectHandle();
		final String prefix = page.getPrefix();
		final String outputExt = page.getOutputExt();
		final int language = page.getChosenLanguage();
		final String sdkname = page.getSDKName();
		final String oooname = page.getOOoName();
		
		// Instantiation of a new thread with a Progress monitor to do the job.
		IRunnableWithProgress op = new IRunnableWithProgress (){

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				// Creates the new project
				createProject(project, monitor);
				
				// Create the ouput and idl packages
				try {
					UnoidlProject unoProject = (UnoidlProject)project.getNature(
														OOEclipsePlugin.UNO_NATURE_ID);
					unoProject.setCompanyPrefix(prefix);
					unoProject.setOutputExtension(outputExt);
					unoProject.setOuputLanguage(language);
					unoProject.setSdk(SDKContainer.getSDKContainer().getSDK(sdkname));
					unoProject.setOOo(OOoContainer.getOOoContainer().getOOo(oooname));
					
					// Creation of the unoidl package
					unoProject.createUnoidlPackage(monitor);
					
					// Creation of the Code Packages
					unoProject.createCodePackage(monitor);
					
					// Creation of the urd output directory
					unoProject.createUrdDir(monitor);
					
				} catch (CoreException e) {
					OOEclipsePlugin.logError(
							OOEclipsePlugin.getTranslationString(I18nConstants.NOT_UNO_PROJECT),
							e);
				}
				
			}			
		};
		
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException e) {
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
				project.delete(true, true, null);
			} catch (CoreException ex){
				// Impossible to delete the project
			}
		} catch (InterruptedException e) {
			// Cancel pressed
		}
		
		return true;
	}

	/**
	 * This method creates and opens the project with the Java and Uno natures
	 * 
	 * @param project project to create
	 * @param monitor monitor used to report the creation state
	 */
	protected void createProject(IProject project, IProgressMonitor monitor) {
		try {
			if (!project.exists()){
				project.create(monitor);
			}
			
			if (!project.isOpen()){
				project.open(monitor);
			}
			
			IProjectDescription description = project.getDescription();
			String[] natureIds = description.getNatureIds();
			String[] newNatureIds = new String[natureIds.length+1];
			System.arraycopy(natureIds, 0, newNatureIds, 0, natureIds.length);
			
			// Adding the Uno Nature
			newNatureIds[natureIds.length] = OOEclipsePlugin.UNO_NATURE_ID;
			
			description.setNatureIds(newNatureIds);
			project.setDescription(description, monitor);
			
			monitor.worked(1);  // Tells the monitor that the process ended with no error
			
		} catch (CoreException e) {
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
					I18nConstants.NATURE_SET_FAILED), e);
			monitor.worked(0);
		}
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// Nothing to do
	}

}
