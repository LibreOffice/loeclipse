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
package org.libreoffice.ide.eclipse.core.internal.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.builders.TypesBuilder;
import org.libreoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.libreoffice.ide.eclipse.core.model.CompositeFactory;
import org.libreoffice.ide.eclipse.core.model.IUnoComposite;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.OOoContainer;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.SDKContainer;
import org.libreoffice.ide.eclipse.core.model.UnoFactoryData;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;
import org.libreoffice.ide.eclipse.core.model.language.AbstractLanguage;
import org.libreoffice.ide.eclipse.core.model.utils.SystemHelper;
import org.libreoffice.plugin.core.model.UnoPackage;

/**
 * Helper class for UNO-IDL project handling.
 */
public class UnoidlProjectHelper {

    /**
     * Project relative path to the build directory.
     */
    public static final String BUILD_BASIS = "build"; //$NON-NLS-1$

    /**
     * Project relative path to the source directory.
     */
    public static final String SOURCE_BASIS = "/source"; //$NON-NLS-1$

    /**
     * Project relative path to the urd output folder.
     */
    public static final String URD_BASIS = "/urd"; //$NON-NLS-1$

    /**
     * Project relative path to the idl root folder.
     */
    public static final String IDL_BASIS = "/idl"; //$NON-NLS-1$

    public static final String DIST_BASIS = "dist"; //$NON-NLS-1$

    public static final String OO_PROFILE_BASIS = ".ooo-debug";

