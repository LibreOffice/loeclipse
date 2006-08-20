/*************************************************************************
 *
 * $RCSfile: TypesBuilder.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/08/20 11:55:51 $
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
package org.openoffice.ide.eclipse.core.builders;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * <p>Main builder of the UNO-IDL projects, it computes the language specific
 * type files and types registry from the <code>idl</code> files. In order
 * to split the work, the different tasks have been split into several builders:
 *   <ul>
 *     <li>{@link IdlcBuilder} generating the urd files from the idl ones</li>
 *     <li>{@link RegmergeBuilder} merging the urd files into the types 
 *     	   registry</li>
 *     <li>{@link org.openoffice.ide.eclipse.core.model.ILanguage#generateFromTypes(ISdk, IOOo, IFile, IFolder, String, IProgressMonitor)}
 *     	   generating the language specific type files</li>
 *   </ul>
 * </p>
 * 
 * @author cbosdonnat
 *
 */
public class TypesBuilder extends IncrementalProjectBuilder {

	/**
	 * The builder ID as set in the <code>plugin.xml</code> file
	 */
	public static final String BUILDER_ID = OOEclipsePlugin.OOECLIPSE_PLUGIN_ID+".types"; //$NON-NLS-1$

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		IUnoidlProject unoproject = ProjectsManager.getInstance().getProject(
				getProject().getName());
		
		// Clears the registries before beginning
		removeAllRegistries(unoproject);
		
		IdlcBuilder.build(unoproject, monitor);
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		RegmergeBuilder.build(unoproject, monitor);
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);

		IFile typesFile = unoproject.getFile(unoproject.getTypesPath());
		IFolder buildFolder = unoproject.getFolder(unoproject.getBuildPath());
		
		unoproject.getLanguage().generateFromTypes(
				unoproject.getSdk(),
				unoproject.getOOo(),
				typesFile, buildFolder,
				unoproject.getRootModule(), monitor);
		
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		return null;
	}
	
	/**
	 * Removes all the registries, ie <code>.urd</code> and 
	 * <code>types.rdb</code> files.
	 * 
	 * @param unoproject the Uno project from which to remove the registries
	 */
	private void removeAllRegistries(IUnoidlProject unoproject) {
		
		try {
			IPath rdbPath = unoproject.getTypesPath();
			IFile rdbFile = getProject().getFile(rdbPath);
			if (rdbFile.exists()) {
				rdbFile.delete(true, null);
			}
			
			IPath urdPath = unoproject.getUrdPath();
			IFolder urdFolder = getProject().getFolder(urdPath);
			IResource[] members = urdFolder.members();
			
			for (int i=0, length=members.length; i<length; i++) {
				IResource resi = members[i];
				if (resi.exists()) {
					resi.delete(true, null);
				}
			}
			
		} catch (CoreException e) {
			PluginLogger.debug(e.getMessage());
		}
	}
}
