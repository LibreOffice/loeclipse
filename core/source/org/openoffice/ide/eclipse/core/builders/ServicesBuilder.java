/*************************************************************************
 *
 * $RCSfile: ServicesBuilder.java,v $
 *
 * $Revision: 1.7 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/08/15 12:27:11 $
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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;

/**
 * @author cedricbosdo
 *
 */
public class ServicesBuilder extends Job{
	
	private IProject mProject;
	
	
	public ServicesBuilder (IProject project) {
		super(Messages.getString("ServicesBuilder.CreationJob")); //$NON-NLS-1$
		setPriority(Job.BUILD);
		mProject = project;
	}

	protected IStatus run(IProgressMonitor monitor) {

		monitor.beginTask(Messages.getString("ServicesBuilder.BuildServiceTask"), 2); //$NON-NLS-1$
		Status status = new Status(IStatus.OK, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
				IStatus.OK, "", null); //$NON-NLS-1$
		
		IUnoidlProject unoProject = ProjectsManager.getProject(
				mProject.getName());
		
		if (unoProject != null) {
			status = syncRun(unoProject, monitor);
			
		} else {
			status = new Status(
					IStatus.ERROR, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
					IStatus.ERROR, 
					Messages.getString("ServicesBuilder.NotIdlProjectError"), null); //$NON-NLS-1$
		}

		return status;
	}
	
	public static Status syncRun(IUnoidlProject unoProject, IProgressMonitor monitor) {
		
		Status status = new Status(IStatus.OK, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
				IStatus.OK, "", null); //$NON-NLS-1$
		
		// Export the library
		IPath libraryPath = null;
		ILanguageBuilder langBuilder = unoProject.getLanguage().getLanguageBuidler();
		try {
			libraryPath = langBuilder.createLibrary(unoProject);
		} catch (Exception e) {
			return new Status(
					IStatus.ERROR, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
					IStatus.ERROR, 
					Messages.getString("ServicesBuilder.ComponentBuildError"), e); //$NON-NLS-1$
		}
		monitor.worked(1);

		// Get the language specific arguments, eg LD_LIBRARY_PATH or CLASSPATH
		String[] env = langBuilder.getBuildEnv(unoProject); 

		String libraryUrl = libraryPath.toOSString();
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			libraryUrl = "/" + libraryUrl;
		}
		
		String libpath = "file://" + libraryUrl; //$NON-NLS-1$
		libpath = libpath.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
		libpath = libpath.replace('\\', '/');

		String regcomp = "regcomp.bin"; //$NON-NLS-1$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			regcomp = "regcomp"; //$NON-NLS-1$
		}

		// Compute regcomp command
		String regcompCmd = regcomp + " -register " +  //$NON-NLS-1$
			"-r \"" + unoProject.getServicesPath().toOSString() + "\" " +  //$NON-NLS-1$ //$NON-NLS-2$
			"-br \"" + unoProject.getOOo().getTypesPath() + "\" " + //$NON-NLS-1$ //$NON-NLS-2$
			"-br \"" + unoProject.getOOo().getServicesPath() + "\" " + //$NON-NLS-1$ //$NON-NLS-2$
			"-c " + libpath; //$NON-NLS-1$

		// The normal messages of regcomp are sent to the err stream :(
		Process process = unoProject.getSdk().runToolWithEnv(unoProject, 
				regcompCmd, env, monitor);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				process.getErrorStream()));

		String message = ""; //$NON-NLS-1$
		try {
			String line = reader.readLine();
			while (line != null) {
				message += line + "\n"; //$NON-NLS-1$
				line = reader.readLine();
			}
		} catch (IOException e) {
			message = Messages.getString("ServicesBuilder.OuputReadError"); //$NON-NLS-1$
		}

		// Check if there was an error
		if (!message.endsWith("succesful!\n")) { //$NON-NLS-1$
			status = new Status(
					IStatus.ERROR, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
					IStatus.ERROR, message, null);

			// removes the wrong services.rdb file
			IFile servicesFile = unoProject.getFile(unoProject.getServicesPath());
			File services = new File(servicesFile.getLocation().toOSString());
			if (services.canWrite()) {
				services.delete();
			}
		} else {
			monitor.worked(1);
		}

		UnoidlProjectHelper.refreshProject(unoProject, monitor);
		return status;
	}
}
