/*************************************************************************
 *
 * $RCSfile: UnoFactory.java,v $
 *
 * $Revision: 1.3 $
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
package org.openoffice.ide.eclipse.core.internal.model;

import java.io.InputStream;
import java.io.StringWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.CompositeFactory;
import org.openoffice.ide.eclipse.core.model.IUnoComposite;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.model.language.ILanguage;
import org.openoffice.ide.eclipse.core.model.language.IProjectHandler;

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
			Integer type = (Integer)inner.getProperty(IUnoFactoryConstants.TYPE_NATURE);
			switch (type.intValue()) {
				// TODO This switch has to be extended to support other types
				case IUnoFactoryConstants.SERVICE:
					createService(inner, prj, page, monitor, false);
					break;
				case IUnoFactoryConstants.INTERFACE:
					createInterface(inner, prj, page, monitor, false);
					break;
			}
		}
		
		UnoidlProjectHelper.refreshProject(prj, null);
		UnoidlProjectHelper.forceBuild(prj, monitor);
		
		// create the language-specific part
		ILanguage language = (ILanguage)data.getProperty(
				IUnoFactoryConstants.PROJECT_LANGUAGE);
		language.getProjectHandler().configureProject(data);
		
		// generate the implementation skeleton
		makeSkeleton(data, prj, page, monitor);
		
		UnoidlProjectHelper.setProjectBuilders(prj, monitor);
	}
	
	/**
	 * Creates a new component implementation skeleton from the project factory 
	 * data and opens the generated file. This method executes the 
	 * uno-skeletonmaker.
	 * 
	 * @param data the project data for which to create the component 
	 * 			implementation skeleton. 
	 * @param prj the project instance
	 * @param activePage the page in which to open the created file 
	 * @param monitor the progress monitor to report the operation progress
	 * @throws Exception is thrown if anything wrong happens
	 */
	public static void makeSkeleton(UnoFactoryData data, IUnoidlProject prj, 
			IWorkbenchPage activePage, IProgressMonitor monitor) throws Exception {
		
		IProjectHandler langProjectHandler = ((ILanguage)data.getProperty(
				IUnoFactoryConstants.PROJECT_LANGUAGE)).getProjectHandler();
		String languageOption = langProjectHandler.getSkeletonMakerLanguage(data);
		
		if (languageOption != null) {
			
			// Get the registries
			String oooTypes = prj.getOOo().getTypesPath();
			oooTypes = oooTypes.replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
			oooTypes = oooTypes.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
			oooTypes = "file:///" + oooTypes; //$NON-NLS-1$
			
			String prjTypes = prj.getTypesPath().toString();
			String typesReg = "-l " + oooTypes + " -l " + prjTypes; //$NON-NLS-1$ //$NON-NLS-2$

			// Get the unorc file
			String unorc = "-env:BOOTSTRAPINI=\"" + prj.getOOo().getUnorcPath() + "\""; //$NON-NLS-1$ //$NON-NLS-2$

			UnoFactoryData[] inner =  data.getInnerData();
			String types = " "; //$NON-NLS-1$
			for (int i=0; i<inner.length; i++) {
				try {
					int type = ((Integer)inner[i].getProperty(IUnoFactoryConstants.TYPE_NATURE)).intValue();
					if (type == IUnoFactoryConstants.SERVICE) {
						String name = (String)inner[i].getProperty(IUnoFactoryConstants.TYPE_NAME);
						String module = (String)inner[i].getProperty(IUnoFactoryConstants.PACKAGE_NAME);

						String fullname = module + "::" + name; //$NON-NLS-1$
						fullname = fullname.replaceAll("::", "."); //$NON-NLS-1$ //$NON-NLS-2$

						types += " -t " + fullname; // $NON-NLS-1$ //$NON-NLS-1$
					}
				} finally {}
			}
			
			String implementationName = langProjectHandler.getImplementationName(data);
			
			String command = "uno-skeletonmaker" +    //$NON-NLS-1$
			" " + unorc +  //$NON-NLS-1$
			" component " + languageOption +  //$NON-NLS-1$
			" -o ./" + prj.getSourcePath().toOSString() +  // Works even on windows //$NON-NLS-1$
			" " + typesReg + //$NON-NLS-1$
			" -n " + implementationName + //$NON-NLS-1$
			types;

			Process process = OOEclipsePlugin.runTool(prj, command, monitor);
	
			InputStream err = process.getErrorStream();
			StringWriter writer = new StringWriter();
			
			try {
				int c = err.read();
				while (c != -1) {
					writer.write(c);
					c = err.read();
				}
			} finally {
				try {
					err.close();
					String error = writer.toString();
					if (!error.equals(""))  //$NON-NLS-1$
						PluginLogger.error(error);
					else
						PluginLogger.info(Messages.getString("UnoFactory.SkeletonGeneratedMessage") +  //$NON-NLS-1$
								langProjectHandler.getImplementationName(data));
				} catch (java.io.IOException e) { }
			}
			
			UnoidlProjectHelper.refreshProject(prj, null);

			// opens the generated files
			IPath implementationPath = langProjectHandler.getImplementationFile(implementationName);
			implementationPath = prj.getSourcePath().append(implementationPath);
			IFile implementationFile = prj.getFile(implementationPath);

			showFile(implementationFile, activePage);
		}
	}
	
	/**
	 * Creates a service from its factory data and opens the created file.
	 * 
	 * @param data the data describing the service
	 * @param prj the uno project that will contain the service
	 * @param activePage the page in which to open the created file
	 * @param monitor the progress monitor to report the operation progress
	 * @throws Exception is thrown if anything wrong happens
	 */
	public static void createService(UnoFactoryData data, IUnoidlProject prj, 
			IWorkbenchPage activePage, IProgressMonitor monitor) throws Exception {
		createService(data, prj, activePage, monitor, true);
	}
	
	/**
	 * Creates a service from its factory data. The created file can be opened
	 * if <code>openFile</code> is set to <code>true</code>.
	 * 
	 * @param data the data describing the service
	 * @param prj the uno project that will contain the service
	 * @param activePage the page in which to open the created file
	 * @param monitor the progress monitor to report the operation progress
	 * @param openFile opens the created file if set to <code>true</code>
	 * @throws Exception is thrown if anything wrong happens
	 */
	public static void createService(UnoFactoryData data, IUnoidlProject prj, 
			IWorkbenchPage activePage, IProgressMonitor monitor, boolean openFile) throws Exception {

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

		if (openFile) {
			IFile serviceFile = prj.getFile(
					prj.getIdlPath().append(filename));

			showFile(serviceFile, activePage);
		}
	}
	
	/**
	 * Creates an interface from its factory data and opens the created file.
	 * 
	 * @param data the data describing the interface
	 * @param prj the uno project that will contain the interface
	 * @param activePage the page in which to open the created file
	 * @param monitor the progress monitor to report the operation progress
	 * @throws Exception is thrown if anything wrong happens
	 */
	public static void createInterface(UnoFactoryData data, IUnoidlProject prj, 
			IWorkbenchPage activePage, IProgressMonitor monitor)
		throws Exception {
		createInterface(data, prj, activePage, monitor, true);
	}
	
	/**
	 * Creates an interface from its factory data. The created file can be opened
	 * if <code>openFile</code> is set to <code>true</code>.
	 * 
	 * @param data the data describing the interface
	 * @param prj the uno project that will contain the interface
	 * @param activePage the page in which to open the created file
	 * @param monitor the progress monitor to report the operation progress
	 * @param openFile opens the created file if set to <code>true</code>
	 * @throws Exception is thrown if anything wrong happens
	 */
	public static void createInterface(UnoFactoryData data, IUnoidlProject prj, 
			IWorkbenchPage activePage, IProgressMonitor monitor, boolean openFile)
		throws Exception {
		
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
		
		if (0 == interfaces.length && 0 == opt_interfaces.length) {
			interfaces = new String[]{"com::sun::star::uno::XInterface"}; //$NON-NLS-1$
		} else if (0 == interfaces.length && 0 < opt_interfaces.length) {
			interfaces = new String[]{opt_interfaces[0]};
			
			// Remove the first optional interface
			String[] new_opt_interfaces = new String[opt_interfaces.length - 1];
			System.arraycopy(opt_interfaces, 1, 
					new_opt_interfaces, 0, new_opt_interfaces.length);
			opt_interfaces = new_opt_interfaces;
		}
		
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

		// Creates all the members
		for (UnoFactoryData memberData : data.getInnerData()) {
			
			// Get the member type: Attribute or Method
			try {
				Integer memberType = (Integer)memberData.getProperty(IUnoFactoryConstants.MEMBER_TYPE);
				if (memberType.intValue() == IUnoFactoryConstants.ATTRIBUTE) {
					// create the method composite
					String attrName = (String)memberData.getProperty(IUnoFactoryConstants.NAME);
					String type = (String)memberData.getProperty(IUnoFactoryConstants.TYPE);
					String flags = (String)memberData.getProperty(IUnoFactoryConstants.FLAGS);
					intf.addChild(CompositeFactory.createAttribute(attrName, type, flags));
				} else if (memberType.intValue() == IUnoFactoryConstants.METHOD) {
					// create the attribute composite
					String methodName = (String)memberData.getProperty(IUnoFactoryConstants.NAME);
					String type = (String)memberData.getProperty(IUnoFactoryConstants.TYPE);
					IUnoComposite method = CompositeFactory.createMethod(methodName, type);
					for (UnoFactoryData argData : memberData.getInnerData()) {
						String argName = (String)argData.getProperty(IUnoFactoryConstants.NAME);
						String argType = (String)argData.getProperty(IUnoFactoryConstants.TYPE);
						String direction = (String)argData.getProperty(IUnoFactoryConstants.ARGUMENT_INOUT);
						method.addChild(CompositeFactory.createMethodArgument(argName, argType, direction));
					}
					intf.addChild(method);
				}
			} catch (NullPointerException e) {
				// just avoid the wrong member
			}
		}
		
		// Generate all the stuffs
		file.create(true);
		file.dispose();

		// show the generated file
		if (openFile) {
			String filename = typepath.replace("::", "/") + ".idl"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			UnoidlProjectHelper.refreshProject(prj, null);
			IFile interfaceFile = prj.getFile(prj.getIdlPath().
					append(filename));
			showFile(interfaceFile, activePage);
		}
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
