/*************************************************************************
 *
 * $RCSfile: RegmergeBuilder.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/25 19:09:57 $
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
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class RegmergeBuilder extends IncrementalProjectBuilder {
		
	/**
	 * UNOI-IDL project handled. This is a quick access to the project nature 
	 */
	private IUnoidlProject unoidlProject;
	
	/**
	 * Root of the generated types, used by regmerge and javamaker. UCR is chosen for
	 * OpenOffice.org compatibility 
	 */
	public static final String TYPE_ROOT_KEY = "/UCR";
	
	public RegmergeBuilder(IUnoidlProject project){
		unoidlProject = project;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		// Regmerge doesn't work as incremental builder, but
		// allways as full builder
		fullBuild(monitor);

		return null;
	}

	/**
	 * 
	 * @param monitor
	 */
	private void fullBuild(IProgressMonitor monitor) {
		try {
			// The registry file is placed in the root of the project as announced 
			// to the api-dev mailing-list
			IFolder urdFolder = unoidlProject.getFolder(
					unoidlProject.getUrdPath());
			
			
			IFile mergeFile = unoidlProject.getFile(unoidlProject.getTypesPath());
			if (mergeFile.exists()){
				mergeFile.delete(true, monitor);
			}
			
			// compile each idl file
			urdFolder.accept(new RegmergeBuildVisitor(monitor));
			
		} catch (CoreException e) {
			PluginLogger.getInstance().error(
					"Error raised during the regmerge execution", e);
		}
	}
	
	
	static void runRegmergeOnFile(IFile file, IProgressMonitor monitor){
		
		IUnoidlProject project = ProjectsManager.getInstance().getProject(
				file.getProject().getName());
		
		// The registry file is placed in the root of the project as announced 
		// to the api-dev mailing-list
		IFile mergeFile = project.getFile(project.getTypesPath());
		
		String existingReg = "";
		if (mergeFile.exists()){
			existingReg = mergeFile.getProjectRelativePath().toOSString() + " ";
		}
		
		String command = "regmerge types.rdb " + TYPE_ROOT_KEY + " " +
						   existingReg + file.getProjectRelativePath().toOSString();
		
		// Process creation
		Process process = OOEclipsePlugin.runTool(project, command, monitor);
		
		// Just wait for the process to end before destroying it
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// Process has been interrupted by the user
		}
		
		// Do not forget to destroy the process
		process.destroy();
	}
}
