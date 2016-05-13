/*************************************************************************
 *
 * $RCSfile: ServiceWizardSet.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:49 $
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
package org.libreoffice.ide.eclipse.core.wizards;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.libreoffice.ide.eclipse.core.internal.model.UnoFactory;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.UnoFactoryData;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.wizards.pages.NewInterfaceWizardPage;
import org.libreoffice.ide.eclipse.core.wizards.pages.NewScopedElementWizardPage;
import org.libreoffice.ide.eclipse.core.wizards.pages.NewServiceWizardPage;
import org.libreoffice.ide.eclipse.core.wizards.utils.WizardPageSet;

/**
 * This wizard page set manages a service page and an interface page.
 *
 *
 */
public class ServiceWizardSet extends WizardPageSet {

    public static final String SERVICE_PAGE_ID = "service"; //$NON-NLS-1$
    public static final String INTERFACE_PAGE_ID = "interface"; //$NON-NLS-1$

    /**
     * An instance of the project in which the wizard set is run.
     *
     * <p>
     * This member should be used only to replace the pages project reference is needed. This is mostly used when the
     * project isn't created when the wizard set is opened, i.e.: at project creation.
     * </p>
     */
    protected IUnoidlProject mProject;

    private boolean mShowInterfacePage = true;

    /**
     * Constructor.
     *
     * @param pWizard
     *            the wizard in which the wizard set will be included
     */
    public ServiceWizardSet(IWizard pWizard) {
        super(pWizard);

        NewServiceWizardPage servicePage = new NewServiceWizardPage(SERVICE_PAGE_ID, null);
        servicePage.addPageListener(mPageListener);

        addPage(servicePage);
        addPage(new NewInterfaceWizardPage(INTERFACE_PAGE_ID, null));
    }

