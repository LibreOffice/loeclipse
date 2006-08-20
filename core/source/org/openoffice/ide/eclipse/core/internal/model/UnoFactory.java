/*************************************************************************
 *
 * $RCSfile: UnoFactory.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/08/20 11:55:49 $
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
package org.openoffice.ide.eclipse.core.internal.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.CompositeFactory;
import org.openoffice.ide.eclipse.core.model.ILanguage;
import org.openoffice.ide.eclipse.core.model.IUnoComposite;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;

/**
 * This class is a factory creating UNO projects and types from data sets
 * describing the object to get.
 * 
 * @author cedricbosdo
 */
public final class UnoFactory {
	
	/**
	 * Creates a UNO project from scratch. It creates the directories,
	 * intial types and generates the basic implementation. 
	 * 
	 * @param data the project description
	 */
	public static void createProject(UnoFactoryData data, IWorkbenchPage page,
			IProgressMonitor monitor) throws Exception {
		
		IUnoidlProject prj = UnoidlProjectHelper.createStructure(data, monitor);
		
		// Create the inner types
		UnoFactoryData[] inners = data.getInnerData();
		for (int i=0; i<inners.length; i++) {
			UnoFactoryData inner = inners[i];
			Integer type = (Integer)inner.getProperty(IUnoFactoryConstants.TYPE);
			switch (type.intValue()) {
				// TODO This switch has to be extended to support other types
				case IUnoFactoryConstants.SERVICE:
					createService(inner, prj, page, monitor);
					break;
			}
		}
		
		UnoidlProjectHelper.setProjectBuilders(prj, monitor);
		
		// create the language-specific part
		ILanguage language = (ILanguage)data.getProperty(
				IUnoFactoryConstants.PROJECT_LANGUAGE);
		language.configureProject(data);
		
		// TODO generate the implementation skeleton
	}
	
	/**
	 * Creates a service from its factory data.
	 * 
	 * @param data the data describing the service
	 * @param prj the uno project that will contain the service
	 * @param activePage the page in which to open the created file
	 * @param monitor the progress monitor to report the operation progress
	 * @throws Exception is thrown if anything wrong happens
	 */
	public static void createService(UnoFactoryData data, IUnoidlProject prj, 
			IWorkbenchPage activePage, IProgressMonitor monitor) throws Exception {

		// Extract the data
		String path = (String)data.getProperty(
				IUnoFactoryConstants.PACKAGE_NAME);
		path = path.replaceAll("\\.", "::"); //$NON-NLS-1$ //$NON-NLS-2$
		String name = (String)data.getProperty(
				IUnoFactoryConstants.TYPE_NAME);
		String[] inheritedIfaces = (String[])data.getProperty(
				IUnoFactoryConstants.INHERITED_INTERFACES);
		boolean published = ((Boolean)data.getProperty(
				IUnoFactoryConstants.TYPE_PUBLISHED)).booleanValue();
		
		// Create the necessary modules
		UnoidlProjectHelper.createModules(path, prj, null);

		String typepath = path +"::" + name; //$NON-NLS-1$

		// Create the file node
		IUnoComposite file = CompositeFactory.createTypeFile(typepath, prj);

		// Create the file content skeleton
		IUnoComposite fileContent = CompositeFactory.createFileContent(typepath);
		file.addChild(fileContent);

		// Add the include line for the inheritance interface
		String inheritanceName = inheritedIfaces[0];
		IUnoComposite include = CompositeFactory.createInclude(
				inheritanceName);
		fileContent.addChild(include);

		// Create the module node using the cascading method
		IUnoComposite topModule = CompositeFactory.createModulesSpaces(path);
		fileContent.addChild(topModule);

		IUnoComposite currentModule = topModule;
		while (currentModule.getChildren().length > 0) {

			// Remain that there should be only zero or one module
			IUnoComposite[] children = currentModule.getChildren();
			if (children.length == 1) {
				currentModule = children[0];
			}
		}

		// Create the service
		IUnoComposite service = CompositeFactory.createService(name,
				published, inheritanceName);
		currentModule.addChild(service);

		// Create all the stuffs
		file.create(true);
		file.dispose();

		// show the generated file
		String filename = typepath.replace("::", "/") + ".idl"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		UnoidlProjectHelper.refreshProject(prj, null);

		IFile serviceFile = prj.getFile(
				prj.getIdlPath().append(filename));

		showFile(serviceFile, activePage);
	}
	
