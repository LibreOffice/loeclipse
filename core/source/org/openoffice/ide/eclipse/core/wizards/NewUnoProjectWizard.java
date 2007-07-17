/*************************************************************************
 *
 * $RCSfile: NewUnoProjectWizard.java,v $
 *
 * $Revision: 1.10 $
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.internal.model.UnoFactory;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.model.language.ILanguage;
import org.openoffice.ide.eclipse.core.model.language.LanguageWizardPage;
import org.openoffice.ide.eclipse.core.wizards.pages.NewUnoProjectPage;
import org.openoffice.ide.eclipse.core.wizards.utils.NoSuchPageException;

public class NewUnoProjectWizard extends BasicNewProjectResourceWizard implements INewWizard {
	
	protected NewUnoProjectPage mMainPage;
	private LanguageWizardPage mLanguagePage = null;
	private ServiceWizardSet mServiceSet = null;
	
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
		
		mServiceSet = new ServiceWizardSet(this);
		IWizardPage[] pages = mServiceSet.getPages();
		for (IWizardPage wizardPage : pages) {
			addPage(wizardPage);
			
			// All the pages of the service wizard set should be hidden if
			// the inheritance interface is fixed.
			if (mServiceIfaceName == null || mServiceIfaceName.equals("")) { //$NON-NLS-1$
				mServiceSet.setHidden(wizardPage, true);
			}
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
		
			try {
				// Compute the default service name
				String serviceName = mMainPage.getProjectName().trim();
				serviceName = serviceName.replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
				String firstLetter = serviceName.substring(0, 1).toUpperCase();
				serviceName = firstLetter + serviceName.substring(1);

				// Compute the default inherited interface name
				String ifaceName = mMainPage.getPrefix() + ".X" + serviceName; //$NON-NLS-1$

				if (mServiceIfaceName != null && !mServiceIfaceName.equals("")) { //$NON-NLS-1$
					// Change the default value if the service inheritance name is fixed
					ifaceName = mServiceIfaceName;
				}

				UnoFactoryData data = new UnoFactoryData();

				data.setProperty(IUnoFactoryConstants.PROJECT_OOO, 
						OOoContainer.getOOo(mMainPage.getOOoName()));
				data.setProperty(IUnoFactoryConstants.PROJECT_PREFIX, mMainPage.getPrefix());

				UnoFactoryData serviceData = new UnoFactoryData();
				serviceData.setProperty(IUnoFactoryConstants.TYPE_NATURE, IUnoFactoryConstants.SERVICE);
				serviceData.setProperty(IUnoFactoryConstants.TYPE_NAME, serviceName);
				serviceData.setProperty(IUnoFactoryConstants.INHERITED_INTERFACES, 
						new String[]{ifaceName.replaceAll("\\.", "::")});
				serviceData.setProperty(IUnoFactoryConstants.PACKAGE_NAME, 
						mMainPage.getPrefix().replaceAll("\\.", "::"));

				data.addInnerData(serviceData);

				if (mServiceIfaceName != null && !mServiceIfaceName.equals("")) { //$NON-NLS-1$
					// Change the default value if the service inheritance name is fixed
					UnoFactoryData ifaceData = new UnoFactoryData();
					ifaceData.setProperty(IUnoFactoryConstants.TYPE_NATURE, IUnoFactoryConstants.INTERFACE);
					
					String[] splitted = mServiceIfaceName.split("::");
					String packageName = splitted[0];
					for (int i = 1; i < splitted.length-1; i++) {
						packageName += "::" + splitted[i];
					}
					
					ifaceData.setProperty(IUnoFactoryConstants.TYPE_NAME, splitted[splitted.length-1]);
					ifaceData.setProperty(IUnoFactoryConstants.PACKAGE_NAME, packageName);
					
					data.addInnerData(ifaceData);
				}
				
				mServiceSet.dataChanged(data);

				data.dispose();
				data = null;
			} catch (Exception e) {
				// Do nothing
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage next = null;
		try {
			next = mServiceSet.getNextPage(page);
		} catch (NoSuchPageException e) {
			// Return the default next page if the page is not in the wizard set. 
			next = super.getNextPage(page);

			try {
				if (mMainPage.equals(page)) {
					if (mLanguagePage != null) {
						next = mLanguagePage;
					} else {
						next = mServiceSet.getPage(ServiceWizardSet.SERVICE_PAGE_ID); // Could be null
					}
				} else if (mLanguagePage != null && mLanguagePage.equals(page)) {
					next = mServiceSet.getPage(ServiceWizardSet.SERVICE_PAGE_ID);
				}
			} catch (Exception ee) {
				next = null;
			}
		}
		
		return next;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		IWizardPage previous = null;
		try {
			previous = mServiceSet.getPreviousPage(page);
		} catch (NoSuchPageException e) {
			// Return the default previous page if the page is not in the
			// wizard set. 
			previous = super.getPreviousPage(page);			
		}

		// If the page is the service page, the previous page shouldn't be null
		if (mServiceSet != null) {
			IWizardPage servicePage = mServiceSet.getPage(ServiceWizardSet.SERVICE_PAGE_ID);
			if (previous == null && page.equals(servicePage)) {
				previous = (mLanguagePage != null) ? mLanguagePage: mMainPage;
			}
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
		if (mLanguagePage != null) {
			data = mLanguagePage.fillData(data);
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
				IUnoidlProject prj = UnoFactory.createProject(mData, mActivePage, monitor);
				
				mServiceSet.mProject = prj; 
				mServiceSet.doFinish(monitor, mActivePage);
				
				UnoidlProjectHelper.setProjectBuilders(prj);
				
			} catch (Exception e) {
				Object o = mData.getProperty(IUnoFactoryConstants.PROJECT_HANDLE);
				if (o instanceof IProject) {
					rollback((IProject)o);
				}
				
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(Display.getDefault().getActiveShell(), 
								Messages.getString("NewUnoProjectWizard.ProjectCreationErrorTitle"),  //$NON-NLS-1$
								Messages.getString("NewUnoProjectWizard.ProjectCreationErrorMessage")); //$NON-NLS-1$
					}
				});
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
		
		private void rollback(IProject project) {
			try {
				project.delete(true, true, null);
			} catch (CoreException ex) {
				PluginLogger.debug(
						Messages.getString("NewUnoProjectWizard.DeleteProjectError")); //$NON-NLS-1$
			}
		}
	}
}
