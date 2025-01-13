/*************************************************************************
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
package org.libreoffice.ide.eclipse.python;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.widgets.Display;

import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.launch.office.IOfficeLaunchConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.config.NullExtraOptionsProvider;
import org.libreoffice.ide.eclipse.core.model.language.AbstractLanguage;
import org.libreoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.libreoffice.ide.eclipse.core.model.language.IProjectHandler;

import com.python.pydev.debug.remote.client_api.PydevRemoteDebuggerServer;

/**
 * Implementation for the Python language.
 */
public class Language extends AbstractLanguage {

    //    private static final String DEFAULT_PYTHON_DEBUG_PORT = "5677";

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageBuilder getLanguageBuilder() {
        return new PythonBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProjectHandler getProjectHandler() {
        return new PythonProjectHandler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectDebuggerToOffice(IUnoidlProject prj, ILaunch launch, IPath userInstallation,
        IProgressMonitor monitor) {

        try {
            if (PydevRemoteDebuggerServer.isRunning()) {
                prj.getOOo().runOffice(prj, launch, userInstallation, new NullExtraOptionsProvider(), monitor);
            } else {
                /* This allows to start the server asynchronously which will give 'Invalid thread access' Exception
                   if started from this thread which has been called by DebugUITools.launch(...)*/
                Display.getDefault().asyncExec(new debugSeverStart(prj, launch, userInstallation, monitor));
            }

        } catch (Exception e) {
            PluginLogger.error("Could not go for Debugging Mode.", e);
        }
    }

    @Override
    public void configureSourceLocator(ILaunchConfigurationWorkingCopy configuration) throws CoreException {
        String projectName = configuration.getAttribute(IOfficeLaunchConstants.PROJECT_NAME, "");
        configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID,
            "org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector");
        configuration.setAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID,
            "org.eclipse.jdt.launching.sourceLookup.javaSourcePathComputer");
        configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
    };

    /**
     * Thread executing the starting of the server followed by launching the LibreOffice instance for debugging.
     */
    private class debugSeverStart implements Runnable {

        private IUnoidlProject mPrj;
        private ILaunch mLaunch;
        private IPath mUserInstallation;
        private IProgressMonitor mMonitor;

        public debugSeverStart(IUnoidlProject prj, ILaunch launch, IPath userInstallation,
            IProgressMonitor monitor) {
            mPrj = prj;
            mLaunch = launch;
            mUserInstallation = userInstallation;
            mMonitor = monitor;
        }

        @Override
        public void run() {
            try {
                PydevRemoteDebuggerServer.startServer();
                mPrj.getOOo().runOffice(mPrj, mLaunch, mUserInstallation, new NullExtraOptionsProvider(), mMonitor);
            } catch (Exception e) {
                PluginLogger.error("Could not start the debug server and start the LibreOffice Instance.", e);
            }
        }
    }
}