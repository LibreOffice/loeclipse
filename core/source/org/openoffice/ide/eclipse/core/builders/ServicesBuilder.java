/*************************************************************************
 *
 * $RCSfile: ServicesBuilder.java,v $
 *
 * $Revision: 1.1 $
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * @author cedricbosdo
 *
 */
public class ServicesBuilder extends IncrementalProjectBuilder {

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		monitor.beginTask(Messages.getString("ServicesBuilder.BuildServiceTask"), 2); //$NON-NLS-1$
		
		IUnoidlProject unoProject = ProjectsManager.getInstance().getProject(
				getProject().getName());
		
		// Export the library
		String libraryPath;
		try {
			libraryPath = unoProject.getLanguage().createLibrary(unoProject);
		} catch (Exception e) {
			throw new CoreException(new Status(
					IStatus.ERROR, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
					IStatus.ERROR, Messages.getString("ServicesBuilder.ComponentBuildError"), e)); //$NON-NLS-1$
		}
		monitor.worked(1);
		
		// Get the language specific arguments, eg LD_LIBRARY_PATH or CLASSPATH
		String[] env = unoProject.getLanguage().getBuildEnv(unoProject,
				UnoidlProjectHelper.getProject(unoProject)); 
		
		// Compute regcomp command
		String regcompCmd = "regcomp -register " +  //$NON-NLS-1$
			 "-r " + unoProject.getServicesPath().toOSString() + " " +  //$NON-NLS-1$ //$NON-NLS-2$
			 "-c " + libraryPath; //$NON-NLS-1$
		
		// The normal messages of regcomp are sent to the err stream :(
		Process process = OOEclipsePlugin.runToolWithEnv(unoProject, 
				regcompCmd, env, monitor);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				process.getErrorStream()));
		
		String message = ""; //$NON-NLS-1$
		try {
			String line = reader.readLine();
			while (line != null) {
				message += line;
				line = reader.readLine();
			}
		} catch (IOException e) {
			message = Messages.getString("ServicesBuilder.RegisterError"); //$NON-NLS-1$
		}
		
		if (process.exitValue() == 0) {
			monitor.worked(1);
		} else {
			throw new CoreException(new Status(
					IStatus.ERROR, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
					IStatus.ERROR, message ,null));
		}
			
		return null;
	}
}
