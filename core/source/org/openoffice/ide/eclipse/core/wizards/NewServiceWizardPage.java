/*************************************************************************
 *
 * $RCSfile: NewServiceWizardPage.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/06/09 06:14:03 $
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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.rows.BooleanRow;
import org.openoffice.ide.eclipse.core.gui.rows.TypeRow;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoComposite;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.UnoFactory;
import org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypeProvider;

/**
 * Service creation wizard page. This page is based on the 
 * {@link NewScopedElementWizardPage}.
 * 
 * @author cbosdonnat
 */
public class NewServiceWizardPage extends NewScopedElementWizardPage {
	
	/**
	 * Simple constructor setting the package root and element name to 
	 * blank values.
	 * 
	 * @param pageName the page name
	 * @param project the project where to create the service
	 */
	public NewServiceWizardPage(String pageName, IUnoidlProject project) {
		super(pageName, project);
	}
	
	/**
	 * Constructor setting Allowing to set custom root package and service
	 * name
	 * 
	 * @param pageName the page name
	 * @param project the project where to create the service
	 * @param aRootName the project root namespace
	 * @param aServiceName the default service name 
	 */
	public NewServiceWizardPage(String pageName, IUnoidlProject project, 
								String aRootName, String aServiceName){
		super(pageName, project, aRootName, aServiceName);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.NewScopedElementWizardPage#getProvidedTypes()
	 */
	public int getProvidedTypes() {
		return UnoTypeProvider.INTERFACE;
	}

	//-------------------------------------------------- Page content managment
	
	private final static String P_IFACE_INHERITANCE = "__iface_inheritance"; 
	private final static String P_PUBLISHED			= "__published";
	
	private TypeRow ifaceInheritanceRow;
	private BooleanRow publishedRow;
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.NewScopedElementWizardPage#createSpecificControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createSpecificControl(Composite parent) {
		
		ifaceInheritanceRow = new TypeRow(parent, 
				P_IFACE_INHERITANCE, 
				"Inherited interface",
				typesProvider,
				UnoTypeProvider.INTERFACE);
		ifaceInheritanceRow.setValue("com.sun.star.uno.XInterface"); // TODO Configure
		ifaceInheritanceRow.setFieldChangedListener(this);
		
		
		publishedRow = new BooleanRow(parent, P_PUBLISHED,
				OOEclipsePlugin.getTranslationString(
						I18nConstants.PUBLISHED));
		publishedRow.setFieldChangedListener(this);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
	 */
	public String getTitle() {
		return OOEclipsePlugin.getTranslationString(
				I18nConstants.NEW_SERVICE_TITLE);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getDescription()
	 */
	public String getDescription() {
		return "";
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.NewScopedElementWizardPage#getTypeLabel()
	 */
	protected String getTypeLabel() {
		return OOEclipsePlugin.getTranslationString(I18nConstants.SERVICE_NAME);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.NewScopedElementWizardPage#getImageDescriptor()
	 */
	protected ImageDescriptor getImageDescriptor() {
		return OOEclipsePlugin.getImageDescriptor(
				ImagesConstants.NEW_SERVICE_IMAGE);
	}
	
	/**
	 * Gets the name of the exported interface
	 */
	public String getInheritanceName() {
		return ifaceInheritanceRow.getValue();
	}
	
	/**
	 * Returns whether the service is published or not
	 */
	public boolean isPublished() {
		return publishedRow.getBooleanValue();
	}
	
	/**
	 * Sets the name of the exported interface
	 */
	public void setInheritanceName(String value, boolean forced) {
		
		if (value.matches("[a-zA-Z0-9_]+(.[a-zA-Z0-9_])*")) {
			ifaceInheritanceRow.setValue(value);
			ifaceInheritanceRow.setEnabled(!forced);	
		}
	}
	
	/**
	 * Sets whether the service is published or not
	 */
	public void setPublished(boolean value, boolean forced) {
		
		publishedRow.setValue(value);
		publishedRow.setEnabled(!forced);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#isPageComplete()
	 */
	public boolean isPageComplete() {
		boolean result = super.isPageComplete(); 
		
		try {
			if (ifaceInheritanceRow.getValue().equals("")) {
				result = false;
			}
		} catch (NullPointerException e) {
			result = false;
		}
		
		return result;
	}
	
	/**
	 * Create a service
	 * 
	 * @param packageName the name of the container
	 * @param name the name of the service
	 * @param inheritanceName the name of the exported interface
	 * @param published <code>true</code> if the service is published
	 * 
	 * @return the handle on the created file containing the service
	 */
	public IFile createService(String packageName, String name,
			String inheritanceName, boolean published){
		
		IFile serviceFile = null;
		
		try {
			
			String path = unoProject.getRootModule();

			if (!getPackage().equals("")) {
				path = path + "::" + packageName;
			}
			
			// Create the necessary modules
			UnoidlProjectHelper.createModules(path, unoProject, null);
			
			String typepath = path +"::" + name;
			
			// Create the file node
			IUnoComposite file = UnoFactory.createTypeFile(typepath, unoProject);
			
			// Create the file content skeleton
			IUnoComposite fileContent = UnoFactory.createFileContent(typepath);
			file.addChild(fileContent);
			
			// Add the include line for the inheritance interface
			IUnoComposite include = UnoFactory.createInclude(
					inheritanceName);
			fileContent.addChild(include);
			
			// Create the module node using the cascading method
			IUnoComposite topModule = UnoFactory.createModulesSpaces(path);
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
			IUnoComposite service = UnoFactory.createService(name,
					published, inheritanceName);
			currentModule.addChild(service);
			
			// Create all the stuffs
			file.create(true);
			
			// Return the IFile to the generated file
			String filename = typepath.replace("::", "/") + ".idl";
			
			UnoidlProjectHelper.refreshProject(unoProject, null);
			
			serviceFile = unoProject.getFile(
					unoProject.getIdlPath().append(filename));
			
		} catch (Exception e) {
			PluginLogger.getInstance().error(OOEclipsePlugin.getTranslationString(
					I18nConstants.SERVICE_CREATION_FAILED), e);
		}
		
		return serviceFile;
	}
}
