/*************************************************************************
 *
 * $RCSfile: NewUnoProjectWizard.java,v $
 *
 * $Revision: 1.6 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/23 18:27:16 $
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.model.UnoFactory;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.model.language.ILanguage;
import org.openoffice.ide.eclipse.core.model.language.LanguageWizardPage;

public class NewUnoProjectWizard extends BasicNewProjectResourceWizard implements INewWizard {
	
	private NewUnoProjectPage mMainPage;
	private LanguageWizardPage mLanguagePage = null;
	private NewServiceWizardPage mServicePage = null;
	private NewInterfaceWizardPage mInterfacePage = null;
	
	private boolean mShowInterfacePage = true;
	private String mServiceIfaceName = null; 
	
	private IWorkbenchPage mActivePage;

	public NewUnoProjectWizard() {
		
		super();
		mActivePage = OOEclipsePlugin.getActivePage();
		setForcePreviousAndNextButtons(false);
	}
	
	protected void setDisableServicePage(String ifaceName) {
		mServiceIfaceName = ifaceName;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		mMainPage = new NewUnoProjectPage("mainpage"); //$NON-NLS-1$
		addPage(mMainPage);
		
		if (mServiceIfaceName == null || mServiceIfaceName.equals("")) { //$NON-NLS-1$
			mServicePage = new NewServiceWizardPage("service", null); //$NON-NLS-1$
			mInterfacePage = new NewInterfaceWizardPage("interface", null); //$NON-NLS-1$
			addPage(mServicePage);
			addPage(mInterfacePage);
		}
	}
	
	public void setLanguagePage(LanguageWizardPage page) {
		if (page != null) {
			if (mLanguagePage == null || 
					!mLanguagePage.getClass().equals(page.getClass())) {
				mLanguagePage = page;
				addPage(mLanguagePage);
			}
		} else {
			if (mLanguagePage != null) mLanguagePage.dispose();
			mLanguagePage = null;
		}
	}
	
	/**
	 * This method should be called by included pages to notify any change that
	 * could have an impact on other pages.
	 *
	 * @param page the page which has changed.
	 */
	public void pageChanged(IWizardPage page) {
		
		if (mMainPage.equals(page)) {
			
			// Create/Remove the language page if needed
			ILanguage lang = mMainPage.getChosenLanguage();
			if (lang != null) {
				UnoFactoryData data = new UnoFactoryData();
				setLanguagePage(lang.getLanguageUI().getWizardPage(
						mMainPage.fillData(data, false)));
				
				// Cleaning
				data.dispose();
			} else {
				setLanguagePage(null);
			}
			
			// change the language page if possible
			if (mLanguagePage != null) { 
				UnoFactoryData data = new UnoFactoryData();
				mLanguagePage.setProjectInfos(
						mMainPage.fillData(data, false));
				
				// cleaning
				data.dispose();
			}
		
			if (mServiceIfaceName == null || mServiceIfaceName.equals("")) { //$NON-NLS-1$
				// Change the service page
				mServicePage.setPackageRoot(mMainPage.getPrefix());
				mServicePage.setPackage("", true); //$NON-NLS-1$
				mServicePage.setOOoInstance(OOoContainer.getInstance().
						getOOo(mMainPage.getOOoName()));

				String serviceName = mMainPage.getProjectName().trim().toLowerCase();
				serviceName = serviceName.replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
				try {
					String firstLetter = serviceName.substring(0, 1).toUpperCase();
					serviceName = firstLetter + serviceName.substring(1);

					String ifaceName = mMainPage.getPrefix() + ".X" + serviceName; //$NON-NLS-1$

					mServicePage.setName(serviceName, false);
					mServicePage.setInheritanceName(ifaceName, false);

					mInterfacePage.setPackageRoot(mMainPage.getPrefix());
					mInterfacePage.setName("X" + serviceName, false); //$NON-NLS-1$
				} catch (Exception e) {
					// May be too early in the initialisation
				}
			}
		} else if (mServicePage.equals(page)) {
			// Checks if the interface page should be shown
			if (mServicePage.getInheritanceName().startsWith(mMainPage.getPrefix())) {
				mShowInterfacePage = true;
			} else {
				mShowInterfacePage = false;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage next = super.getNextPage(page);
		
		if (mMainPage.equals(page)) {
			if (mLanguagePage != null) {
				next = mLanguagePage;
			} else {
				next = mServicePage; // Could be null
			}
		} else if (mLanguagePage.equals(page)) {
			next = mServicePage;
		} else if (mServicePage != null && mServicePage.equals(page)) {
			if (mShowInterfacePage) {
				next = mInterfacePage;
			} else {
				next = null;
			}
		} else if (mShowInterfacePage && page.equals(mInterfacePage)) {
			next = null;
		}
		return next;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		IWizardPage previous = super.getPreviousPage(page);
		
		if (page.equals(mLanguagePage)) {
			previous = mMainPage;
		} else if (mServicePage != null && mServicePage.equals(page)) {
			if (mLanguagePage != null) {
				previous = mLanguagePage;
			}
		} else if (page.equals(mInterfacePage)) {
			previous = mServicePage; // The service page can't be null here 
		}
		return previous;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		
		// First gather the data
		UnoFactoryData data = new UnoFactoryData();
		data = mMainPage.fillData(data, true);
		if (mLanguagePage != null) data = mLanguagePage.fillData(data);
		if (mServiceIfaceName != null && !mServiceIfaceName.equals("")) { //$NON-NLS-1$
			data.addInnerData(
					NewServiceWizardPage.getTypeData(data, mServiceIfaceName));
		} else {
			data.addInnerData(mServicePage.fillData(new UnoFactoryData()));
			if (mShowInterfacePage) {
				data.addInnerData(mInterfacePage.fillData(new UnoFactoryData()));
			}
		}
		
		new ProjectCreationJob(data).schedule();
		
		return true;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.wizards.newresource.BasicNewResourceWizard#getWorkbench()
	 */
	public IWorkbench getWorkbench() {
		return OOEclipsePlugin.getDefault().getWorkbench();
	}
	
	private class ProjectCreationJob extends Job {
		
		private UnoFactoryData mData;
		
		public ProjectCreationJob(UnoFactoryData data) {
			super(Messages.getString("NewUnoProjectWizard.JobName")); //$NON-NLS-1$
			setPriority(Job.INTERACTIVE);
			
			mData = data;
		}

		protected IStatus run(IProgressMonitor monitor) {
			
			IStatus status = new Status(IStatus.OK, 
					OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, 
					IStatus.OK, "", null); //$NON-NLS-1$
			
			// Create the projet folder structure
			try {
				UnoFactory.createProject(mData, mActivePage, monitor);
			} catch (Exception e) {
				
				Object o = mData.getProperty(IUnoFactoryConstants.PROJECT_HANDLE);
				if (o instanceof IProject) {
					rollback(e, (IProject)o);
				}
				
				PluginLogger.error(
						Messages.getString("NewUnoProjectWizard.CreateProjectError"), e); //$NON-NLS-1$
				
				status = new Status(IStatus.OK, 
						OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, 
						IStatus.OK, 
						Messages.getString("NewUnoProjectWizard.CreateProjectError"),  //$NON-NLS-1$
						e);
			}
			
			if (mData != null) mData.dispose();
			mData = null;
			
			return status;
		}
		
		private void rollback(Exception e, IProject project) {
			try {
				project.delete(true, true, null);
			} catch (CoreException ex) {
				PluginLogger.debug(
						Messages.getString("NewUnoProjectWizard.DeleteProjectError")); //$NON-NLS-1$
			}
		}
	}
}
