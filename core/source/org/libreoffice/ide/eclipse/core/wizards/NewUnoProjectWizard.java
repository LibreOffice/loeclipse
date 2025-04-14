/*************************************************************************
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
package org.libreoffice.ide.eclipse.core.wizards;

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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.libreoffice.ide.eclipse.core.internal.model.UnoFactory;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.OOoContainer;
import org.libreoffice.ide.eclipse.core.model.UnoFactoryData;
import org.libreoffice.ide.eclipse.core.model.language.AbstractLanguage;
import org.libreoffice.ide.eclipse.core.model.language.LanguageWizardPage;
import org.libreoffice.ide.eclipse.core.utils.WorkbenchHelper;
import org.libreoffice.ide.eclipse.core.wizards.pages.NewUnoProjectPage;
import org.libreoffice.ide.eclipse.core.wizards.utils.NoSuchPageException;

/**
 * New UNO project wizard.
 */
public class NewUnoProjectWizard extends BasicNewProjectResourceWizard implements INewWizard {

    protected NewUnoProjectPage mMainPage;
    private LanguageWizardPage mLanguagePage = null;
    private ServiceWizardSet mServiceSet = null;

    private String mServiceIfaceName = null;

    private IWorkbenchPage mActivePage;

    /**
     * Constructor.
     */
    public NewUnoProjectWizard() {

        super();
        mActivePage = WorkbenchHelper.getActivePage();
        setForcePreviousAndNextButtons(false);
    }