	public static void createInterface(UnoFactoryData data, IUnoidlProject prj, 
			IWorkbenchPage activePage, IProgressMonitor monitor) throws Exception {
		
		// Extract the data
		String path = (String)data.getProperty(
				IUnoFactoryConstants.PACKAGE_NAME);
		path = path.replaceAll("\\.", "::"); //$NON-NLS-1$ //$NON-NLS-2$
		String name = (String)data.getProperty(
				IUnoFactoryConstants.TYPE_NAME);
		String[] interfaces = (String[])data.getProperty(
				IUnoFactoryConstants.INHERITED_INTERFACES);
		String[] opt_interfaces = (String[])data.getProperty(
				IUnoFactoryConstants.OPT_INHERITED_INTERFACES);
		boolean published = ((Boolean)data.getProperty(
				IUnoFactoryConstants.TYPE_PUBLISHED)).booleanValue();
		
		// Create the necessary modules
		UnoidlProjectHelper.createModules(path, prj, null);

		String typepath = path +"::" + name; //$NON-NLS-1$

		// Create the file node
		IUnoComposite file = CompositeFactory.createTypeFile(typepath, prj);

		// Create the file content skeleton
		IUnoComposite fileContent = CompositeFactory.createFileContent(typepath);
		file.addChild(fileContent);

		// Create the includes nodes for each inherited interface
		for (int i=0; i<interfaces.length; i++) {
			fileContent.addChild(CompositeFactory.createInclude(
					interfaces[i]));
		}
	
		// Create the includes nodes for each optional inherited interface
		for (int i=0; i<opt_interfaces.length; i++) {
			fileContent.addChild(CompositeFactory.createInclude(
					opt_interfaces[i]));
		}

		// Create the module node using the cascading method
		IUnoComposite topModule = CompositeFactory.createModulesSpaces(path);
		fileContent.addChild(topModule);

		IUnoComposite currentModule = topModule;
		while (currentModule.getChildren().length > 0) {

			// Remain that there should be only zero or one module
			IUnoComposite[] children = currentModule.getChildren();
			if (children.length == 1) {
				currentModule = children[0];
			}
		}

		IUnoComposite intf = CompositeFactory.createInterface(name,
				published, interfaces);
		currentModule.addChild(intf);

		// Create the optional inheritances
		for (int i=0; i<opt_interfaces.length; i++) {
			IUnoComposite inherit = CompositeFactory.createInterfaceInheritance(
					opt_interfaces[i], true);
			intf.addChild(inherit);
		}

		// Generate all the stuffs
		file.create(true);
		file.dispose();

		// show the generated file
		String filename = typepath.replace("::", "/") + ".idl"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		UnoidlProjectHelper.refreshProject(prj, null);
		IFile interfaceFile = prj.getFile(prj.getIdlPath().
				append(filename));
		showFile(interfaceFile, activePage);
	}
	
	/**
	 * Simply shows the file in the IDE
	 */
	private static void showFile(IFile file, IWorkbenchPage page) {
		
		try {
			IWorkbench workbench = PlatformUI.getWorkbench();
			BasicNewResourceWizard.selectAndReveal(
					file, workbench.getActiveWorkbenchWindow());

			final IWorkbenchPage activePage = page;
			final IFile toShow = file;

			if (activePage != null) {
				final Display display = Display.getDefault();
				if (display != null) {
					display.asyncExec(new Runnable() {
						public void run() {
							try {
								IDE.openEditor(activePage, toShow, true);
							} catch (PartInitException e) {
								PluginLogger.debug(e.getMessage());
							}
						}
					});
				}
			}
		} catch (Exception e) {
			PluginLogger.error("Can't open file", e); //$NON-NLS-1$
		}
	}
}
