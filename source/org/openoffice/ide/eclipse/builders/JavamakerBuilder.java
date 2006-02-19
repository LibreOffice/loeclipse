/*************************************************************************
 *
 * $RCSfile: JavamakerBuilder.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/02/19 11:32:40 $
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
package org.openoffice.ide.eclipse.builders;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.model.UnoidlProject;
import org.openoffice.ide.eclipse.preferences.ooo.OOo;
import org.openoffice.ide.eclipse.preferences.sdk.SDK;

public class JavamakerBuilder extends IncrementalProjectBuilder {
	
	/**
	 * Unique Id of the javamaker builder
	 */
	public static final String BUILDER_ID = OOEclipsePlugin.OOECLIPSE_PLUGIN_ID+".javamaker";

	/**
	 * UNOI-IDL project handled. This is a quick access to the project nature 
	 */
	private UnoidlProject unoidlProject;
	
	
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		unoidlProject = (UnoidlProject)getProject().getNature(OOEclipsePlugin.UNO_NATURE_ID);
		
		// Clears the registries before beginning
		removeAllRegistries();
		
		IdlcBuilder idlcBuilder = new IdlcBuilder(unoidlProject);
		idlcBuilder.build(FULL_BUILD, args, monitor);
		
		// Workspace refreshing needed for the next tool
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		RegmergeBuilder regmergeBuilder = new RegmergeBuilder(unoidlProject);
		regmergeBuilder.build(FULL_BUILD, args, monitor);

		// Workspace refreshing needed for the next tool
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		generateJava(monitor);
		
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		return null;
	}
	
	private void removeAllRegistries() {
		
		try {
			IPath rdbPath = unoidlProject.getRegistryPath();
			IFile rdbFile = getProject().getFile(rdbPath);
			if (rdbFile.exists()) {
				rdbFile.delete(true, null);
			}
			
			IPath urdPath = unoidlProject.getUrdLocation();
			IFolder urdFolder = getProject().getFolder(urdPath);
			IResource[] members = urdFolder.members();
			
			for (int i=0, length=members.length; i<length; i++) {
				IResource resi = members[i];
				if (resi.exists()) {
					resi.delete(true, null);
				}
			}
			
		} catch (CoreException e) {
			if (null != System.getProperty("DEBUG")) {
				e.printStackTrace();
			}
		}
	}
	
	private void generateJava(IProgressMonitor monitor) throws CoreException {
		
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		// Get a handle on the types.rdb file
		IFile registryFile = getProject().getFile("types.rdb");
		
		if (registryFile.exists()){
			
			try {
			
				UnoidlProject project = (UnoidlProject)getProject().getNature(OOEclipsePlugin.UNO_NATURE_ID);
				SDK sdk = project.getSdk();
				OOo ooo = project.getOOo();
				
				if (null != sdk && null != ooo){
					
					IPath ooTypesPath = new Path (ooo.getOOoHome()).append("/program/types.rdb");
					
					// TODO Find the first modules using the AST
					String firstModule = project.getUnoidlPrefixPath().segment(1);
					
					// HELP quotes are placed here to prevent Windows path names with spaces
					String command = "javamaker -T" + firstModule + ".* -nD -Gc -BUCR " + 
											"-O ." + System.getProperty("file.separator") + 
											         project.getBuildLocation().toOSString() + " " +
											registryFile.getProjectRelativePath().toOSString() + " " +
											"-X\"" + ooTypesPath.toOSString() + "\"";
					
					
					Process process = OOEclipsePlugin.runTool(getProject(), command, monitor);
					
					LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(process.getErrorStream()));
					
					// Only for debugging purpose
					if (null != System.getProperties().getProperty("DEBUG")){
					
						String line = lineReader.readLine();
						while (null != line){
							System.out.println(line);
							line = lineReader.readLine();
						}
					}
					
					process.waitFor();
				}
			} catch (InterruptedException e) {
				// interrupted process: the code generation failed
			} catch (IOException e) {
				// Error whilst reading the error stream
			}
		}
	}
}
