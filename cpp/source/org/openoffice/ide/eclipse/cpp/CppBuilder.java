/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Novell, Inc.
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
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2009 by Novell, Inc.
 *
 * All Rights Reserved.
 * 
 ************************************************************************/
package org.openoffice.ide.eclipse.cpp;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.core.LogLevels;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.UnoPackage;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.ISdk;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;

public class CppBuilder implements ILanguageBuilder {
    
    public static final String INCLUDE = "include"; //$NON-NLS-1$

    @Override
    public IPath createLibrary(IUnoidlProject unoProject) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void fillUnoPackage(UnoPackage unoPackage, IUnoidlProject prj) {
        // TODO Auto-generated method stub

    }

    @Override
    public String[] getBuildEnv(IUnoidlProject unoProject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void generateFromTypes(ISdk pSdk, IOOo pOoo, IProject pPrj,
            File pTypesFile, File pBuildFolder, String pRootModule,
            IProgressMonitor pMonitor) {
        
        if (pTypesFile.exists()) {

            try {

                if (null != pSdk && null != pOoo) {

                    String[] paths = pOoo.getTypesPath();
                    String oooTypesArgs = ""; //$NON-NLS-1$
                    for (String path : paths) {
                        IPath ooTypesPath = new Path (path);
                        oooTypesArgs += " \"" + ooTypesPath.toOSString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
                    }

                    String command = "cppumaker -T\"*\"" +  //$NON-NLS-1$
                    " -Gc -BUCR " +  //$NON-NLS-1$
                    "-O \"" + new File( pBuildFolder, INCLUDE ).getAbsolutePath() + "\" \"" + //$NON-NLS-1$ //$NON-NLS-2$
                    pTypesFile.getAbsolutePath() + "\" " + //$NON-NLS-1$
                    oooTypesArgs; 

                    IUnoidlProject unoprj = ProjectsManager.getProject(pPrj.getName());
                    Process process = pSdk.runTool(unoprj,command, pMonitor);

                    LineNumberReader lineReader = new LineNumberReader(
                            new InputStreamReader(process.getErrorStream()));

                    // Only for debugging purpose
                    if (PluginLogger.isLevel(LogLevels.DEBUG)) {

                        String line = lineReader.readLine();
                        while (null != line) {
                            System.out.println(line);
                            line = lineReader.readLine();
                        }
                    }
                    
                    process.waitFor();
                    
                    // Check if the build/include dir is in the includes
                    IPath includePath = pPrj.getFolder( 
                            unoprj.getBuildPath().append( CppBuilder.INCLUDE ) ).getProjectRelativePath();
                    
                    CppProjectHandler.addIncludesAndLibs( pPrj, 
                            new CIncludePathEntry[]{ new CIncludePathEntry( includePath, ICSettingEntry.VALUE_WORKSPACE_PATH ) }, 
                            new CLibraryPathEntry[0] );
                }
            } catch (InterruptedException e) {
                PluginLogger.error(
                        "cppumaker code generation failed", e);
            } catch (IOException e) {
                PluginLogger.warning(
                        "Unreadable output error");
            }
        }
    }
}
