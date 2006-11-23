/*************************************************************************
 *
 * $RCSfile: UreLaunchDelegate.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/23 18:27:19 $
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
package org.openoffice.ide.eclipse.core.launch;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.openoffice.ide.eclipse.core.builders.ServicesBuilder;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

public class UreLaunchDelegate extends LaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		monitor.beginTask(MessageFormat.format("{0}...", new Object[]{configuration.getName()}), 3); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		
		String prjName = configuration.getAttribute(
				IUreLaunchConstants.PROJECT_NAME, ""); //$NON-NLS-1$
		String mainName = configuration.getAttribute(
				IUreLaunchConstants.MAIN_TYPE, ""); //$NON-NLS-1$
		String args = configuration.getAttribute(
				IUreLaunchConstants.PROGRAM_ARGS, ""); //$NON-NLS-1$
		
		IUnoidlProject prj = ProjectsManager.getInstance().getProject(prjName);
		if (prj != null) {
			
			// creates the services.rdb file
			Status status = ServicesBuilder.syncRun(prj, monitor);
			
			if (status.getSeverity() == IStatus.OK) {
				// Run the URE Applicaton using IOOo.runUno()
				prj.getOOo().runUno(prj, mainName, args, launch, monitor);
			} else {
				Display.getDefault().asyncExec(new Runnable(){

					public void run() {
						MessageDialog.openError(Display.getDefault().getActiveShell(),
								Messages.getString("UreLaunchDelegate.ErrorTitle"),  //$NON-NLS-1$
								Messages.getString("UreLaunchDelegate.ErrorMessage")); //$NON-NLS-1$	
					}
				});
			}
		}
	}

}
