/*************************************************************************
 *
 * $RCSfile: NewInterfaceWizardPage.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/11/27 17:48:22 $
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
package org.openoffice.ide.eclipse.wizards;

import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.gui.InterfacesTable;
import org.openoffice.ide.eclipse.gui.rows.BooleanRow;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.model.Declaration;
import org.openoffice.ide.eclipse.model.Include;
import org.openoffice.ide.eclipse.model.Interface;
import org.openoffice.ide.eclipse.model.InterfaceInheritance;
import org.openoffice.ide.eclipse.model.ScopedName;
import org.openoffice.ide.eclipse.model.TreeNode;
import org.openoffice.ide.eclipse.model.UnoidlFile;
import org.openoffice.ide.eclipse.model.UnoidlModel;
import org.openoffice.ide.eclipse.model.UnoidlProject;
import org.openoffice.ide.eclipse.unotypebrowser.InternalUnoType;
import org.openoffice.ide.eclipse.unotypebrowser.UnoTypeProvider;

public class NewInterfaceWizardPage extends NewScopedElementWizardPage 
									implements ISelectionChangedListener{

	public NewInterfaceWizardPage(String pageName, UnoidlProject unoProject) {
		super(pageName, unoProject);
	}

	public NewInterfaceWizardPage(String pageName, UnoidlProject project,
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
		publishedRow.setValue(true);
		
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
	
	public Interface createInterface() {
		
		Interface newInterface = null;
		
		try {
			String path = unoProject.getPath() + unoProject.getSeparator() +
							unoProject.getRootScopedName();
			
			if (!getPackage().equals("")) {
				path = path + Declaration.SEPARATOR + getPackage();
			}
			
			unoProject.createModules(
					new ScopedName(unoProject.getRootScopedName() + 
							ScopedName.SEPARATOR + getPackage()),
					null);
			
			TreeNode parent = UnoidlModel.getUnoidlModel().findNode(path);
			
			// Perform only if the parent exists
			if (null != parent) {
				
				// Creates a file and the necessary folders for this new type
				String[] segments = getPackage().split("\\.");
				
				IFolder folder = unoProject.getProject().getFolder(
						unoProject.getUnoidlPrefixPath());
				
				for (int i=0; i<segments.length; i++) {
					folder = folder.getFolder(segments[i]);
					if (!folder.exists()) {
						folder.create(true, true, null);
					}
				}
				
				IFile file = folder.getFile(getElementName() + ".idl");
				TreeNode fileNode = unoProject.findNode(
						UnoidlFile.computePath(file));
				UnoidlFile unofile = null;
				
				if (null == fileNode){
					unofile = new UnoidlFile(unoProject, file);
				} else {
					unofile = (UnoidlFile)fileNode;
				}
				
				// Create the new interface object
				newInterface = new Interface(parent, 
						getElementName(), 
						unofile);
				newInterface.setPublished(publishedRow.getBooleanValue());
				
				// Add the selected interface inheritances as children of the 
				// new interface created
				
				Vector lines = interfaceInheritances.getLines();
				for (int i=0, length=lines.size(); i<length; i++) {
					InterfacesTable.InheritanceLine line = 
						(InterfacesTable.InheritanceLine)lines.get(i);
					
					InterfaceInheritance inheritance = new InterfaceInheritance(
							newInterface,
							new ScopedName(line.getInterfaceName().replace(".", "::")),
							unofile,
							line.isOptional());
					newInterface.addNode(inheritance);
					
					// Check whether the interface is a project one or not.
					InternalUnoType type = typesProvider.getType(
							line.getInterfaceName());
					
					boolean isLibrary = false;
					if (null != type) {
						isLibrary = !type.isLocalType();
					}
					
					// Add the correct include to the file
					unofile.addInclude(
							Include.createInclude(
									line.getInterfaceName().replace(".", "::"),
									isLibrary));
				}
				
				parent.addNode(newInterface);
				unoProject.addNode(unofile);
				
				unofile.addDeclaration(newInterface);
				unofile.save();
			}
		} catch (Exception e) {
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
					I18nConstants.SERVICE_CREATION_FAILED), e);
		}
		
		return newInterface;
	}
}
