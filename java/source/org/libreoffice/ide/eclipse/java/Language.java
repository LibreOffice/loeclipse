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
package org.libreoffice.ide.eclipse.java;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.JavaRuntime;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.launch.office.IOfficeLaunchConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.language.AbstractLanguage;
import org.libreoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.libreoffice.ide.eclipse.core.model.language.IProjectHandler;

/**
 * Implementation for the Java language.
 */
public class Language extends AbstractLanguage {

    private static final String DEFAULT_JAVA_DEBUG_PORT = "7861";

    /**
     * {@inheritDoc}
     */
    @Override
    public ILanguageBuilder getLanguageBuidler() {
        return new JavaBuilder(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProjectHandler getProjectHandler() {
        return new JavaProjectHandler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectDebuggerToOffice(IUnoidlProject pPrj, ILaunch pLaunch, IPath pUserInstallation,
        IProgressMonitor pMonitor) {

        try {
            // org.eclipse.jdt.launching.socketListenConnector
            // SocketListenConnector
            String connectorId = "org.eclipse.jdt.launching.socketListenConnector";
            IVMConnector connector = JavaRuntime.getVMConnector(connectorId);
            Map<String, String> argMap = new HashMap<>();
            argMap.put("timeout", "80000");
            //FIXME implement some kind of port pickup/retry mechanism in case the default port is already used.
            argMap.put("port", DEFAULT_JAVA_DEBUG_PORT);

            connector.connect(argMap, pMonitor, pLaunch);

            pPrj.getOOo().runOffice(pPrj, pLaunch, pUserInstallation,
                new JavaDebugExtraOptionsProvider(DEFAULT_JAVA_DEBUG_PORT), pMonitor);
        } catch (Exception e) {
            PluginLogger.error("Could not start remote debugger.", e);
        }
    }

    @Override
    public void configureSourceLocator(ILaunchConfigurationWorkingCopy pConfiguration) throws CoreException {
        String projectName = pConfiguration.getAttribute(IOfficeLaunchConstants.PROJECT_NAME, "");
        pConfiguration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID,
            "org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector");
        pConfiguration.setAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID,
            "org.eclipse.jdt.launching.sourceLookup.javaSourcePathComputer");
        pConfiguration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
    };
}