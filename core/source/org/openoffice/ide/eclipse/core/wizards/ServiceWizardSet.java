package org.openoffice.ide.eclipse.core.wizards;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.internal.model.UnoFactory;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.wizards.pages.NewInterfaceWizardPage;
import org.openoffice.ide.eclipse.core.wizards.pages.NewServiceWizardPage;
import org.openoffice.ide.eclipse.core.wizards.utils.WizardPageSet;

/**
 * This wizard page set manages a service page and an interface page.
 * 
 * @author cedricbosdo <cedricbosdo@openoffice.org>
 *
 */
public class ServiceWizardSet extends WizardPageSet {

	public static final String SERVICE_PAGE_ID = "service";
	public static final String INTERFACE_PAGE_ID = "interface";
	
	/**
	 * An instance of the project in which the wizard set is run. 
	 * 
	 * <p>This member should be used only to replace the pages project reference
	 * is needed. This is mostly used when the project isn't created when the 
	 * wizard set is opened, ie: at project creation.</p> 
	 */
	protected IUnoidlProject mProject;

	private boolean showInterfacePage = true;
	
	public ServiceWizardSet(IWizard wizard) {
		super(wizard);
		
		NewServiceWizardPage servicePage = new NewServiceWizardPage(SERVICE_PAGE_ID, null);
		servicePage.addPageListener(mPageListener);
		
		addPage(servicePage);
		addPage(new NewInterfaceWizardPage(INTERFACE_PAGE_ID, null));
	}
	
	/**
	 * Initializes the service and interface pages at their creation.
	 * 
	 * <p>The factory data needed for the pages initialization should contain some 
	 * fields about the project and two or less children (one for a service 
	 * and/or one for an interface). The data structure should contain the 
	 * following fields:</p>
	 * 
	 * <ol>
	 * 		<li><p>Project fields</p>
	 * 			<ul>
	 * 				<li>Project name</li>
	 * 				<li>OOo Instance</li>
	 * 				<li>Company prefix</li>
	 * 			</ul>
	 * 		</li>
	 * 		<li><p>Service fields <em>(opt.)</em></p>
	 * 			<ul>
	 * 				<li>Name <em>(opt. : the project name is used)</em></li>
	 * 				<li>Package name <em>(opt. : the company prefix can be used)</em></li>
	 * 				<li>Inherited interface <em>(opt.)</em></li>
	 * 			</ul>
	 * 		</li>
	 * 		<li><p>Interface fields <em>(opt.)</em></p>
	 * 			<ul>
	 * 				<li>Name <em>(opt.)</em></li>
	 * 				<li>Package name <em>(opt.)</em></li>
	 * 			</ul>
	 * 		</li>
	 * </ol>
	 * 
	 * The interface fields may not be given: in this case the missing fields
	 * will get a default value computed from the service fields. If the service
	 * inherited interface isn't provided, a default interface based on the 
	 * service name will be chosen.
	 */
	@Override
	public void initialize(UnoFactoryData data) {
		try {			
			// Get the project infos
			String prjName = (String)data.getProperty(IUnoFactoryConstants.PROJECT_NAME);
			IOOo ooo = (IOOo)data.getProperty(IUnoFactoryConstants.PROJECT_OOO);
			String prefix = (String)data.getProperty(IUnoFactoryConstants.PROJECT_PREFIX);

			String packageRoot = (prefix != null) ? prefix.replaceAll("\\.", "::") : null;
			
			UnoFactoryData[] inner = data.getInnerData();
			int serviceId = -1;
			int interfaceId = -1;
			int i = 0;
			while (i < inner.length && (serviceId == -1 || interfaceId == -1)) {
				Integer type = (Integer)inner[i].getProperty(IUnoFactoryConstants.TYPE_NATURE);
				if (type != null) {
					if (type.intValue() == IUnoFactoryConstants.SERVICE) serviceId = i;
					if (type.intValue() == IUnoFactoryConstants.INTERFACE) interfaceId = i;
				}
				i++;
			}
			
			// Get the service infos
			String serviceName = null;
			String servicePackage = null;
			String serviceInheritance = null;
			
			if (serviceId != -1) {
				UnoFactoryData serviceData = inner[serviceId];
				serviceName = (String)serviceData.getProperty(IUnoFactoryConstants.TYPE_NAME);
				servicePackage = (String)serviceData.getProperty(IUnoFactoryConstants.PACKAGE_NAME);
				String[] inheritances = (String[])serviceData.getProperty(
						IUnoFactoryConstants.INHERITED_INTERFACES);
				if (inheritances != null && inheritances.length > 0) {
					serviceInheritance = inheritances[0];
				}

				if (servicePackage == null) servicePackage = packageRoot;
				if (serviceName == null || serviceName.equals("")) {
					serviceName = prjName.replace("\\W", "");
					serviceName = serviceName.substring(0, 1).toUpperCase() + 
						serviceName.substring(1);
				}
				
				if (serviceInheritance == null) {
					serviceInheritance = servicePackage + "::X" + serviceName;
				}
			}

			// Get the interface infos
			String ifaceName = null;
			String ifacePackage = null;
			
			if (interfaceId != -1) {
				UnoFactoryData ifaceData = inner[interfaceId];
				ifaceName = (String)ifaceData.getProperty(IUnoFactoryConstants.TYPE_NAME);
				ifacePackage = (String)ifaceData.getProperty(IUnoFactoryConstants.PACKAGE_NAME);
			}
			
			if (ifaceName == null) {
				ifaceName = serviceInheritance.substring(serviceInheritance.lastIndexOf(':')+1);
			}
			
			if (ifacePackage == null) {
				ifacePackage = serviceInheritance.substring(0,
						serviceInheritance.lastIndexOf(":") -1 );
			}
			
			/* 
			 * Check if the interface page is needed.
			 * 
			 * The interface page is needed if the interface package starts with
			 * the project prefix and if the corresponding idl file doesn't
			 * exists in the project IDL folders.
			 */
			IUnoidlProject prj = ProjectsManager.getProject(prjName);
				
			boolean needsInterfacePage = serviceInheritance.startsWith(packageRoot);
			needsInterfacePage &= !existsIdlFile(serviceInheritance, prj);
			showInterfacePage = needsInterfacePage;
			
			// Set the data into the service page
			NewServiceWizardPage servicePage = (NewServiceWizardPage)getPage(SERVICE_PAGE_ID);
			
			mChangingPages = true;
			
			servicePage.setName(serviceName, false);
			servicePage.setPackageRoot(packageRoot);
			servicePage.setPackage(servicePackage.substring(packageRoot.length()), false);
			servicePage.setOOoInstance(ooo);
			servicePage.setUnoidlProject(prj);
			servicePage.setInheritanceName(serviceInheritance, false);
			
			NewInterfaceWizardPage ifacePage = (NewInterfaceWizardPage)getPage(INTERFACE_PAGE_ID);
			setHidden(ifacePage, !showInterfacePage);
			
			// Set the data into the interface page
			ifacePage.setPackageRoot(packageRoot);
			ifacePage.setPackage(ifacePackage.substring(packageRoot.length()), false);
			ifacePage.setName(ifaceName, false);
			ifacePage.setOOoInstance(ooo);
			ifacePage.setUnoidlProject(prj);
			
			mChangingPages = false;
			
		} catch (Exception e) {
			PluginLogger.warning("Wrong data for service page set initizalization", e);
		}
	}
	
