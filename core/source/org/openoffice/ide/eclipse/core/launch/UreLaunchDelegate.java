/*************************************************************************
 *
 * $RCSfile: UreLaunchDelegate.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:32 $
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
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

/**
 * This class launches the URE application from its configuration.
 * 
 * @author cedricbosdo
 *
 */
public class UreLaunchDelegate extends LaunchConfigurationDelegate {

    private static final int TASK_UNITS = 3;

    /**
     * {@inheritDoc}
     */
    public void launch(ILaunchConfiguration pConfiguration, String pMode, 
            ILaunch pLaunch, IProgressMonitor pMonitor) throws CoreException {
        
        if (pMonitor == null) {
            pMonitor = new NullProgressMonitor();
        }
        
        pMonitor.beginTask(MessageFormat.format("{0}...", 
                new Object[]{pConfiguration.getName()}), TASK_UNITS); //$NON-NLS-1$
        // check for cancellation
        if (pMonitor.isCanceled()) {
            return;
        }
        
        String prjName = pConfiguration.getAttribute(
                IUreLaunchConstants.PROJECT_NAME, ""); //$NON-NLS-1$
        String mainName = pConfiguration.getAttribute(
                IUreLaunchConstants.MAIN_TYPE, ""); //$NON-NLS-1$
        String args = pConfiguration.getAttribute(
                IUreLaunchConstants.PROGRAM_ARGS, ""); //$NON-NLS-1$
        
        IUnoidlProject prj = ProjectsManager.getProject(prjName);
        if (prj != null) {
            
            // creates the services.rdb file
            Status status = ServicesBuilder.syncRun(prj, pMonitor);
            
            if (status.getSeverity() == IStatus.OK) {
                // Run the URE Applicaton using IOOo.runUno()
                prj.getOOo().runUno(prj, mainName, args, pLaunch, pMonitor);
            } else {
                Display.getDefault().asyncExec(new Runnable() {

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