    /**
     * Create a default configuration file for UNO-IDL projects.
     *
     * @param configFile
     *            the descriptor of the file to create
     */
    public static void createDefaultConfig(File configFile) {
        Properties properties = new Properties();
        properties.setProperty(UnoidlProject.IDL_DIR, IDL_BASIS);
        properties.setProperty(UnoidlProject.BUILD_DIR, BUILD_BASIS);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(configFile);
            properties.store(out, Messages.getString("UnoidlProjectHelper.ConfigFileComment")); //$NON-NLS-1$
        } catch (Exception e) {
            PluginLogger.warning(Messages.getString("UnoidlProjectHelper.DefaultConfigFileError"), e); //$NON-NLS-1$
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Create the basic structure of a UNO-IDL project.
     *
     * @param data
     *            the data describing the UNO project to create
     * @param monitor
     *            the progress monitor reporting the creation progress in the User Interface
     * @return the created UNO project
     *
     * @throws Exception
     *             is thrown if anything wrong happens
     */
    public static IUnoidlProject createStructure(UnoFactoryData data, IProgressMonitor monitor) throws Exception {

        IUnoidlProject unoProject = null;

        // Creates the new project whithout it's builders
        IProject project = (IProject) data.getProperty(IUnoFactoryConstants.PROJECT_HANDLE);

        createProject(project, monitor);
        unoProject = ProjectsManager.getProject(project.getName());

        // Set the company prefix
        String prefix = (String) data.getProperty(IUnoFactoryConstants.PROJECT_PREFIX);
        unoProject.setCompanyPrefix(prefix);

        // Set the output extension
        String comp = (String) data.getProperty(IUnoFactoryConstants.PROJECT_COMP);
        unoProject.setOutputExtension(comp);

        // Set the language
        AbstractLanguage language = (AbstractLanguage) data.getProperty(IUnoFactoryConstants.PROJECT_LANGUAGE);
        unoProject.setLanguage(language);

        // Set the SDK
        String sdkname = (String) data.getProperty(IUnoFactoryConstants.PROJECT_SDK);
        ISdk sdk = SDKContainer.getSDK(sdkname);
        unoProject.setSdk(sdk);

        // Set the OOo runtime
        String oooname = (String) data.getProperty(IUnoFactoryConstants.PROJECT_OOO);
        IOOo ooo = OOoContainer.getOOo(oooname);
        unoProject.setOOo(ooo);

        // Set the idl directory
        String idlDir = (String) data.getProperty(IUnoFactoryConstants.PROJECT_IDL_DIR);
        if (idlDir == null || idlDir.equals("")) { //$NON-NLS-1$
            idlDir = IDL_BASIS;
        }
        unoProject.setIdlDir(idlDir);

        // Set the sources directory
        String sourcesDir = (String) data.getProperty(IUnoFactoryConstants.PROJECT_SRC_DIR);
        if (sourcesDir == null || sourcesDir.equals("")) { //$NON-NLS-1$
            sourcesDir = SOURCE_BASIS;
        }
        unoProject.setSourcesDir(sourcesDir);

        // create the language-specific part
        language.getProjectHandler().configureProject(data, monitor);

        // Save all the properties to the configuration file
        unoProject.saveAllProperties();

        if (!language.getName().contains("Python")) {
            // Creation of the unoidl package
            createUnoidlPackage(unoProject, monitor);

            // Creation of the Code Packages
            createCodePackage(unoProject, monitor);

            // Creation of the urd output directory
            createUrdDir(unoProject, monitor);
        }

        return unoProject;
    }

    /**
     * Deletes the given project resources (for ever).
     *
     * @param unoProject
     *            the UNO project to delete
     * @param monitor
     *            the progress monitor reporting the task progression
     */
    public static void deleteProject(IUnoidlProject unoProject, IProgressMonitor monitor) {
        if (unoProject != null) {
            try {
                ((UnoidlProject) unoProject).getProject().delete(true, true, monitor);
            } catch (CoreException e) {
                // Nothing to do
            }
        }
    }

    /**
     * Forces a synchronous project build.
     *
     * @param unoProject
     *            the project to build
     * @param monitor
     *            the monitor reporting the build progress
     */
    public static void forceBuild(IUnoidlProject unoProject, IProgressMonitor monitor) {

        UnoidlProject project = (UnoidlProject) unoProject;
        try {
            TypesBuilder.build(project.getProject(), monitor);
        } catch (Exception e) {
            PluginLogger.error(Messages.getString("UnoidlProjectHelper.NotUnoProjectError"), e); //$NON-NLS-1$
        }
    }

    /**
     * Set the project builders and run the build.
     *
     * @param unoProject
     *            the project on which to set the builders
     */
    public static void setProjectBuilders(IUnoidlProject unoProject) {

        UnoidlProject project = (UnoidlProject) unoProject;
        try {
            // Add the project builders
            project.setBuilders();
        } catch (CoreException e) {
            PluginLogger.error(Messages.getString("UnoidlProjectHelper.NotUnoProjectError"), e); //$NON-NLS-1$
        }
    }

    /**
     * Refreshes the given UNO project.
     *
     * @param unoProject
     *            the project to refresh
     * @param monitor
     *            the monitor reporting the refresh progress
     */
    public static void refreshProject(IUnoidlProject unoProject, IProgressMonitor monitor) {

        if (unoProject != null) {
            try {
                ((UnoidlProject) unoProject).getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
            } catch (CoreException e) {
            }
        }
    }

    /**
     * Creates the modules directories with the module fully qualified name.
     *
     * @param fullName
     *            module fully qualified name (eg: <code>foo::bar</code>)
     * @param unoProject
     *            the UNO project on which to perform the action
     * @param monitor
     *            a progress monitor
     *
     * @throws Exception
     *             if anything wrong happens
     */
    public static void createModules(String fullName, IUnoidlProject unoProject, IProgressMonitor monitor)
        throws Exception {

        if (fullName != null && !fullName.equals("")) { //$NON-NLS-1$
            // Create the directories
            IUnoComposite moduleDir = CompositeFactory.createModuleDir(fullName, unoProject);
            moduleDir.create(true);
            moduleDir.dispose();

            ((UnoidlProject) unoProject).getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
        } else {
            String msg = Messages.getString("UnoidlProjectHelper.BadFullnameError"); //$NON-NLS-1$
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * This method creates the folder described by the company prefix.
     *
     * <p>
     * It assumes that the company prefix is already set, otherwise no package will be created.
     * </p>
     *
     * @param unoProject
     *            the UNO project on which to perform the action
     * @param monitor
     *            progress monitor
     */
    public static void createUnoidlPackage(IUnoidlProject unoProject, IProgressMonitor monitor) {

        try {
            if (null != unoProject.getRootModule().replaceAll("::", ".")) { //$NON-NLS-1$ //$NON-NLS-2$

                PluginLogger.debug("Creating unoidl packages"); //$NON-NLS-1$

                // Create the IDL folder and all its parents
                IPath idlPath = unoProject.getIdlPath();
                String currentPath = "/"; //$NON-NLS-1$

                for (int i = 0, length = idlPath.segmentCount(); i < length; i++) {
                    currentPath += idlPath.segment(i) + "/"; //$NON-NLS-1$
                    IFolder folder = unoProject.getFolder(currentPath);
                    if (!folder.exists()) {
                        folder.create(true, true, monitor);
                    }
                }

                PluginLogger.debug("Unoidl base directory created"); //$NON-NLS-1$

                createModules(unoProject.getRootModule(), unoProject, monitor);
                PluginLogger.debug("All the modules dir have been created"); //$NON-NLS-1$
            }
        } catch (Exception e) {
            PluginLogger.error(Messages.getString("UnoidlProjectHelper.FolderCreationError") + //$NON-NLS-1$
                unoProject.getRootModulePath().toString(), e);
        }
    }

    /**
     * Method that creates the directory where to produce the code.
     *
     * @param unoProject
     *            the UNO project on which to perform the action
     * @param monitor
     *            monitor to report
     */
    public static void createCodePackage(IUnoidlProject unoProject, IProgressMonitor monitor) {

        String sourcesDir = unoProject.getSourcePath().toPortableString();
        if (sourcesDir == null || sourcesDir.equals("")) { //$NON-NLS-1$
            sourcesDir = SOURCE_BASIS;
        }

        try {
            PluginLogger.debug("Creating source directories"); //$NON-NLS-1$

            // Create the sources directory
            IFolder codeFolder = unoProject.getFolder(sourcesDir);
            if (!codeFolder.exists()) {
                codeFolder.create(true, true, monitor);
                PluginLogger.debug("source folder created"); //$NON-NLS-1$
            }

            unoProject.getLanguage().getProjectHandler().addOOoDependencies(unoProject.getOOo(),
                ((UnoidlProject) unoProject).getProject());
            PluginLogger.debug("OOo dependencies added"); //$NON-NLS-1$
        } catch (CoreException e) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(),
                        Messages.getString("UnoidlProjectHelper.CreationErrorTitle"), //$NON-NLS-1$
                        Messages.getString("UnoidlProjectHelper.CreationErrorMessage")); //$NON-NLS-1$
                }
            });
            String msg = Messages.getString("UnoidlProjectHelper.FolderCreationError"); //$NON-NLS-1$
            PluginLogger.error(msg + sourcesDir, e);
        }

    }

    /**
     * Creates the <code>urd</code> directory.
     *
     * @param unoProject
     *            the project where to create the <code>urd</code> directory.
     * @param monitor
     *            a progress monitor
     */
    public static void createUrdDir(IUnoidlProject unoProject, IProgressMonitor monitor) {

        try {
            PluginLogger.debug("Creating ouput directories"); //$NON-NLS-1$
            IFolder urdFolder = unoProject.getFolder(unoProject.getUrdPath());
            if (!urdFolder.exists()) {

                String[] basis_dirs = unoProject.getUrdPath().segments();
                String path = ""; //$NON-NLS-1$
                int i = 0;
                while (i < basis_dirs.length) {

                    path = path + basis_dirs[i] + "/"; //$NON-NLS-1$
                    IFolder tmpFolder = unoProject.getFolder(path);

                    if (!tmpFolder.exists()) {
                        tmpFolder.create(true, true, monitor);
                        tmpFolder.setDerived(true, monitor);
                        PluginLogger.debug(tmpFolder.getName() + " folder created"); //$NON-NLS-1$
                    }
                    i++;
                }
            }
        } catch (CoreException e) {
            String msg = Messages.getString("UnoidlProjectHelper.FolderCreationError"); //$NON-NLS-1$
            PluginLogger.error(msg + URD_BASIS, e);
        }
    }

    /**
     * Returns the UNO project underlying {@link IProject} resource.
     *
     * @param unoProject
     *            the UNO project for which to return the {@link IProject}
     *
     * @return the underlying {@link IProject} or <code>null</code> if the given project is <code>null</code> or any
     *         problem appear.
     */
    public static IProject getProject(IUnoidlProject unoProject) {

        IProject project = null;
        if (unoProject != null && unoProject instanceof UnoidlProject) {
            project = ((UnoidlProject) unoProject).getProject();
        }

        return project;
    }

    // ---------------------------------------------------------- Private methods

    /**
     * This method creates and opens the project with the Java and UNO natures.
     *
     * @param project
     *            project to create
     * @param monitor
     *            monitor used to report the creation state
     */
    private static void createProject(IProject project, IProgressMonitor monitor) {
        try {
            if (!project.exists()) {
                project.create(monitor);
                PluginLogger.debug("Project resource created: " + //$NON-NLS-1$
                    project.getName());
            }

            if (!project.isOpen()) {
                project.open(monitor);
                PluginLogger.debug("Project is opened: " + //$NON-NLS-1$
                    project.getName());
            }

            IProjectDescription description = project.getDescription();
            String[] natureIds = description.getNatureIds();
            String[] newNatureIds = new String[natureIds.length + 1];
            System.arraycopy(natureIds, 0, newNatureIds, 0, natureIds.length);

            // Adding the Uno Nature
            newNatureIds[natureIds.length] = OOEclipsePlugin.UNO_NATURE_ID;

            description.setNatureIds(newNatureIds);
            project.setDescription(description, monitor);
            PluginLogger.debug("UNO-IDL nature set"); //$NON-NLS-1$

            UnoidlProject unoProject = (UnoidlProject) project.getNature(OOEclipsePlugin.UNO_NATURE_ID);

            // XXX Allow custom configuration
            createDefaultConfig(unoProject.getConfigFile());

            ProjectsManager.addProject(unoProject);

        } catch (CoreException e) {
            PluginLogger.error(Messages.getString("UnoidlProjectHelper.NatureSetError"), e); //$NON-NLS-1$
        }
    }

    /**
     * Create the minimal {@link UnoPackage} for the project.
     *
     * @param prj
     *            the project to package
     * @param dest
     *            the package destination file
     *
     * @return the minimal {@link UnoPackage}
     */
    public static UnoPackage createMinimalUnoPackage(IUnoidlProject prj, File dest) {

        UnoPackage unoPackage = new UnoPackage(dest);

        File libFile = SystemHelper.getFile(prj.getFile(prj.getTypesPath()));
        File prjFile = SystemHelper.getFile(prj);

        // Add content to the package
        if (libFile.exists()) {
            unoPackage.addTypelibraryFile(UnoPackage.getPathRelativeToBase(libFile, prjFile), libFile); //$NON-NLS-1$
        }
        prj.getLanguage().getLanguageBuilder().fillUnoPackage(unoPackage, prj);

        return unoPackage;
    }

    /**
     * Checks if the resource is contained in the UNO package.
     *
     * @param res
     *            the resource to check
     * @return <code>true</code> if the resource is contained in the package
     */
    public static boolean isContainedInPackage(IResource res) {
        boolean contained = false;

        String prjName = res.getProject().getName();
        IUnoidlProject prj = ProjectsManager.getProject(prjName);

        try {
            URI resUri = res.getLocationURI();

            if (prj != null) {

                File outputDir = new File(System.getProperty("user.home")); //$NON-NLS-1$

                File dest = new File(outputDir, prj.getName() + ".zip"); //$NON-NLS-1$
                UnoPackage unoPackage = createMinimalUnoPackage(prj, dest);

                List<File> files = unoPackage.getContainedFiles();
                int i = 0;
                while (i < files.size() && !contained) {
                    URI uri = files.get(i).toURI();
                    if (uri.equals(resUri)) {
                        contained = true;
                    }
                    i++;
                }
                unoPackage.dispose();
            }
        } catch (Exception e) {
            contained = false;
        }

        return contained;
    }

    /**
     * Get the list of the files contained in the minimal UNO package.
     *
     * @param prj
     *            the project for which to get the minimal resources
     * @return the list of files
     */
    public static List<IResource> getContainedFile(IProject prj) {
        ArrayList<IResource> resources = new ArrayList<IResource>();

        String prjName = prj.getName();
        IUnoidlProject unoPrj = ProjectsManager.getProject(prjName);

        if (unoPrj != null) {

            File outputDir = new File(System.getProperty("user.home")); //$NON-NLS-1$

            File dest = new File(outputDir, prj.getName() + ".zip"); //$NON-NLS-1$
            UnoPackage unoPackage = UnoidlProjectHelper.createMinimalUnoPackage(unoPrj, dest);

            List<File> files = unoPackage.getContainedFiles();
            File prjFile = SystemHelper.getFile(prj);
            for (File file : files) {
                String relative = UnoPackage.getPathRelativeToBase(file, prjFile);
                IResource res = prj.findMember(relative);
                if (res != null) {
                    resources.add(res);
                }
            }

            unoPackage.dispose();
        }

        return resources;
    }

}
