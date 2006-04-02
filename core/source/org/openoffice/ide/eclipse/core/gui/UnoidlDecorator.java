/*************************************************************************
 *
 * $RCSfile: UnoidlDecorator.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/02 20:13:06 $
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
package org.openoffice.ide.eclipse.core.gui;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class UnoidlDecorator extends LabelProvider implements ILabelDecorator {

	/**
	 * Methods that returns the idl icons for idl resources
	 */
	public Image decorateImage(Image image, Object element) {
		
		
		Image newImage = null;
		
		if (isIdlFolder(element)){
			newImage = OOEclipsePlugin.getImage(ImagesConstants.IDL_FOLDER);
		} else if (element instanceof IProject){
			IProject project = (IProject)element;
			try {
				if (project.hasNature(OOEclipsePlugin.UNO_NATURE_ID)){
					newImage = OOEclipsePlugin.getImage(ImagesConstants.UNO_PROJECT);
				}
			} catch (CoreException e) {
				// Nothing to do: no uno nature found
			}
		} else if (isDbFolder(element)){
			newImage = OOEclipsePlugin.getImage(ImagesConstants.DB_FOLDER);
		}
		
		return newImage;
	}

	/**
	 * Replace the "." by "/". It avoid the package explorer to show it as a package
	 */
	public String decorateText(String text, Object element) {
		
		if (isIdlFolder(element)){
			
			text = text.replaceAll("\\.", "/");
		}
		
		return text;
	}

	/**
	 * Tests if the element is a folder containing the IDL_FOLDER persistent property
	 * 
	 * @param element element to check
	 * @return <code>true</code> if the element if a folder a possess the IDL_FOLDER
	 *         persistent property, <code>false</code> otherwise.
	 */
	private boolean isIdlFolder(Object element){
		boolean result = false;
		
		/** 
		 * If the element is a folder that has the property
		 * IDL_FOLDER set to <code>true</code>, apply the IDL_FOLDER icon
         */
		if (element instanceof IResource){
			IResource resource = (IResource)element;

			try {
				if (IResource.FOLDER == resource.getType()){
					String propertyValue = resource.getPersistentProperty(
							new QualifiedName(OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
										      IUnoidlProject.IDL_FOLDER)); 
					
					if (null != propertyValue && propertyValue.equals("true")){
						result = true;
					}
				}
			} catch (CoreException e){
				result = false;
			}
		}
		
		return result;
	}
	
	/**
	 * Tests if the elements is the urd folder of a unoidl project
	 * or one of its children.
	 * 
	 * @param element the element to test
	 * @return <code>true</code> if the element is the urd folder of a unoidl project
	 *         or one of its children. Otherwise of if the element is a urd child but
	 *         not a folder, <code>false</code> is returned
	 */
	private boolean isDbFolder (Object element){
		boolean result = false;
		
		if (element instanceof IFolder) {
			
			try {
				IFolder folder = (IFolder)element;
			
				IUnoidlProject project = ProjectsManager.getInstance().getProject(
						folder.getProject().getName());
				
				if (folder.getProjectRelativePath().toString().startsWith(
						project.getUrdPath().toString())) {
					
					result = true;
				}
			} catch (Exception e) {
				result = false;
			}
			
		}
		
		return result;
	}
}
