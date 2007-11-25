/*************************************************************************
 *
 * $RCSfile: NewServiceWizard.java,v $
 *
 * $Revision: 1.6 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:29 $
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
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.wizards.utils.NoSuchPageException;

/**
 * The wizard for the creation of UNO services. 
 * 
 * @author cedricbosdo
 *
 */
public class NewServiceWizard extends BasicNewResourceWizard implements INewWizard {

    private IWorkbenchPage mActivePage;
    
    private ServiceWizardSet mWizardSet;
    
    /**
     * Constructor.
     */
    public NewServiceWizard() {
        super();
        mActivePage = OOEclipsePlugin.getActivePage();
    }

    /**
     * {@inheritDoc}
     */
    public boolean performFinish() {
        
        Job serviceJob = new Job(Messages.getString("NewServiceWizard.JobName")) { //$NON-NLS-1$

            protected IStatus run(IProgressMonitor pMonitor) {
                
                IStatus status = new Status(IStatus.OK,
                        OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
                        IStatus.OK, "", null); //$NON-NLS-1$
                try {

                    mWizardSet.doFinish(pMonitor, mActivePage);
                    
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

    /**
     * {@inheritDoc}
     */
    @Override
    public IWizardPage getNextPage(IWizardPage pPage) {
        IWizardPage next = null;
        try {
            next = mWizardSet.getNextPage(pPage);
        } catch (NoSuchPageException e) { }
        
        return next;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IWizardPage getPreviousPage(IWizardPage pPage) {
        IWizardPage previous = null;
        try {
            previous = mWizardSet.getPreviousPage(pPage);
        } catch (NoSuchPageException e) { }
        
        return previous;
    }
    
    /**
     * {@inheritDoc}
     */
    public void init(IWorkbench pWorkbench, IStructuredSelection pSelection) {
        
        super.init(pWorkbench, pSelection);
        
        if (pSelection.getFirstElement() instanceof IAdaptable) {
            
            IAdaptable adapter = (IAdaptable)pSelection.getFirstElement();
            IResource resource = (IResource)adapter.getAdapter(IResource.class);
            
            if (resource != null) {
                createPages(resource.getProject());
            }
        }
    }
    
    /**
     * Creates all the wizard pages needed by the UNO service creation wizard.
     * 
     * <p>The created pages are described by the {@link ServiceWizardSet}.</p>
     * 
     * @param pProject the project where to create the service.
     */
    private void createPages(IProject pProject) {
        if (null != pProject) {
            try {
                if (pProject.hasNature(OOEclipsePlugin.UNO_NATURE_ID)) {
                    UnoidlProject unoProject = (UnoidlProject)pProject.getNature(
                            OOEclipsePlugin.UNO_NATURE_ID);
                    
                    mWizardSet = new ServiceWizardSet(this);
                    
                    IWizardPage[] pages = mWizardSet.getPages();
                    for (IWizardPage wizardPage : pages) {
                        addPage(wizardPage);
                    }
                    
                    // initializes the wizard
                    UnoFactoryData data = new UnoFactoryData();
                    data.setProperty(IUnoFactoryConstants.PROJECT_NAME, unoProject.getName());
                    data.setProperty(IUnoFactoryConstants.PROJECT_PREFIX, unoProject.getCompanyPrefix());
                    data.setProperty(IUnoFactoryConstants.PROJECT_OOO, unoProject.getOOo());
                    
                    UnoFactoryData serviceData = new UnoFactoryData();
                    serviceData.setProperty(IUnoFactoryConstants.TYPE_NATURE, IUnoFactoryConstants.SERVICE);
                    serviceData.setProperty(IUnoFactoryConstants.TYPE_NAME, "MyService");
                    
                    data.addInnerData(serviceData);
                    
                    mWizardSet.initialize(data);
                    
                }
            } catch (CoreException e) {
                PluginLogger.debug(e.getMessage());
            }
        }
    }
}