	/**
	 * Change the service and/or interface page from a data delta.
	 * 
	 * <p>The delta is a {@link UnoFactoryData} structured in the same way than
	 * the data used in {@link #initialize(UnoFactoryData)}. The main difference
	 * is that only the changed data should be set. The according fields will be
	 * modified in the pages.</p>
	 * 
	 * <p>The service inheritance and interface name and package are changed if
	 * the service module or name has changed. This doesn't apply if the service 
	 * inheritance has been manually changed by the user.</p>
	 * 
	 * @param delta the data delta to update the pages with
	 * 
	 * @see #initialize(UnoFactoryData) for details on how the delta should be
	 * 		structured
	 */
	@Override
	public void dataChanged(UnoFactoryData delta) {
		
		try {			
			// Get the project infos
			String prjName = (String)delta.getProperty(IUnoFactoryConstants.PROJECT_NAME);
			IOOo ooo = (IOOo)delta.getProperty(IUnoFactoryConstants.PROJECT_OOO);
			String prefix = (String)delta.getProperty(IUnoFactoryConstants.PROJECT_PREFIX);

			String packageRoot = (prefix != null) ? prefix.replaceAll("\\.", "::") : null;
			
			UnoFactoryData[] inner = delta.getInnerData();
			int serviceId = -1;
			int interfaceId = -1;
			int i = 0;
			while (i < inner.length && (serviceId == -1 || interfaceId == -1)) {
				Integer type = (Integer)inner[i].getProperty(IUnoFactoryConstants.TYPE_NATURE);
				if (type != null) {
					if (type.intValue() == IUnoFactoryConstants.SERVICE) serviceId = i;
					if (type.intValue() == IUnoFactoryConstants.INTERFACE) interfaceId = i;
				}
				i++;
			}
			
			// Get the service infos
			String serviceName = null;
			String servicePackage = null;
			String serviceInheritance = null;
			
			if (serviceId != -1) {
				UnoFactoryData serviceData = inner[serviceId];
				serviceName = (String)serviceData.getProperty(IUnoFactoryConstants.TYPE_NAME);
				servicePackage = (String)serviceData.getProperty(IUnoFactoryConstants.PACKAGE_NAME);
				String[] inheritances = (String[])serviceData.getProperty(
						IUnoFactoryConstants.INHERITED_INTERFACES);
				if (inheritances != null && inheritances.length > 0) {
					serviceInheritance = inheritances[0];
				}
				
				/* 
				 * Change the service inheritance if no inheritance interface is defined
				 * in the service page and one of the service module or name is 
				 * changed.
				 * 
				 * If a service inheritance if defined in the delta, do not change it
				 */
				if (serviceInheritance == null && (serviceName != null || servicePackage != null)) {
					NewServiceWizardPage servicePage = (NewServiceWizardPage)getPage(
							SERVICE_PAGE_ID);
					String inputInheritance = servicePage.getInheritanceName();
					String pkg = (servicePackage != null) ? servicePackage : servicePage.getPackage();
					String name = (serviceName != null) ? serviceName : servicePage.getElementName();
					
					boolean notChangedInheritance = inputInheritance == null || inputInheritance.equals("");
					notChangedInheritance |= !servicePage.isInheritanceChanged();
					
					if (notChangedInheritance) {
						serviceInheritance = pkg + "::X" + name;  
					}
				}
				
				/*
				 * Finds the package module after the root module (company prefix)
				 */
				if (servicePackage != null && packageRoot == null) {
					NewServiceWizardPage servicePage = (NewServiceWizardPage)getPage(
							SERVICE_PAGE_ID);
					IUnoidlProject unoPrj = servicePage.getProject();
					String existingRoot = unoPrj.getRootModule();
					servicePackage = servicePackage.substring(existingRoot.length());
					if (servicePackage.startsWith("::")) servicePackage = servicePackage.substring(2);
				} else if (servicePackage != null && packageRoot != null) {
					servicePackage = servicePackage.substring(packageRoot.length());
					if (servicePackage.startsWith("::")) servicePackage = servicePackage.substring(2);
				}
			}
			
		
			// Get the interface infos
			String ifaceName = null;
			String ifacePackage = null;
			
			if (interfaceId != -1) {
				UnoFactoryData ifaceData = inner[interfaceId];
				ifaceName = (String)ifaceData.getProperty(IUnoFactoryConstants.TYPE_NAME);
				ifacePackage = (String)ifaceData.getProperty(IUnoFactoryConstants.PACKAGE_NAME);
			}
			
			/* 
			 * Check if the interface page is needed.
			 * 
			 * The interface page is needed if the interface package starts with
			 * the project prefix and if the corresponding idl file doesn't
			 * exists in the project IDL folders.
			 * 
			 * If the interface variable is null, then there is nothing to 
			 * update here.
			 */
			IUnoidlProject prj = (prjName != null) ? ProjectsManager.getProject(prjName) : null;
			if (serviceInheritance != null) {
				IUnoidlProject unoprj = prj;
				NewServiceWizardPage servicePage = (NewServiceWizardPage)getPage(
						SERVICE_PAGE_ID);
				if (unoprj == null) {
					unoprj = servicePage.getProject();
				}
				
				// The project might be not set...
				String existingRoot = servicePage.getPackageRoot();
				if (unoprj != null) {
					existingRoot = unoprj.getRootModule();
				}
				
				if (existingRoot != null && !existingRoot.equals("")) {
					boolean needsInterfacePage = serviceInheritance.startsWith(existingRoot);
					needsInterfacePage = needsInterfacePage && !existsIdlFile(serviceInheritance, unoprj);
					showInterfacePage = needsInterfacePage;
				} else {
					showInterfacePage = false;
				}
			}
			
			/*
			 * Change the interface name if serviceInheritance has changed 
			 */
			if (serviceInheritance != null && showInterfacePage) {
				/*
				 * compute the interface name and package from the 
				 * service inheritance
				 */
				ifaceName = serviceInheritance.substring(serviceInheritance.lastIndexOf(':')+1);
				ifacePackage = serviceInheritance.substring(0, 
						serviceInheritance.lastIndexOf(":")-1);
			}
			
			/*
			 * Finds the package module after the root module (company prefix)
			 * for the interface package name
			 */
			if (ifacePackage != null && packageRoot == null) {
				NewInterfaceWizardPage ifacePage = (NewInterfaceWizardPage)getPage(
						INTERFACE_PAGE_ID);
				IUnoidlProject unoPrj = ifacePage.getProject();
				String existingRoot = ifacePage.getPackageRoot();
				if (unoPrj != null) {
					existingRoot = unoPrj.getRootModule();
				}
				ifacePackage = ifacePackage.substring(existingRoot.length());
				if (ifacePackage.startsWith("::")) ifacePackage = ifacePackage.substring(2);
			} else if (ifacePackage != null && packageRoot != null) {
				if (ifacePackage.startsWith(packageRoot)) {
					ifacePackage = ifacePackage.substring(packageRoot.length());
					if (ifacePackage.startsWith("::")) ifacePackage = ifacePackage.substring(2);
				}
			}
			
			// Set the data into the service page
			NewServiceWizardPage servicePage = (NewServiceWizardPage)getPage(SERVICE_PAGE_ID);

			NewInterfaceWizardPage ifacePage = (NewInterfaceWizardPage)getPage(INTERFACE_PAGE_ID);
			setHidden(ifacePage, !showInterfacePage);
			
			mChangingPages = true;
			
			if (serviceName != null) servicePage.setName(serviceName, false);
			if (packageRoot != null) servicePage.setPackageRoot(packageRoot);
			if (servicePackage != null) servicePage.setPackage(servicePackage, false);
			if (ooo != null) servicePage.setOOoInstance(ooo);
			if (prj != null) servicePage.setUnoidlProject(prj);
			if (serviceInheritance != null) servicePage.setInheritanceName(serviceInheritance, false);
			
			// Set the data into the interface page
			if (packageRoot != null) ifacePage.setPackageRoot(packageRoot);
			if (ifacePackage != null) ifacePage.setPackage(ifacePackage, false);
			if (ifaceName != null) ifacePage.setName(ifaceName, false);
			if (ooo != null) ifacePage.setOOoInstance(ooo);
			if (prj != null) ifacePage.setUnoidlProject(prj);
				
			mChangingPages = false;
			
		} catch (Exception e) {
			PluginLogger.warning("Wrong data for service page set initizalization", e);
		}
	}
	
