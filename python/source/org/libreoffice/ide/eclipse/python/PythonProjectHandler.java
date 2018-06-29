/*************************************************************************
 *
 * $RCSfile: JavaProjectHandler.java,v $
 *
 * $Revision: 1.10 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:43:02 $
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
package org.libreoffice.ide.eclipse.python;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.builders.TypesBuilder;
import org.libreoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.UnoFactoryData;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.language.IProjectHandler;
import org.libreoffice.ide.eclipse.core.utils.WorkbenchHelper;
import org.libreoffice.ide.eclipse.python.utils.TemplatesHelper;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * The Project handler implementation for Python.
 */
public class PythonProjectHandler implements IProjectHandler {

    private static final String P_REGISTRATION_CLASSNAME = "regclassname"; //$NON-NLS-1$
    private static final String P_JAVA_VERSION = "javaversion"; //$NON-NLS-1$

    private static final String SOURCE_BASIS = "/source"; //$NON-NLS-1$

    private static final String PYTHON_NATURE = "org.python.pydev.pythonNature"; //$NON-NLS-1$
    private static final String PYTHON_BUILDER = "org.python.pydev.PyDevBuilder"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOOoDependencies(IOOo pOoo, IProject pProject) {

        PluginLogger.debug("For a Python project 'No' OOo dependencies are added"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProjectNature(IProject pProject) {
        try {
            if (!pProject.exists()) {
                pProject.create(null);
                PluginLogger.debug("Project created during language specific operation"); //$NON-NLS-1$
            }

            if (!pProject.isOpen()) {
                pProject.open(null);
                PluginLogger.debug("Project opened"); //$NON-NLS-1$
            }

            IProjectDescription description = pProject.getDescription();
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
            pProject.setDescription(description, null);
            PluginLogger.debug(Messages.getString("Language.PythonNatureBuilderSet")); //$NON-NLS-1$

        } catch (CoreException e) {
            PluginLogger.error(Messages.getString("Language.NatureSettingFailed")); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureProject(UnoFactoryData pData, IProgressMonitor pMonitor) throws Exception {

        // Get the project from data
        IProject prj = (IProject) pData.getProperty(IUnoFactoryConstants.PROJECT_HANDLE);
        IUnoidlProject unoprj = ProjectsManager.getProject(prj.getName());

        //Check if the source folder exists or not
        String sourcesDir = unoprj.getSourcePath().toPortableString();
        if (sourcesDir == null || sourcesDir.equals("")) { //$NON-NLS-1$
            sourcesDir = SOURCE_BASIS;
        }

        //Copy the Starting Python Source File under the source folder
        IFolder sourceFolder = prj.getFolder(sourcesDir);
        sourceFolder.create(true, true, pMonitor);
        IPath sourcePath = sourceFolder.getProjectRelativePath();
        Object[] args = { prj.getName() };
        TemplatesHelper.copyTemplate(prj, "StartingPythonClass.py", PythonProjectHandler.class, sourcePath.toString(), //$NON-NLS-1$
            false, args); //false denotes that the source .tpl filename and the destination filename are not same

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
    public String getImplementationName(IUnoidlProject pPrj, String pService) throws Exception {
        String prefix = pPrj.getCompanyPrefix();
        String comp = pPrj.getOutputExtension();

        String implementationName = null;

        if (pService.startsWith(prefix)) {
            String localName = pService.substring(prefix.length());
            implementationName = prefix + "." + comp + localName + "Impl"; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            throw new Exception("Cannot find implementation name for service: " + pService); //$NON-NLS-1$
        }

        return implementationName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPath getImplementationFile(String pImplementationName) {

        return new Path(pImplementationName.replace(".", "/") + ".java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSkeletonMakerLanguage(UnoFactoryData pData) throws Exception {
        // Get the project from data
        String name = (String) pData.getProperty(IUnoFactoryConstants.PROJECT_NAME);
        IUnoidlProject unoprj = ProjectsManager.getProject(name);

        return "--" + unoprj.getProperty(P_JAVA_VERSION); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeOOoDependencies(IOOo pOoo, IProject pProject) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLibraryPath(IUnoidlProject pProject) {
        return getJarFile(pProject).getLocation().toOSString();
    }

    /**
     * Returns a handle to the project jar file. Beware that this handle may refer to a non-existing file. Users have to
     * create it if necessary.
     *
     * @param pProject
     *            the concerned UNO project
     * @return a handle to the jar file of the project
     */
    public IFile getJarFile(IUnoidlProject pProject) {
        String filename = pProject.getName().replace(" ", "") + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return pProject.getFile(filename);
    }

    /**
     * Returns a handle to the project jar file. Beware that this handle may refer to a non-existing file. Users have to
     * create it if necessary.
     *
     * @param pProjectDir
     *            the concerned UNO project directory
     * @return a handle to the jar file of the project
     */
    public File getJarFile(File pProjectDir) throws IOException, XPathException {
        String filename = getName(pProjectDir).replace(" ", "") + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return new File(pProjectDir, filename);
    }

    private String getName(File projectDir) throws IOException, XPathException {
        File projectFile = new File(projectDir, ".project");
        if (!projectFile.exists()) {
            return null;
        }
        final FileInputStream byteStream = new FileInputStream(projectFile);
        InputSource source = new InputSource(byteStream);

        // evaluation de l'expression XPath
        XPathFactory factory = XPathFactory.newInstance();
        javax.xml.xpath.XPath xpath = factory.newXPath();
        final String xPathExpr = "//projectDescription/name";
        XPathExpression exp = xpath.compile(xPathExpr);
        Node node = (Node) exp.evaluate(source, XPathConstants.NODE);
        if (node == null) {
            return null;
        }
        return node.getTextContent();
    }

    /**
     * Get the UNO registration class name of the project.
     *
     * @param pProject
     *            the project for witch to get the registration class.
     *
     * @return the registration class name
     */
    public String getRegistrationClassName(IUnoidlProject pProject) {
        return pProject.getProperty(P_REGISTRATION_CLASSNAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFolder[] getBinFolders(IUnoidlProject pUnoidlProject) {
        ArrayList<IFolder> folders = new ArrayList<IFolder>();

        IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
        IProject prj = workspace.getProject(pUnoidlProject.getName());
        IJavaProject javaPrj = JavaCore.create(prj);
        try {
            folders.add(workspace.getFolder(javaPrj.getOutputLocation()));

            IClasspathEntry[] entries = javaPrj.getRawClasspath();
            for (IClasspathEntry entry : entries) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE && entry.getOutputLocation() != null) {
                    folders.add(workspace.getFolder(entry.getOutputLocation()));
                }
            }
        } catch (JavaModelException e) {
        }

        return folders.toArray(new IFolder[folders.size()]);
    }

    // --------------------------------------------- Jar finding private methods

    /**
     * returns the path of all the kept jars contained in the folder pointed by path.
     *
     * @param pOoo
     *            the OOo instance from which to get the jars
     * @return a vector of Path pointing to each jar.
     */
    public static Vector<Path> findJarsFromPath(IOOo pOoo) {
        Vector<Path> jarsPath = new Vector<Path>();

        //        String[] paths = pOoo.getClassesPath();
        //        for (String path : paths) {
        //            Path folderPath = new Path(path);
        //            File programFolder = folderPath.toFile();
        //
        //            String[] content = programFolder.list();
        //            for (int i = 0, length = content.length; i < length; i++) {
        //                String contenti = content[i];
        //                if (isKeptJar(contenti)) {
        //                    Path jariPath = new Path(path + "/" + contenti); //$NON-NLS-1$
        //                    jarsPath.add(jariPath);
        //                }
        //            }
        //        }

        return jarsPath;
    }

}
