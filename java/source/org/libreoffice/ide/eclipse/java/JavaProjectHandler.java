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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
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
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.UnoFactoryData;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.language.IProjectHandler;
import org.libreoffice.ide.eclipse.java.build.OOoContainerPage;
import org.libreoffice.ide.eclipse.java.registration.RegistrationHelper;
import org.libreoffice.ide.eclipse.java.tests.TestsHelper;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * The Project handler implementation for Java.
 */
public class JavaProjectHandler implements IProjectHandler {

    private static final String P_REGISTRATION_CLASSNAME = "regclassname"; //$NON-NLS-1$
    private static final String P_JAVA_VERSION = "javaversion"; //$NON-NLS-1$

    private static final String[] KEPT_JARS = {
        "libreoffice.jar", //$NON-NLS-1$
        "unoil.jar", //$NON-NLS-1$
        "ridl.jar", //$NON-NLS-1$
        "juh.jar", //$NON-NLS-1$
        "jurt.jar", //$NON-NLS-1$
        "unoloader.jar", //$NON-NLS-1$
        "officebean.jar", //$NON-NLS-1$
        "java_websocket.jar", //$NON-NLS-1$
        "unoagent.jar" //$NON-NLS-1$
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOOoDependencies(IOOo ooo, IProject project) {

        IJavaProject javaProject = JavaCore.create(project);

        OOoContainerPage.addOOoDependencies(ooo, javaProject);
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
            newNatureIds[natureIds.length] = JavaCore.NATURE_ID;

            description.setNatureIds(newNatureIds);
            project.setDescription(description, null);
            PluginLogger.debug(Messages.getString("Language.JavaNatureSet")); //$NON-NLS-1$

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

        // Set some properties on the project

        // The registration class name is always computed in the same way
        String regclass = RegistrationHelper.getRegistrationClassName(unoprj);
        unoprj.setProperty(P_REGISTRATION_CLASSNAME, regclass);

        // Java version
        String javaversion = (String) data.getProperty(JavaWizardPage.JAVA_VERSION);
        unoprj.setProperty(P_JAVA_VERSION, javaversion);

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(unoprj.getName());

        // Create the project structure
        IJavaProject javaProject = JavaCore.create(project);
        javaProject.open(monitor);

        IPath sourcePath = unoprj.getFolder(unoprj.getSourcePath()).getFullPath();
        IPath buildPath = unoprj.getFolder(unoprj.getBuildPath()).getFullPath();

        IClasspathEntry[] entries = new IClasspathEntry[] { JavaCore.newSourceEntry(sourcePath),
            JavaRuntime.getDefaultJREContainerEntry(),
            JavaCore.newLibraryEntry(buildPath, null, null, false) };

        javaProject.setRawClasspath(entries, monitor);

        // Add the registration files
        RegistrationHelper.generateFiles(unoprj);

        // Tests creation
        Boolean usetests = (Boolean) data.getProperty(JavaWizardPage.JAVA_TESTS);
        if (usetests.booleanValue()) {
            TestsHelper.writeTestClasses(unoprj);

            IJavaProject javaprj = JavaCore.create(prj);
            TestsHelper.addJUnitLibraries(javaprj);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImplementationName(IUnoidlProject prj, String service) throws Exception {
        String prefix = prj.getCompanyPrefix();
        String comp = prj.getOutputExtension();

        String implementationName = null;

        if (service.startsWith(prefix)) {
            String localName = service.substring(prefix.length());
            implementationName = prefix + "." + comp + localName + "Impl"; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            throw new Exception("Cannot find implementation name for service: " + service); //$NON-NLS-1$
        }

        return implementationName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPath getImplementationFile(String implementationName) {

        return new Path(implementationName.replace(".", "/") + ".java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSkeletonMakerLanguage(UnoFactoryData data) throws Exception {
        // Get the project from data
        String name = (String) data.getProperty(IUnoFactoryConstants.PROJECT_NAME);
        IUnoidlProject unoprj = ProjectsManager.getProject(name);

        return "--" + unoprj.getProperty(P_JAVA_VERSION); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeOOoDependencies(IOOo ooo, IProject project) {
        IJavaProject javaProject = JavaCore.create(project);

        OOoContainerPage.removeOOoDependencies(javaProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLibraryPath(IUnoidlProject project) {
        return getJarFile(project).getLocation().toOSString();
    }

    /**
     * Returns a handle to the project jar file. Beware that this handle may refer to a non-existing file. Users have to
     * create it if necessary.
     *
     * @param project
     *            the concerned UNO project
     * @return a handle to the jar file of the project
     */
    public IFile getJarFile(IUnoidlProject project) {
        String filename = project.getName().replace(" ", "") + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return project.getFile(filename);
    }

    /**
     * Returns a handle to the project jar file. Beware that this handle may refer to a non-existing file. Users have to
     * create it if necessary.
     *
     * @param projectDir
     *            the concerned UNO project directory
     * @return a handle to the jar file of the project
     */
    public File getJarFile(File projectDir) throws IOException, XPathException {
        String filename = getName(projectDir).replace(" ", "") + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return new File(projectDir, filename);
    }

    private String getName(File projectDir) throws IOException, XPathException {
        String name = null;
        File projectFile = new File(projectDir, ".project");
        if (projectFile.exists()) {
            final FileInputStream byteStream = new FileInputStream(projectFile);
            InputSource source = new InputSource(byteStream);

            // XPath expression evaluation
            XPathFactory factory = XPathFactory.newInstance();
            javax.xml.xpath.XPath xpath = factory.newXPath();
            final String xPathExpr = "//projectDescription/name";
            XPathExpression exp = xpath.compile(xPathExpr);
            Node node = (Node) exp.evaluate(source, XPathConstants.NODE);
            if (node != null) {
                name = node.getTextContent();
            }
        }
        return name;
    }

    /**
     * Get the UNO registration class name of the project.
     *
     * @param project
     *            the project for witch to get the registration class.
     *
     * @return the registration class name
     */
    public String getRegistrationClassName(IUnoidlProject project) {
        return project.getProperty(P_REGISTRATION_CLASSNAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFolder[] getBinFolders(IUnoidlProject unoProject) {
        ArrayList<IFolder> folders = new ArrayList<IFolder>();

        IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
        IProject prj = workspace.getProject(unoProject.getName());
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
     * @param ooo
     *            the OOo instance from which to get the jars
     * @return a vector of Path pointing to each jar.
     */
    public static Vector<Path> findJarsFromPath(IOOo ooo) {
        Vector<Path> jarsPath = new Vector<Path>();

        String[] paths = ooo.getClassesPath();
        for (String path : paths) {
            Path folderPath = new Path(path);
            File programFolder = folderPath.toFile();

            String[] content = programFolder.list();
            for (int i = 0, length = content.length; i < length; i++) {
                String contenti = content[i];
                if (isKeptJar(contenti)) {
                    Path jariPath = new Path(path + "/" + contenti); //$NON-NLS-1$
                    jarsPath.add(jariPath);
                }
            }
        }

        return jarsPath;
    }

    /**
     * Check if the specified jar file is one of those define in the KEPT_JARS constant.
     *
     * @param jarName
     *            name of the jar file to check
     * @return <code>true</code> if jarName is one of those defined in KEPT_JARS, <code>false</code> otherwise.
     */
    private static boolean isKeptJar(String jarName) {

        int i = 0;
        boolean isKept = false;

        while (i < KEPT_JARS.length && !isKept) {
            if (jarName.equals(KEPT_JARS[i])) {
                isKept = true;
            } else {
                i++;
            }
        }
        return isKept;
    }
}
