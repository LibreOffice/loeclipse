/*************************************************************************
 *
 * $RCSfile: IdlcBuilder.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/06/09 06:14:00 $
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
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.preferences.ISdk;

/**
 * Builder for the IDL files. 
 * 
 * <p>This builder should not be associated directly
 * to a UNO project: the right builder for this is {@link TypesBuilder}. 
 * This builder doesn't make any difference between full and incremental 
 * builds.</p>
 * 
 * @author cbosdonnat
 *
 */
public class IdlcBuilder extends IncrementalProjectBuilder {
	
	/**
	 * Identifier defined in the <code>plugin.xml</code> file for the 
	 * marker associated with this builder errors.
	 */
	public final static String IDLERROR_MARKER_ID = OOEclipsePlugin.OOECLIPSE_PLUGIN_ID + ".idlcerrormarker";
	
	/**
	 * UNOI-IDL project handled. This is a quick access to the project nature 
	 */
	private IUnoidlProject unoidlProject;
	
	/**
	 * Vector of the urd files to delete after the deletion of their idl file reference
	 */
	private Vector urdToDelete = new Vector();
	
	/**
	 * Constructor for the idlc builder
	 *
	 */
	public IdlcBuilder(IUnoidlProject project) {
		super();
		
		unoidlProject = project;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		// Removes the urd to delete
		for (int i=0, length=urdToDelete.size(); i<length; i++){
			IResource resource = (IResource)urdToDelete.get(i);
			if (resource.exists()){
				resource.delete(true, monitor);
			}
		}
		urdToDelete.clear();
			
		fullBuild(monitor);

		return null;
	}

	/**
	 * Method that perform the full build of the project. 
	 * 
	 * <p>It launches the {@link IdlcBuildVisitor} on the idl
	 * folder to build every <code>.idl</code> file.</p>
	 * 
	 * @param monitor progress monitor
	 */
	private void fullBuild(IProgressMonitor monitor) {

		try {
			// compile each idl file
			IFolder idlFolder = unoidlProject.getFolder(
					unoidlProject.getRootModulePath());
			idlFolder.accept(new IdlcBuildVisitor(monitor));
			
		} catch (CoreException e) {
			PluginLogger.getInstance().error(
					OOEclipsePlugin.getTranslationString(
							I18nConstants.IDLC_ERROR), e);
		}
	}
	
	/**
	 * Convenience method to execute the <code>idlc</code> tool on a given
	 * file.
	 * 
	 * @param file the file to run <code>idlc</code> on.
	 * @param monitor a progress monitor
	 */
	static void runIdlcOnFile(IFile file, IProgressMonitor monitor){
		
		IUnoidlProject project = ProjectsManager.getInstance().
				getProject(file.getProject().getName());
		
		ISdk sdk = project.getSdk();
		
		if (null != sdk){
			
			// Get local references to the SDK used members
			String sdkHome = sdk.getHome();
			
			Path sdkPath = new Path(sdkHome);
			int segmentCount = project.getIdlPath().segmentCount();
			
			IPath outputLocation = project.getUrdPath().append(
					file.getProjectRelativePath().removeLastSegments(1).
					removeFirstSegments(segmentCount));
			
			String command = "idlc -O " + outputLocation.toOSString() +
				" -I " + sdkPath.append("idl").toOSString() +
				" -I " + project.getIdlPath().toOSString() + 
				" " + file.getProjectRelativePath().toString(); 
			
			Process process = OOEclipsePlugin.runTool(
					project, command, monitor);
			
			IdlcErrorReader errorReader = new IdlcErrorReader(
					process.getErrorStream(), file);
			errorReader.readErrors();
			
			// Do not forget to destroy the process
			process.destroy();
		}
	}
}
