/*************************************************************************
 *
 * $RCSfile: UnoidlProjectHelper.java,v $
 *
 * $Revision: 1.12 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:51 $
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
package org.openoffice.ide.eclipse.core.internal.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.builders.TypesBuilder;
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.openoffice.ide.eclipse.core.model.CompositeFactory;
import org.openoffice.ide.eclipse.core.model.IUnoComposite;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.SDKContainer;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.ISdk;
import org.openoffice.ide.eclipse.core.model.language.AbstractLanguage;
import org.openoffice.ide.eclipse.core.model.pack.UnoPackage;

/**
 * Helper class for UNO-IDL project handling.
 * 
 * @author Cedric Bosdonnat
 *
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
    
    /**
     * Create a default configuration file for UNO-IDL projects.
     * 
     * @param pConfigFile the descriptor of the file to create
     */
    public static void createDefaultConfig(File pConfigFile) {
        Properties properties = new Properties();
        properties.setProperty(UnoidlProject.IDL_DIR, IDL_BASIS);
        properties.setProperty(UnoidlProject.BUILD_DIR, BUILD_BASIS);
        
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(pConfigFile);
            properties.store(out, Messages.getString("UnoidlProjectHelper.ConfigFileComment")); //$NON-NLS-1$
        } catch (Exception e) {
            PluginLogger.warning(Messages.getString("UnoidlProjectHelper.DefaultConfigFileError"), e); //$NON-NLS-1$
        } finally {
            try { out.close(); } catch (IOException e) { }
        }
    }
    
    /**
     * Create the basic structure of a UNO-IDL project.
     * 
     * @param pData the data describing the UNO project to create
     * @param pMonitor the progress monitor reporting the creation progress
     *                 in the User Interface
     * @return the created UNO project 
     * 
     * @throws Exception is thrown if anything wrong happens
     */
    public static IUnoidlProject createStructure(UnoFactoryData pData, 
            IProgressMonitor pMonitor) throws Exception {
        
        IUnoidlProject unoProject = null;
        
        // Creates the new project whithout it's builders
        IProject project = (IProject)pData.getProperty(
                IUnoFactoryConstants.PROJECT_HANDLE);
        
        createProject(project, pMonitor);
        unoProject = ProjectsManager.getProject(
                project.getName());
        
        // Set the company prefix
        String prefix = (String)pData.getProperty(
                IUnoFactoryConstants.PROJECT_PREFIX);
        unoProject.setCompanyPrefix(prefix);

        // Set the output extension
        String comp = (String)pData.getProperty(
                IUnoFactoryConstants.PROJECT_COMP);
        unoProject.setOutputExtension(comp);

        // Set the language
        AbstractLanguage language = (AbstractLanguage)pData.getProperty(
                IUnoFactoryConstants.PROJECT_LANGUAGE);
        unoProject.setLanguage(language);
        
        // Set the SDK
        String sdkname = (String)pData.getProperty(
                IUnoFactoryConstants.PROJECT_SDK);
        ISdk sdk = SDKContainer.getSDK(sdkname);
        unoProject.setSdk(sdk);

        // Set the OOo runtime
        String oooname = (String)pData.getProperty(
                IUnoFactoryConstants.PROJECT_OOO);
        IOOo ooo = OOoContainer.getOOo(oooname);
        unoProject.setOOo(ooo);


        // Set the idl directory
        String idlDir = (String)pData.getProperty(IUnoFactoryConstants.PROJECT_IDL_DIR);
        if (idlDir == null || idlDir.equals("")) { //$NON-NLS-1$
            idlDir = IDL_BASIS;
        }
        unoProject.setIdlDir(idlDir);
        
        // Set the sources directory
        String sourcesDir = (String)pData.getProperty(IUnoFactoryConstants.PROJECT_SRC_DIR);
        if (sourcesDir == null || sourcesDir.equals("")) { //$NON-NLS-1$
            sourcesDir = SOURCE_BASIS;
        }
        unoProject.setSourcesDir(sourcesDir);
        
        // create the language-specific part
        language.getProjectHandler().configureProject(pData, pMonitor);
        
        
        // Save all the properties to the configuration file
        unoProject.saveAllProperties();
        
        // Creation of the unoidl package
        createUnoidlPackage(unoProject, pMonitor);

        // Creation of the Code Packages
        createCodePackage(unoProject, pMonitor);

        // Creation of the urd output directory
        createUrdDir(unoProject, pMonitor);
        
        return unoProject;
    }
    
    /**
     * Deletes the given project resources (for ever).
     * 
     * @param pUnoProject the UNO project to delete
     * @param pMonitor the progress monitor reporting the task progression
     */
    public static void deleteProject(IUnoidlProject pUnoProject,
            IProgressMonitor pMonitor) {
        if (pUnoProject != null) {
            try {
                ((UnoidlProject)pUnoProject).getProject().delete(true, true, pMonitor);
            } catch (CoreException e) {
                // Nothing to do
            }
        }
    }
    
    /**
     * Forces a synchronous project build.
     * 
     * @param pUnoProject the project to build
     * @param pMonitor the monitor reporting the build progress
     */
    public static void forceBuild(IUnoidlProject pUnoProject, 
            IProgressMonitor pMonitor) {
        
        UnoidlProject project = (UnoidlProject)pUnoProject;
        try {
            TypesBuilder.build(project.getProject(), pMonitor);
        } catch (Exception e) {
            PluginLogger.error(
                    Messages.getString("UnoidlProjectHelper.NotUnoProjectError"), e); //$NON-NLS-1$
        }
    }
    
    /**
     * Set the project builders and run the build.
     * 
     * @param pUnoProject the project on which to set the builders
     */
    public static void setProjectBuilders(IUnoidlProject pUnoProject) {
        
        UnoidlProject project = (UnoidlProject)pUnoProject;
        try {
            // Add the project builders
            project.setBuilders();
        } catch (CoreException e) {
            PluginLogger.error(
                Messages.getString("UnoidlProjectHelper.NotUnoProjectError"), e); //$NON-NLS-1$
        }
    }
    
    /**
     * Refreshes the given UNO project.
     * 
     * @param pUnoproject the project to refresh
     * @param pMonitor the monitor reporting the refresh progress
     */
    public static void refreshProject(IUnoidlProject pUnoproject,
            IProgressMonitor pMonitor) {
        
        if (pUnoproject != null) {
            try {
                ((UnoidlProject)pUnoproject).getProject().refreshLocal(
                    IResource.DEPTH_INFINITE, pMonitor);
            } catch (CoreException e) {
            }
        }
    }
    
    /**
     * Creates the modules directories with the module fully qualified name.
     * 
     * @param pFullName module fully qualified name (eg: 
     *             <code>foo::bar</code>)
     * @param pUnoproject the UNO project on which to perform the action
     * @param pMonitor a progress monitor
     * 
     * @throws Exception if anything wrong happens
     */
    public static void createModules(String pFullName, IUnoidlProject pUnoproject,
                        IProgressMonitor pMonitor) throws Exception {
        
        if (pFullName != null && !pFullName.equals("")) { //$NON-NLS-1$
            // Create the directories
            IUnoComposite moduleDir = CompositeFactory.createModuleDir(
                    pFullName, pUnoproject);
            moduleDir.create(true);
            moduleDir.dispose();
            
            ((UnoidlProject)pUnoproject).getProject().refreshLocal(
                    IProject.DEPTH_INFINITE, pMonitor);
        } else {
            throw new IllegalArgumentException(
                    Messages.getString("UnoidlProjectHelper.BadFullnameError")); //$NON-NLS-1$
        }
    }
    
    /**
     * This method creates the folder described by the company prefix. 
     * 
     * <p>It assumes that the company prefix is already set, otherwise no
     * package will be created.</p>
     * 
     * @param pUnoproject the UNO project on which to perform the action
     * @param pMonitor progress monitor
     */
    public static void createUnoidlPackage(IUnoidlProject pUnoproject,
            IProgressMonitor pMonitor) {
        
        try {
            if (null != pUnoproject.getRootModule().replaceAll("::", ".")) { //$NON-NLS-1$ //$NON-NLS-2$

                PluginLogger.debug("Creating unoidl packages"); //$NON-NLS-1$
                
                // Create the IDL folder and all its parents
                IPath idlPath = pUnoproject.getIdlPath();
                String currentPath = "/"; //$NON-NLS-1$
                
                for (int i = 0, length = idlPath.segmentCount(); i < length; i++) {
                    currentPath += idlPath.segment(i) + "/"; //$NON-NLS-1$
                    IFolder folder = pUnoproject.getFolder(currentPath);
                    if (!folder.exists()) {
                        folder.create(true, true, pMonitor);
                    }
                }

                PluginLogger.debug(
                        "Unoidl base directory created"); //$NON-NLS-1$
                
                createModules(pUnoproject.getRootModule(), pUnoproject, pMonitor);
                PluginLogger.debug(
                        "All the modules dir have been created"); //$NON-NLS-1$
            }
        } catch (Exception e) {
            PluginLogger.error(
                    Messages.getString("UnoidlProjectHelper.FolderCreationError") + //$NON-NLS-1$
                        pUnoproject.getRootModulePath().toString(),
                    e);
        }
    }

    /**
     * Method that creates the directory where to produce the code.
     * 
     * @param pUnoproject the UNO project on which to perform the action
     * @param pMonitor monitor to report
     */
    public static void createCodePackage(IUnoidlProject pUnoproject,
            IProgressMonitor pMonitor) {

        String sourcesDir = pUnoproject.getSourcePath().toPortableString();
        if (sourcesDir == null || sourcesDir.equals("")) { //$NON-NLS-1$
            sourcesDir = SOURCE_BASIS;
        }
        
        try {
            PluginLogger.debug("Creating source directories"); //$NON-NLS-1$
            
            // Create the sources directory
            IFolder codeFolder = pUnoproject.getFolder(sourcesDir);
            if (!codeFolder.exists()) {
                codeFolder.create(true, true, pMonitor);
                PluginLogger.debug(
                    "source folder created"); //$NON-NLS-1$
            }

            pUnoproject.getLanguage().getProjectHandler().addOOoDependencies(
                    pUnoproject.getOOo(), ((UnoidlProject)pUnoproject).getProject());
            PluginLogger.debug("OOo dependencies added"); //$NON-NLS-1$
        } catch (CoreException e) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), 
                            Messages.getString("UnoidlProjectHelper.CreationErrorTitle"),  //$NON-NLS-1$
                            Messages.getString("UnoidlProjectHelper.CreationErrorMessage")); //$NON-NLS-1$
                }
            });
            PluginLogger.error(
                    Messages.getString("UnoidlProjectHelper.FolderCreationError") + sourcesDir, //$NON-NLS-1$
                    e);
        }
        
    }
    
    /**
     * Creates the <code>urd</code> directory.
     * 
     * @param pUnoproject the project where to create the <code>urd</code> directory.
     * @param pMonitor a progress monitor
     */
    public static void createUrdDir(IUnoidlProject pUnoproject,
            IProgressMonitor pMonitor) {
        
        try {
            PluginLogger.debug("Creating ouput directories"); //$NON-NLS-1$
            IFolder urdFolder = pUnoproject.getFolder(pUnoproject.getUrdPath());
            if (!urdFolder.exists()) {
                
                String[] basis_dirs = pUnoproject.getUrdPath().segments();
                String path = ""; //$NON-NLS-1$
                int i = 0;
                while (i < basis_dirs.length) {
                    
                    path = path + basis_dirs[i] + "/"; //$NON-NLS-1$
                    IFolder tmpFolder = pUnoproject.getFolder(path);
                    
                    if (!tmpFolder.exists()) {
                        tmpFolder.create(true, true, pMonitor);
                        tmpFolder.setDerived(true);
                        PluginLogger.debug(
                                tmpFolder.getName() + " folder created"); //$NON-NLS-1$
                    }
                    i++;
                }
            }
        } catch (CoreException e) {
            PluginLogger.error(
                Messages.getString("UnoidlProjectHelper.FolderCreationError") + URD_BASIS, e); //$NON-NLS-1$
        }
    }

    /**
     * Returns the UNO project underlying {@link IProject} resource.
     *
     * @param pUnoProject the UNO project for which to return the {@link IProject}
     *
     * @return the underlying {@link IProject} or <code>null</code>
     *             if the given project is <code>null</code> or any problem
     *             appear.
     */
    public static IProject getProject(IUnoidlProject pUnoProject) {
        
        IProject project = null;
        if (pUnoProject != null && pUnoProject instanceof UnoidlProject) {
            project = ((UnoidlProject)pUnoProject).getProject();
        }
        
        return project;
    }
    
    
    //---------------------------------------------------------- Private methods
    
    
    
    /**
     * This method creates and opens the project with the Java and UNO natures.
     * 
     * @param pProject project to create
     * @param pMonitor monitor used to report the creation state
     */
    private static void createProject(IProject pProject, 
            IProgressMonitor pMonitor) {
        try {
            if (!pProject.exists()) {
                pProject.create(pMonitor);
                PluginLogger.debug("Project resource created: " +  //$NON-NLS-1$
                        pProject.getName());
            }
            
            if (!pProject.isOpen()) {
                pProject.open(pMonitor);
                PluginLogger.debug("Project is opened: " +  //$NON-NLS-1$
                        pProject.getName());
            }
            
            IProjectDescription description = pProject.getDescription();
            String[] natureIds = description.getNatureIds();
            String[] newNatureIds = new String[natureIds.length + 1];
            System.arraycopy(natureIds, 0, newNatureIds, 0, natureIds.length);
            
            // Adding the Uno Nature
            newNatureIds[natureIds.length] = OOEclipsePlugin.UNO_NATURE_ID;
            
            description.setNatureIds(newNatureIds);
            pProject.setDescription(description, pMonitor);
            PluginLogger.debug("UNO-IDL nature set"); //$NON-NLS-1$
            
            UnoidlProject unoProject = (UnoidlProject)pProject.getNature(
                    OOEclipsePlugin.UNO_NATURE_ID);
            
            // TODO Allow custom configuration
            createDefaultConfig(unoProject.getConfigFile());
            
            ProjectsManager.addProject(unoProject);
            
        } catch (CoreException e) {
            PluginLogger.error(
                Messages.getString("UnoidlProjectHelper.NatureSetError"), e); //$NON-NLS-1$
        }
    }
    
    /**
     * Create the minimal {@link UnoPackage} for the project.
     * 
     * @param pPrj the project to package
     * @param pDest the package destination file
     * 
     * @return the minimal {@link UnoPackage}
     */
    public static UnoPackage createMinimalUnoPackage(IUnoidlProject pPrj, File pDest) {

        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( pPrj.getName() );
        UnoPackage unoPackage = new UnoPackage(pDest, prj);
        
        // Add content to the package
        unoPackage.addTypelibraryFile( pPrj.getFile( pPrj.getTypesPath() ), "RDB"); //$NON-NLS-1$
        pPrj.getLanguage().getLanguageBuidler().fillUnoPackage(unoPackage, pPrj);
        
        return unoPackage;
    }
}