    /**
     * Initializes the service and interface pages at their creation.
     *
     * <p>
     * The factory data needed for the pages initialization should contain some fields about the project and two or less
     * children (one for a service and/or one for an interface). The data structure should contain the following fields:
     * </p>
     *
     * <ol>
     * <li>
     * <p>
     * Project fields
     * </p>
     * <ul>
     * <li>Project name</li>
     * <li>OOo Instance</li>
     * <li>Company prefix</li>
     * </ul>
     * </li>
     * <li>
     * <p>
     * Service fields <em>(opt.)</em>
     * </p>
     * <ul>
     * <li>Name <em>(opt. : the project name is used)</em></li>
     * <li>Package name <em>(opt. : the company prefix can be used)</em></li>
     * <li>Inherited interface <em>(opt.)</em></li>
     * </ul>
     * </li>
     * <li>
     * <p>
     * Interface fields <em>(opt.)</em>
     * </p>
     * <ul>
     * <li>Name <em>(opt.)</em></li>
     * <li>Package name <em>(opt.)</em></li>
     * </ul>
     * </li>
     * </ol>
     *
     * The interface fields may not be given: in this case the missing fields will get a default value computed from the
     * service fields. If the service inherited interface isn't provided, a default interface based on the service name
     * will be chosen.
     *
     * @param pData
     *            the service initialization data as described above.
     */
    @Override
    public void initialize(UnoFactoryData pData) {
        try {
            // Get the project infos
            String prjName = (String) pData.getProperty(IUnoFactoryConstants.PROJECT_NAME);
            IOOo ooo = (IOOo) pData.getProperty(IUnoFactoryConstants.PROJECT_OOO);
            String prefix = (String) pData.getProperty(IUnoFactoryConstants.PROJECT_PREFIX);

            String packageRoot = null;
            if (prefix != null) {
                packageRoot = prefix.replaceAll("\\.", "::"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            UnoFactoryData[] inner = pData.getInnerData();
            HashMap<Integer, Integer> ids = getPagesId(inner);

            String serviceInheritance = null;

            // Set the data into the service page
            if (ids.containsKey(IUnoFactoryConstants.SERVICE)) {
                UnoFactoryData serviceData = inner[ids.get(IUnoFactoryConstants.SERVICE)];
                setServicePageData(serviceData, packageRoot, prjName, ooo);

                serviceInheritance = getServiceInheritance(serviceData, packageRoot, prjName);
            }

            if (ids.containsKey(IUnoFactoryConstants.INTERFACE)) {
                UnoFactoryData ifaceData = inner[ids.get(IUnoFactoryConstants.INTERFACE)];
                setInterfacePageData(ifaceData, packageRoot, prjName, ooo, serviceInheritance);
            }

        } catch (Exception e) {
            PluginLogger.warning(Messages.getString("ServiceWizardSet.WrongInitDataWarning"), e); //$NON-NLS-1$
        }
    }

    /**
     * Set the interface creation page data from the interface {@link UnoFactoryData} and the different external data.
     *
     * @param pData
     *            the interface data
     * @param pPackageRoot
     *            the project company prefix
     * @param pPrjName
     *            the project name
     * @param pOOo
     *            the OOo used for the project configuration
     * @param pServiceInheritance
     *            the service inheritance interface
     */
    private void setInterfacePageData(UnoFactoryData pData, String pPackageRoot, String pPrjName, IOOo pOOo,
        String pServiceInheritance) {

        // Get the interface infos
        String ifaceName = (String) pData.getProperty(IUnoFactoryConstants.TYPE_NAME);
        String ifacePackage = (String) pData.getProperty(IUnoFactoryConstants.PACKAGE_NAME);

        if (ifaceName == null) {
            ifaceName = pServiceInheritance.substring(pServiceInheritance.lastIndexOf(':') + 1);
        }

        if (ifacePackage == null) {
            ifacePackage = pServiceInheritance.substring(0, pServiceInheritance.lastIndexOf(":") - 1); //$NON-NLS-1$
        }

        IUnoidlProject prj = ProjectsManager.getProject(pPrjName);
        checkIsInterfacePageNeeded(prj, pServiceInheritance);

        /*
         * Change the interface name if serviceInheritance has changed
         */
        if (pServiceInheritance != null) {
            /*
             * compute the interface name and package from the service inheritance
             */
            ifaceName = pServiceInheritance.substring(pServiceInheritance.lastIndexOf(':') + 1);
            ifacePackage = pServiceInheritance.substring(0, pServiceInheritance.lastIndexOf(":") - 1); //$NON-NLS-1$
        }

        NewInterfaceWizardPage ifacePage = (NewInterfaceWizardPage) getPage(INTERFACE_PAGE_ID);
        setHidden(ifacePage, !mShowInterfacePage);

        mChangingPages = true;

        // Set the data into the interface page
        ifacePage.setPackageRoot(pPackageRoot);
        ifacePage.setPackage(ifacePackage.substring(pPackageRoot.length()), false);
        ifacePage.setName(ifaceName, false);
        if (pOOo != null) {
            ifacePage.setOOoInstance(pOOo);
        }
        if (prj != null) {
            ifacePage.setUnoidlProject(prj);
        }

        mChangingPages = false;
    }

    /**
     * Set the service creation page data from the service {@link UnoFactoryData} and the different external data.
     *
     * @param pServiceData
     *            the service data
     * @param pPackageRoot
     *            the project company prefix
     * @param pPrjName
     *            the project name
     * @param pOOo
     *            the OOo used for the project configuration
     */
    private void setServicePageData(UnoFactoryData pServiceData, String pPackageRoot, String pPrjName, IOOo pOOo) {
        // Get the service infos
        String serviceName = null;
        String servicePackage = null;

        serviceName = (String) pServiceData.getProperty(IUnoFactoryConstants.TYPE_NAME);
        servicePackage = (String) pServiceData.getProperty(IUnoFactoryConstants.PACKAGE_NAME);

        // Get the service package and name from the dialog if null: may be
        // called on data change
        NewServiceWizardPage servicePage = (NewServiceWizardPage) getPage(SERVICE_PAGE_ID);
        if (servicePackage == null) {
            servicePackage = servicePage.getPackage();
        }

        if (serviceName == null) {
            serviceName = servicePage.getElementName();
        }

        // If the service package and service names aren't defined in the page, use defaults
        if (servicePackage == null || servicePackage.equals("")) { //$NON-NLS-1$
            servicePackage = pPackageRoot;
        }
        if ((serviceName == null || serviceName.equals("")) && pPrjName != null) { //$NON-NLS-1$
            serviceName = pPrjName.replace("\\W", ""); //$NON-NLS-1$ //$NON-NLS-2$
            serviceName = serviceName.substring(0, 1).toUpperCase() + serviceName.substring(1);
        }

        String serviceInheritance = getServiceInheritance(pServiceData, pPackageRoot, pPrjName);
        servicePackage = guessPackage(servicePackage, pPackageRoot);

        mChangingPages = true;

        servicePage.setName(serviceName, false);
        servicePage.setPackageRoot(pPackageRoot);
        servicePage.setPackage(servicePackage, false);
        if (pOOo != null) {
            servicePage.setOOoInstance(pOOo);
        }
        if (pPrjName != null) {
            IUnoidlProject prj = ProjectsManager.getProject(pPrjName);
            servicePage.setUnoidlProject(prj);
        }
        servicePage.setInheritanceName(serviceInheritance, false);

        mChangingPages = false;
    }

    /**
     * Fetch the ids of the types data in the given array of {@link UnoFactoryData}.
     *
     * @param pTypesData
     *            the data describing the types set
     *
     * @return a map, associating the keys {@link IUnoFactoryConstants#SERVICE} or
     *         {@link IUnoFactoryConstants#INTERFACE} to the type id in the array.
     */
    private HashMap<Integer, Integer> getPagesId(UnoFactoryData[] pTypesData) {
        HashMap<Integer, Integer> ids = new HashMap<Integer, Integer>();

        int serviceId = -1;
        int interfaceId = -1;
        int i = 0;
        while (i < pTypesData.length && (serviceId == -1 || interfaceId == -1)) {
            Integer type = (Integer) pTypesData[i].getProperty(IUnoFactoryConstants.TYPE_NATURE);
            if (type != null) {
                if (type.intValue() == IUnoFactoryConstants.SERVICE) {
                    serviceId = i;
                    ids.put(IUnoFactoryConstants.SERVICE, serviceId);
                }
                if (type.intValue() == IUnoFactoryConstants.INTERFACE) {
                    interfaceId = i;
                    ids.put(IUnoFactoryConstants.INTERFACE, interfaceId);
                }
            }
            i++;
        }

        return ids;
    }

    /**
     * Change the service and/or interface page from a data delta.
     *
     * <p>
     * The delta is a {@link UnoFactoryData} structured in the same way than the data used in
     * {@link #initialize(UnoFactoryData)}. The main difference is that only the changed data should be set. The
     * according fields will be modified in the pages.
     * </p>
     *
     * <p>
     * The service inheritance and interface name and package are changed if the service module or name has changed.
     * This doesn't apply if the service inheritance has been manually changed by the user.
     * </p>
     *
     * @param pDelta
     *            the data delta to update the pages with
     *
     * @see #initialize(UnoFactoryData) for details on how the delta should be structured
     */
    @Override
    public void dataChanged(UnoFactoryData pDelta) {

        try {
            // Get the project infos
            String prjName = (String) pDelta.getProperty(IUnoFactoryConstants.PROJECT_NAME);
            IOOo ooo = (IOOo) pDelta.getProperty(IUnoFactoryConstants.PROJECT_OOO);
            String prefix = (String) pDelta.getProperty(IUnoFactoryConstants.PROJECT_PREFIX);

            String packageRoot = null;
            if (prefix != null) {
                packageRoot = prefix.replaceAll("\\.", "::"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if (packageRoot == null) {
                NewServiceWizardPage servicePage = (NewServiceWizardPage) getPage(SERVICE_PAGE_ID);
                packageRoot = servicePage.getPackageRoot();
            }

            UnoFactoryData[] inner = pDelta.getInnerData();
            HashMap<Integer, Integer> ids = getPagesId(inner);

            // Set the service informations
            String serviceInheritance = null;
            if (ids.containsKey(IUnoFactoryConstants.SERVICE)) {
                UnoFactoryData serviceData = inner[ids.get(IUnoFactoryConstants.SERVICE)];
                setServicePageData(serviceData, packageRoot, prjName, ooo);
                serviceInheritance = getServiceInheritance(serviceData, packageRoot, prjName);
            }

            // Set the interface informations
            UnoFactoryData ifaceData = new UnoFactoryData();
            if (ids.containsKey(IUnoFactoryConstants.INTERFACE)) {
                ifaceData = inner[ids.get(IUnoFactoryConstants.INTERFACE)];
            }
            setInterfacePageData(ifaceData, packageRoot, prjName, ooo, serviceInheritance);

        } catch (Exception e) {
            PluginLogger.warning(Messages.getString("ServiceWizardSet.WrongInitDataWarning"), e); //$NON-NLS-1$
        }
    }

    /**
     * Computes the service inheritance name.
     *
     * @param pData
     *            the service data
     * @param pPackageRoot
     *            the project company prefix
     * @param pPrjName
     *            the project name
     *
     * @return the computed service inheritance
     */
    private String getServiceInheritance(UnoFactoryData pData, String pPackageRoot, String pPrjName) {
        String serviceInheritance = null;

        String serviceName = null;
        String servicePackage = null;

        serviceName = (String) pData.getProperty(IUnoFactoryConstants.TYPE_NAME);
        servicePackage = (String) pData.getProperty(IUnoFactoryConstants.PACKAGE_NAME);

        // Get the service package and name from the dialog if null: may be
        // called on data change
        NewServiceWizardPage servicePage = (NewServiceWizardPage) getPage(SERVICE_PAGE_ID);
        if (servicePackage == null) {
            servicePackage = servicePage.getPackage();
        }

        if (serviceName == null) {
            serviceName = servicePage.getElementName();
        }

        // Otherwise computes it from the project
        if (servicePackage == null) {
            servicePackage = pPackageRoot;
        }
        if ((serviceName == null || serviceName.equals("")) && pPrjName != null) { //$NON-NLS-1$
            serviceName = pPrjName.replace("\\W", ""); //$NON-NLS-1$ //$NON-NLS-2$
            serviceName = serviceName.substring(0, 1).toUpperCase() + serviceName.substring(1);
        }

        String[] inheritances = (String[]) pData.getProperty(IUnoFactoryConstants.INHERITED_INTERFACES);
        if (inheritances != null && inheritances.length > 0) {
            serviceInheritance = inheritances[0];
        }

        if (serviceInheritance == null) {
            serviceInheritance = servicePackage + "::X" + serviceName; //$NON-NLS-1$
        }

        return serviceInheritance;
    }

    /**
     * Check if the interface page is needed.
     *
     * The interface page is needed if the interface package starts with the project prefix and if the corresponding IDL
     * file doesn't exists in the project IDL folders.
     *
     * If the interface variable is <code>null</code>, then there is nothing to update here.
     *
     * @param pPrj
     *            the project of the wizard. If <code>null</code> the project defined in the service page will be used.
     * @param pServiceInheritance
     *            the service inheritance, which is also the name of the interface described by the interface page
     */
    private void checkIsInterfacePageNeeded(IUnoidlProject pPrj, String pServiceInheritance) {

        if (pServiceInheritance != null) {
            NewServiceWizardPage servicePage = (NewServiceWizardPage) getPage(SERVICE_PAGE_ID);
            if (pPrj == null) {
                pPrj = servicePage.getProject();
            }

            // The project might be not set...
            String existingRoot = servicePage.getPackageRoot();
            if (pPrj != null) {
                existingRoot = pPrj.getRootModule();
            }

            if (existingRoot != null && !existingRoot.equals("")) { //$NON-NLS-1$
                boolean needsInterfacePage = pServiceInheritance.startsWith(existingRoot);
                needsInterfacePage = needsInterfacePage
                    && !NewScopedElementWizardPage.existsIdlFile(pServiceInheritance, pPrj);
                mShowInterfacePage = needsInterfacePage;
            } else {
                mShowInterfacePage = false;
            }
        }
    }

    /**
     * Finds the package module after the root module (company prefix).
     *
     * @param pTypePackage
     *            the service package contained in the data delta.
     * @param pPackageRoot
     *            the company prefix if contained in the delta.
     *
     * @return the package without the company prefix separated by "::"
     */
    private String guessPackage(String pTypePackage, String pPackageRoot) {

        if (pPackageRoot == null) {
            // Get the UNO project to fetch the missing package root
            NewServiceWizardPage servicePage = (NewServiceWizardPage) getPage(SERVICE_PAGE_ID);
            pPackageRoot = servicePage.getPackageRoot();
        }

        if (pTypePackage != null) {
            pTypePackage = pTypePackage.substring(pPackageRoot.length());
            if (pTypePackage.startsWith("::")) { //$NON-NLS-1$
                pTypePackage = pTypePackage.substring(2);
            }
        }

        return pTypePackage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFinish(IProgressMonitor pMonitor, IWorkbenchPage pActivePage) {
        NewServiceWizardPage servicePage = (NewServiceWizardPage) getPage(SERVICE_PAGE_ID);
        NewInterfaceWizardPage ifacePage = (NewInterfaceWizardPage) getPage(INTERFACE_PAGE_ID);

        try {
            IUnoidlProject prj = servicePage.getProject();
            /*
             * If the project in the service page is null, then try with the wizard set instance: the project might not
             * be created when running the wizard set.
             */
            if (prj == null) {
                prj = mProject;
            }

            // Create the interface file
            if (mShowInterfacePage) {
                UnoFactoryData ifaceData = new UnoFactoryData();
                ifaceData = ifacePage.fillData(ifaceData);
                UnoFactory.createInterface(ifaceData, prj, pActivePage, pMonitor, false);
                ifaceData.dispose();
            }

            // Create the service file
            UnoFactoryData serviceData = new UnoFactoryData();
            serviceData = servicePage.fillData(serviceData);
            UnoFactory.createService(serviceData, prj, pActivePage, pMonitor, false);

            UnoidlProjectHelper.refreshProject(prj, null);
            UnoidlProjectHelper.forceBuild(prj, pMonitor);

            // Create the implementation skeleton
            UnoFactoryData wizardSetData = new UnoFactoryData();
            wizardSetData.addInnerData(serviceData);
            wizardSetData.setProperty(IUnoFactoryConstants.PROJECT_LANGUAGE, prj.getLanguage());
            wizardSetData.setProperty(IUnoFactoryConstants.PROJECT_NAME, prj.getName());
            UnoFactory.makeSkeleton(wizardSetData, pActivePage, pMonitor);
            wizardSetData.dispose();

        } catch (Exception e) {
            PluginLogger.error(Messages.getString("ServiceWizardSet.ServiceCreationError"), e); //$NON-NLS-1$
        }

    }
}