    /**
     * Force the inheritance interface to a given value and do not show the service creation pages.
     *
     * <p>
     * This is used by other wizards like the new URE application wizard.
     * </p>
     *
     * @param ifaceName
     *            the inheritance interface to force, separated with "::"
     */
    protected void setDisableServicePage(String ifaceName) {
        mServiceIfaceName = ifaceName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

    /**
     * Set the language page to use for the project.
     *
     * @param page
     *            the language page to use.
     */
    public void setLanguagePage(LanguageWizardPage page) {
        if (page != null) {
            if (mLanguagePage == null || !mLanguagePage.getClass().equals(page.getClass())) {
                mLanguagePage = page;
                addPage(mLanguagePage);
            }
        } else {
            if (mLanguagePage != null) {
                mLanguagePage.dispose();
            }
            mLanguagePage = null;
        }
    }

    /**
     * This method should be called by included pages to notify any change that could have an impact on other pages.
     *
     * @param page
     *            the page which has changed.
     */
    public void pageChanged(IWizardPage page) {

        if (mMainPage.equals(page)) {

            // change the language page if possible
            updateLanguagePage();

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

                data.setProperty(IUnoFactoryConstants.PROJECT_OOO, OOoContainer.getOOo(mMainPage.getOOoName()));
                data.setProperty(IUnoFactoryConstants.PROJECT_PREFIX, mMainPage.getPrefix());

                UnoFactoryData serviceData = new UnoFactoryData();
                serviceData.setProperty(IUnoFactoryConstants.TYPE_NATURE, IUnoFactoryConstants.SERVICE);
                serviceData.setProperty(IUnoFactoryConstants.TYPE_NAME, serviceName);
                serviceData.setProperty(IUnoFactoryConstants.INHERITED_INTERFACES,
                    new String[] { ifaceName.replaceAll("\\.", "::") }); //$NON-NLS-1$ //$NON-NLS-2$
                serviceData.setProperty(IUnoFactoryConstants.PACKAGE_NAME,
                    mMainPage.getPrefix().replaceAll("\\.", "::")); //$NON-NLS-1$ //$NON-NLS-2$

                data.addInnerData(serviceData);

                if (mServiceIfaceName != null && !mServiceIfaceName.equals("")) { //$NON-NLS-1$
                    // Change the default value if the service inheritance name is fixed
                    UnoFactoryData ifaceData = new UnoFactoryData();
                    ifaceData.setProperty(IUnoFactoryConstants.TYPE_NATURE, IUnoFactoryConstants.INTERFACE);

                    String[] splitted = mServiceIfaceName.split("::"); //$NON-NLS-1$
                    String packageName = splitted[0];
                    for (int i = 1; i < splitted.length - 1; i++) {
                        packageName += "::" + splitted[i]; //$NON-NLS-1$
                    }

                    ifaceData.setProperty(IUnoFactoryConstants.TYPE_NAME, splitted[splitted.length - 1]);
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

    /**
     * {@inheritDoc}
     */
    @Override
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
                        // Could be null
                        next = mServiceSet.getPage(ServiceWizardSet.SERVICE_PAGE_ID);
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

    /**
     * {@inheritDoc}
     */
    @Override
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
            boolean isServicePage = previous == null && page.equals(servicePage);
            if (isServicePage && mLanguagePage != null) {
                previous = mLanguagePage;
            } else if (isServicePage && mLanguagePage == null) {
                previous = mMainPage;
            }
        }

        return previous;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

    /**
     * {@inheritDoc}
     */
    @Override
    public IWorkbench getWorkbench() {
        return PlatformUI.getWorkbench();
    }

    /**
     * Adapts the language specific page using the selected language.
     */
    private void updateLanguagePage() {
        // Create/Remove the language page if needed
        AbstractLanguage lang = mMainPage.getChosenLanguage();
        if (lang != null && !lang.toString().contains("python")) {
            UnoFactoryData data = new UnoFactoryData();
            LanguageWizardPage page = lang.getNewWizardPage();
            if (page != null) {
                page.setProjectInfos(mMainPage.fillData(data, false));
            }
            setLanguagePage(page);

            // Cleaning
            data.dispose();
        } else {
            setLanguagePage(null);
        }

        if (mLanguagePage != null) {
            UnoFactoryData data = new UnoFactoryData();
            mLanguagePage.setProjectInfos(mMainPage.fillData(data, false));

            // cleaning
            data.dispose();
        }
    }

    /**
     * Thread executing the project creation tasks.
     */
    private class ProjectCreationJob extends Job {

        private UnoFactoryData mData;

        /**
         * Constructor.
         *
         * @param data
         *            the data describing the project to create.
         */
        public ProjectCreationJob(UnoFactoryData data) {
            super(Messages.getString("NewUnoProjectWizard.JobName")); //$NON-NLS-1$
            setPriority(Job.INTERACTIVE);
            mData = data;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected IStatus run(IProgressMonitor monitor) {

            IStatus status = new Status(IStatus.OK, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
                IStatus.OK, "", null); //$NON-NLS-1$

            // Create the projet folder structure
            try {
                IUnoidlProject prj = UnoFactory.createProject(mData, monitor);

                mServiceSet.mProject = prj;
                mServiceSet.doFinish(monitor, mActivePage);

                UnoidlProjectHelper.setProjectBuilders(prj);

            } catch (Exception e) {
                PluginLogger.error(null, e);
                Object o = mData.getProperty(IUnoFactoryConstants.PROJECT_HANDLE);
                if (o instanceof IProject) {
                    rollback((IProject) o);
                }

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageDialog.openError(Display.getDefault().getActiveShell(),
                            Messages.getString("NewUnoProjectWizard.ProjectCreationErrorTitle"), //$NON-NLS-1$
                            Messages.getString("NewUnoProjectWizard.ProjectCreationErrorMessage")); //$NON-NLS-1$
                    }
                });
                PluginLogger.error(Messages.getString("NewUnoProjectWizard.CreateProjectError"), e); //$NON-NLS-1$

                status = new Status(IStatus.OK, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IStatus.OK,
                    Messages.getString("NewUnoProjectWizard.CreateProjectError"), //$NON-NLS-1$
                    e);
            }

            if (mData != null) {
                mData.dispose();
            }
            mData = null;

            return status;
        }

        /**
         * Undo the project creation if something have happened during the project creation.
         *
         * @param pProject
         *            the project to remove.
         */
        private void rollback(IProject pProject) {
            try {
                pProject.delete(true, true, null);
            } catch (CoreException ex) {
                PluginLogger.debug(Messages.getString("NewUnoProjectWizard.DeleteProjectError")); //$NON-NLS-1$
            }
        }
    }
}