	@Override
	public void doFinish(IProgressMonitor monitor, IWorkbenchPage activePage) {
		NewServiceWizardPage servicePage = (NewServiceWizardPage)getPage(SERVICE_PAGE_ID);
		NewInterfaceWizardPage ifacePage = (NewInterfaceWizardPage)getPage(INTERFACE_PAGE_ID);
		
		try {
			IUnoidlProject prj = servicePage.getProject();
			/*
			 * If the project in the service page is null, then try with the
			 * wizard set instance: the project might not be created when 
			 * running the wizard set.
			 */
			if (prj == null) {
				prj = mProject;
			}
		
			// Create the interface file
			if (showInterfacePage == true) {
				UnoFactoryData ifaceData = new UnoFactoryData();
				ifaceData = ifacePage.fillData(ifaceData);
				UnoFactory.createInterface(ifaceData, prj, activePage, monitor, false);
				ifaceData.dispose();
			}
			
			// Create the service file
			UnoFactoryData serviceData = new UnoFactoryData();
			serviceData = servicePage.fillData(serviceData);
			UnoFactory.createService(serviceData, prj, activePage, monitor, false);
			
			UnoidlProjectHelper.refreshProject(prj, null);
			UnoidlProjectHelper.forceBuild(prj, monitor);
			
			// Create the implementation skeleton
			UnoFactoryData wizardSetData = new UnoFactoryData();
			wizardSetData.addInnerData(serviceData);
			wizardSetData.setProperty(IUnoFactoryConstants.PROJECT_LANGUAGE, prj.getLanguage());
			wizardSetData.setProperty(IUnoFactoryConstants.PROJECT_NAME, prj.getName());
			UnoFactory.makeSkeleton(wizardSetData, activePage, monitor);
			wizardSetData.dispose();
			
		} catch (Exception e) {
			PluginLogger.error("Error happened during service creation", e);
		}
		
	}
	
	/**
	 * Checks if an IDL file exists in the project for a given IDL type.
	 * 
	 * <p>Please note that this method behaves correctly only if the user is 
	 * respecting the following design rules:
	 * <ul>
	 * 	<li>One IDL type per file</li>
	 *  <li></li>
	 * </ul>
	 * </p>
	 * 
	 * @param idlFullName
	 * @param prj
	 * @return
	 */
	private boolean existsIdlFile(String idlFullName, IUnoidlProject prj) {
		
		boolean exists = false;
		
		if (prj != null) {
			try {
				IPath idlPath = prj.getIdlPath();
				idlPath = idlPath.append(idlFullName.replace("::", "/") + ".idl");
				
				idlPath = prj.getProjectPath().append(idlPath);
				
				exists = idlPath.toFile().exists();
			} catch (Exception e) {
				PluginLogger.warning("Error determining if the idl file exists: " + idlFullName, e);
			}
		}
		
		return exists;
	}
}
