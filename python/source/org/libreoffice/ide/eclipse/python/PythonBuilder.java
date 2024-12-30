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

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;
import org.libreoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.libreoffice.ide.eclipse.core.model.utils.SystemHelper;
import org.libreoffice.plugin.core.model.UnoPackage;

/**
 * The language builder implementation for Python.
 */
public class PythonBuilder implements ILanguageBuilder {
    /**
     * {@inheritDoc}
     */
    @Override
    public IFile createLibrary(IUnoidlProject pUnoProject) throws Exception {
        // Nothing to do for Python
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateFromTypes(ISdk pSdk, IOOo pOoo, IProject pPrj, File pTypesFile,
        File pBuildFolder, String pRootModule, IProgressMonitor pMonitor) {
        // Nothing to do for Python
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getBuildEnv(IUnoidlProject pUnoProject) {
        return new String[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fillUnoPackage(UnoPackage pUnoPackage, IUnoidlProject pUnoPrj) {
        File prjFile = SystemHelper.getFile(pUnoPrj);

        // Add the "main" python file as component
        String mainPythonFilePath = pUnoPrj.getSourcePath() + "/" + pUnoPrj.getName().replace(" ", "") + ".py";
        File mainPythonFile = SystemHelper.getFile(pUnoPrj.getFile(mainPythonFilePath));
        pUnoPackage.addComponentFile(
            UnoPackage.getPathRelativeToBase(mainPythonFile, prjFile),
            mainPythonFile, "Python");

        //All the constituent Python files of the project are added
        IFolder sourceFolder = pUnoPrj.getFolder(pUnoPrj.getSourcePath());
        ArrayList<IFile> pythonFiles = new ArrayList<IFile>();
        getPythonFiles(sourceFolder, pythonFiles, pUnoPrj);

        for (IFile pythonFile : pythonFiles) {
            File eachFile = SystemHelper.getFile(pythonFile);
            pUnoPackage.addOtherFile(UnoPackage.getPathRelativeToBase(eachFile, prjFile), eachFile);
        }
    }

    /**
     * Set the Python files (ie: pPythonFiles) that are located in the project
     * directory or one of its sub-folder.
     *
     * @param pSourceFolder the source folder
     * @param pPythonFiles the Python files
     * @param pUnoPrj the project from which to get the Python files
     */
    private void getPythonFiles(IFolder pSourceFolder, ArrayList<IFile> pPythonFiles, IUnoidlProject pUnoPrj) {
        try {
            for (IResource member : pSourceFolder.members()) {
                if (member.getType() == IResource.FOLDER) {
                    IFolder subSourceFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(member.getFullPath());
                    getPythonFiles(subSourceFolder, pPythonFiles, pUnoPrj);
                    continue;
                }
                IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(member.getFullPath());
                pPythonFiles.add(file);
            }
        } catch (Exception e) {
            PluginLogger.error(
                Messages.getString("PythonExport.SourceFolderError"), e);
        }
    }
}
