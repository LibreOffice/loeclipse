/*************************************************************************
 *
 * $RCSfile: NewInterfaceWizardPage.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/25 19:10:00 $
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

import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.InterfacesTable;
import org.openoffice.ide.eclipse.core.gui.rows.BooleanRow;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoComposite;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.UnoFactory;
import org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypeProvider;

public class NewInterfaceWizardPage extends NewScopedElementWizardPage 
									implements ISelectionChangedListener{

	public NewInterfaceWizardPage(String pageName, IUnoidlProject unoProject) {
		super(pageName, unoProject);
	}

	public NewInterfaceWizardPage(String pageName, IUnoidlProject project,
			String aRootName, String aElementName) {
		super(pageName, project, aRootName, aElementName);
	}
	
	public void dispose() {
		
		interfaceInheritances.removeSelectionChangedListener(this);
		interfaceInheritances = null;
		
		super.dispose();
	}
	
	public int getProvidedTypes() {
		return UnoTypeProvider.INTERFACE;
	}

	
	//--------------------------------------------------- Page content managment

	private final static String P_PUBLISHED = "__published";
	private BooleanRow publishedRow;
	private InterfacesTable interfaceInheritances;
	
	protected void createSpecificControl(Composite parent) {
		
		publishedRow = new BooleanRow(parent, P_PUBLISHED,
				OOEclipsePlugin.getTranslationString(
						I18nConstants.PUBLISHED));
		publishedRow.setFieldChangedListener(this);
		
		Composite tableParent = new Composite(parent, SWT.NORMAL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		tableParent.setLayoutData(gd);
		tableParent.setLayout(new GridLayout(1, false));
		
		interfaceInheritances = new InterfacesTable(tableParent,
				 new UnoTypeProvider(unoProject, UnoTypeProvider.INTERFACE));
		interfaceInheritances.addInterface(
				"com.sun.star.uno.XInterface", false); // TODO configuration
		interfaceInheritances.addSelectionChangedListener(this);
	}
	
	public String getTitle() {
		return OOEclipsePlugin.getTranslationString(
				I18nConstants.NEW_INTERFACE_TITLE);
	}
	
	public String getDescription() {
		return "";
	}
	
	protected String getTypeLabel() {
		return OOEclipsePlugin.getTranslationString(I18nConstants.INTERFACE_NAME);
	}
	
	protected ImageDescriptor getImageDescriptor() {
		return OOEclipsePlugin.getImageDescriptor(
				ImagesConstants.NEW_INTERFACE_IMAGE);
	}

	/*
	 * 	Override isPageComplete to be sure to have at least one inheritance
	 *  
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#isPageComplete()
	 */
	public boolean isPageComplete() {
		return super.isPageComplete() && 
			interfaceInheritances.getLines().size() >= 1;
	}

	/*
	 * When such an event is catch, this method reevaluate the page completeness
	 * 
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		setPageComplete(isPageComplete());
	}
	
	public IFile createInterface() {
		
		IFile interfaceFile = null;
		
		try {
			
			String path = unoProject.getRootModule();

			if (!getPackage().equals("")) {
				path = path + "::" + getPackage();
			}
			
			// Create the necessary modules
			UnoidlProjectHelper.createModules(path, unoProject, null);
			
			String typepath = path +"::" + getElementName();
			
			// Create the file node
			IUnoComposite file = UnoFactory.createTypeFile(typepath, unoProject);
			
			// Create the file content skeleton
			IUnoComposite fileContent = UnoFactory.createFileContent(typepath);
			file.addChild(fileContent);
			
			// Vector containing the interface inheritance paths "::" separated
			Vector optionalIntf = new Vector();
			Vector mandatoryIntf = new Vector();
			
			// Separate the optional and mandatory interface inheritances			
			Vector lines = interfaceInheritances.getLines();
			for (int i=0, length=lines.size(); i<length; i++) {
				InterfacesTable.InheritanceLine line = 
					(InterfacesTable.InheritanceLine)lines.get(i);
				
				if (line.isOptional()) {
					optionalIntf.add(line.getInterfaceName().replace(".", "::"));
				} else {
					mandatoryIntf.add(line.getInterfaceName().replace(".", "::"));
				}
				
				// Create the includes nodes for each inherited interface
				fileContent.addChild(UnoFactory.createInclude(
						line.getInterfaceName().replace(".", "::")));
			}
			
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
			
			// Create the interface with its mandatory inheritances
			String[] interfaces = new String[mandatoryIntf.size()];
			for (int i=0, length=mandatoryIntf.size(); i<length; i++) {
				interfaces[i] = (String)mandatoryIntf.get(i);
			}
			
			IUnoComposite intf = UnoFactory.createInterface(getElementName(),
					publishedRow.getBooleanValue(), interfaces);
			currentModule.addChild(intf);
			
			// Create the optional inheritances
			for (int i=0, length=optionalIntf.size(); i<length; i++) {
				IUnoComposite inherit = UnoFactory.createInterfaceInheritance(
						(String)optionalIntf.get(i), true);
				intf.addChild(inherit);
			}
			
			// Generate all the stuffs
			file.create(true);
			
			// Returns the IFile to the generated file
			String filename = typepath.replace("::", "/") + ".idl";
			
			UnoidlProjectHelper.refreshProject(unoProject, null);
	
			interfaceFile = unoProject.getFile(
					unoProject.getIdlPath().append(filename));
			
		} catch (Exception e) {
			PluginLogger.getInstance().error(OOEclipsePlugin.getTranslationString(
					I18nConstants.SERVICE_CREATION_FAILED), e);
		}

		return interfaceFile;
	}
}
