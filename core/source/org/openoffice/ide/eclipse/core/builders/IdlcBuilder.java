/*************************************************************************
 *
 * $RCSfile: IdlcBuilder.java,v $
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
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
public class IdlcBuilder {
	
	/**
	 * Runs the full build
	 * 
	 * @param project the uno project to build
	 * @param monitor a monitor to watch the progress
	 * @throws CoreException if anything wrong happened
	 */
	public static void build(IUnoidlProject project, IProgressMonitor monitor)
			throws CoreException {
		
		// Build each idlc file
		try {
			// compile each idl file
			IFolder idlFolder = project.getFolder(
					project.getRootModulePath());
			idlFolder.accept(new IdlcBuildVisitor(monitor));
			
		} catch (CoreException e) {
			PluginLogger.error(
					Messages.getString("IdlcBuilder.IdlcError"), e); //$NON-NLS-1$
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
			
			String command = "idlc -O " + outputLocation.toOSString() + //$NON-NLS-1$
				" -I " + sdkPath.append("idl").toOSString() + //$NON-NLS-1$ //$NON-NLS-2$
				" -I " + project.getIdlPath().toOSString() +  //$NON-NLS-1$
				" " + file.getProjectRelativePath().toString();  //$NON-NLS-1$
			
			Process process = OOEclipsePlugin.runTool(
					project, command, monitor);
			
			IdlcErrorReader errorReader = new IdlcErrorReader(
					process.getErrorStream(), file);
			errorReader.readErrors();
		}
	}
}
