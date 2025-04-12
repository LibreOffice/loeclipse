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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.UnoFactoryData;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.language.IProjectHandler;
import org.libreoffice.ide.eclipse.core.utils.WorkbenchHelper;
import org.libreoffice.ide.eclipse.python.utils.TemplatesHelper;

/**
 * The Project handler implementation for Python.
 */
public class PythonProjectHandler implements IProjectHandler {

    private static final String SOURCE_BASIS = "/source"; //$NON-NLS-1$

    private static final String PYTHON_TEMPLATE = "StartingPythonClass.py"; //$NON-NLS-1$
    private static final String PYTHON_NATURE = "org.python.pydev.pythonNature"; //$NON-NLS-1$
    private static final String PYTHON_BUILDER = "org.python.pydev.PyDevBuilder"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOOoDependencies(IOOo ooo, IProject project) {
        // Nothing to do for Python
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProjectNature(IProject project) {
        try {
            if (!project.exists()) {
                project.create(null);
                PluginLogger.debug("Project created during language specific operation"); //$NON-NLS-1$
            }

            if (!project.isOpen()) {
                project.open(null);
                PluginLogger.debug("Project opened"); //$NON-NLS-1$
            }

            IProjectDescription description = project.getDescription();
            String[] natureIds = description.getNatureIds();
            String[] newNatureIds = new String[natureIds.length + 1];
            System.arraycopy(natureIds, 0, newNatureIds, 0, natureIds.length);

            // Adding the nature
            newNatureIds[natureIds.length] = PYTHON_NATURE;

            // Adding the buildCommand org.python.pydev.PyDevBuilder under buildSpec in .project file
            ICommand[] builders = description.getBuildSpec();

            ICommand typesbuilderCommand = description.newCommand();
            typesbuilderCommand.setBuilderName(PYTHON_BUILDER); //$NON-NLS-1$

            //During the eclipse startup it keeps on adding the same buildCommand again
            List<ICommand> list = Arrays.asList(builders);
            if (!list.contains(typesbuilderCommand)) {
                ICommand[] newCommands = new ICommand[builders.length + 1];
                newCommands[0] = typesbuilderCommand;
                System.arraycopy(builders, 0, newCommands, 1, builders.length);

                description.setBuildSpec(newCommands);
            }

            description.setNatureIds(newNatureIds);
            project.setDescription(description, null);
            PluginLogger.debug(Messages.getString("Language.PythonNatureBuilderSet")); //$NON-NLS-1$

        } catch (CoreException e) {
            PluginLogger.error(Messages.getString("Language.NatureSettingFailed")); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureProject(UnoFactoryData data, IProgressMonitor monitor) throws Exception {

        // Get the project from data
        IProject prj = (IProject) data.getProperty(IUnoFactoryConstants.PROJECT_HANDLE);
        IUnoidlProject unoprj = ProjectsManager.getProject(prj.getName());

        // Check if the source folder exists or not
        String sourcesDir = unoprj.getSourcePath().toPortableString();
        if (sourcesDir == null || sourcesDir.equals("")) { //$NON-NLS-1$
            sourcesDir = SOURCE_BASIS;
        }

        // Copy the Starting Python Source File under the source folder
        IFolder sourceFolder = prj.getFolder(sourcesDir);
        sourceFolder.create(true, true, monitor);
        IPath sourcePath = sourceFolder.getProjectRelativePath();
        Object[] args = { prj.getName() };
        // false denotes that the source .tpl filename and the destination filename are not same
        TemplatesHelper.copyTemplate(prj, PYTHON_TEMPLATE, PythonProjectHandler.class,
            sourcePath.toString(), false, args);

        // Refresh the project
        try {
            prj.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (Exception e) {
        }

        // Show the newly created Python source file
        IFolder srcDir = prj.getFolder(sourcePath);
        IFile pythonSourceFile = srcDir.getFile(prj.getName() + ".py"); //$NON-NLS-1$
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        WorkbenchHelper.showFile(pythonSourceFile,
            windows[0].getActivePage());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImplementationName(IUnoidlProject prj, String service) throws Exception {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPath getImplementationFile(String implementationName) {

        return new Path(implementationName.replace(".", "/") + ".py"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSkeletonMakerLanguage(UnoFactoryData data) throws Exception {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeOOoDependencies(IOOo ooo, IProject project) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLibraryPath(IUnoidlProject project) {
        return "";
    }

    @Override
    public IFolder[] getBinFolders(IUnoidlProject unoidlProject) {
        return new IFolder[0];
    }

}
