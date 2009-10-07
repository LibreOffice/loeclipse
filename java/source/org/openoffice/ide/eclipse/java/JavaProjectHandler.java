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
package org.openoffice.ide.eclipse.java;

import java.io.File;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.language.IProjectHandler;
import org.openoffice.ide.eclipse.java.build.OOoContainerPage;
import org.openoffice.ide.eclipse.java.registration.RegistrationHelper;
import org.openoffice.ide.eclipse.java.tests.TestsHelper;

/**
 * The Project handler implementation for Java.
 * 
 * @author cedricbosdo
 *
 */
public class JavaProjectHandler implements IProjectHandler {

    private static final String P_REGISTRATION_CLASSNAME = "regclassname";  //$NON-NLS-1$
    private static final String P_JAVA_VERSION = "javaversion";  //$NON-NLS-1$
    
    private static final String[] KEPT_JARS = {
        "unoil.jar", //$NON-NLS-1$
        "ridl.jar", //$NON-NLS-1$
        "juh.jar", //$NON-NLS-1$
        "jurt.jar", //$NON-NLS-1$
        "unoloader.jar", //$NON-NLS-1$
        "officebean.jar" //$NON-NLS-1$
    };

    /**
     * {@inheritDoc}
     */
    public void addOOoDependencies(IOOo pOoo, IProject pProject) {

        IJavaProject javaProject = JavaCore.create(pProject);
        
        OOoContainerPage.addOOoDependencies(pOoo, javaProject);
    }

    /**
     * {@inheritDoc}
     */
    public void addProjectNature(IProject pProject) {
        try {
            if (!pProject.exists()) {
                pProject.create(null);
                PluginLogger.debug(
                        "Project created during language specific operation"); //$NON-NLS-1$
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
            newNatureIds[natureIds.length] = JavaCore.NATURE_ID;
            
            description.setNatureIds(newNatureIds);
            pProject.setDescription(description, null);
            PluginLogger.debug(Messages.getString("Language.JavaNatureSet")); //$NON-NLS-1$
            
        } catch (CoreException e) {
            PluginLogger.error(Messages.getString("Language.NatureSettingFailed")); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    public void configureProject(UnoFactoryData pData, IProgressMonitor pMonitor) throws Exception {

        // Get the project from data
        IProject prj = (IProject)pData.getProperty(
                IUnoFactoryConstants.PROJECT_HANDLE);
        IUnoidlProject unoprj = ProjectsManager.getProject(prj.getName());

        // Set some properties on the project

        // The registration class name is always computed in the same way
        String regclass = RegistrationHelper.getRegistrationClassName(unoprj);
        unoprj.setProperty(P_REGISTRATION_CLASSNAME, regclass);

        // Java version
        String javaversion = (String)pData.getProperty(
                JavaWizardPage.JAVA_VERSION);
        unoprj.setProperty(P_JAVA_VERSION, javaversion);
        
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
                unoprj.getName());
        
        // Create the project structure
        IJavaProject javaProject = JavaCore.create(project);
        javaProject.open( pMonitor );
        
        IPath sourcePath = unoprj.getFolder(unoprj.getSourcePath()).getFullPath();
        IPath buildPath = unoprj.getFolder(unoprj.getBuildPath()).getFullPath();
        
        IClasspathEntry[] entries = new IClasspathEntry[] { 
                JavaCore.newSourceEntry(sourcePath),
                JavaRuntime.getDefaultJREContainerEntry(),
                JavaCore.newLibraryEntry(buildPath, null, null, false)
        };
        
        javaProject.setRawClasspath(entries, pMonitor);
        
        // Add the registration files
        RegistrationHelper.generateFiles( unoprj );
        
        // Tests creation
        Boolean usetests = (Boolean)pData.getProperty( JavaWizardPage.JAVA_TESTS );
        if ( usetests.booleanValue() ) {
            TestsHelper.writeTestClasses( unoprj );
            
            IJavaProject javaprj = JavaCore.create( prj );
            TestsHelper.addJUnitLibraries( javaprj );
        }
    }

    /**
     * {@inheritDoc}
     */
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
    public IPath getImplementationFile(String pImplementationName) {
        
        return new Path(pImplementationName.replace(".", "/") + ".java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * {@inheritDoc}
     */
    public String getSkeletonMakerLanguage(UnoFactoryData pData) throws Exception {
        // Get the project from data
        String name = (String)pData.getProperty(
                IUnoFactoryConstants.PROJECT_NAME);
        IUnoidlProject unoprj = ProjectsManager.getProject(name);
        
        return "--" + unoprj.getProperty(P_JAVA_VERSION); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public void removeOOoDependencies(IOOo pOoo, IProject pProject) {
        IJavaProject javaProject = JavaCore.create(pProject);
        
        OOoContainerPage.removeOOoDependencies(javaProject);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getLibraryPath(IUnoidlProject pProject) {
        return getJarFile(pProject).getAbsolutePath();
    }
    
    /**
     * Returns a handle to the project jar file. Beware that this handle
     * may refer to a non-existing file. Users have to create it if necessary.
     * 
     * @param pProject the concerned UNO project
     * @return a handle to the jar file of the project
     */
    public File getJarFile(IUnoidlProject pProject) {
        String filename = pProject.getName().replace(" ", "") + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return pProject.getFile(filename).getLocation().toFile();
    }
    
    /**
     * Get the UNO registration class name of the project.
     * 
     * @param pProject the project for witch to get the registration class.
     * 
     * @return the registration class name
     */
    public String getRegistrationClassName(IUnoidlProject pProject) {
        return pProject.getProperty(P_REGISTRATION_CLASSNAME);
    }
    
    //--------------------------------------------- Jar finding private methods
    
    /**
     * returns the path of all the kept jars contained in the folder pointed by path.
     * 
     * @param pOoo the OOo instance from which to get the jars
     * @return a vector of Path pointing to each jar.
     */
    public static Vector<Path> findJarsFromPath(IOOo pOoo) {
        Vector<Path> jarsPath = new Vector<Path>();
        
        String[] paths = pOoo.getClassesPath();
        for (String path : paths) {
            Path folderPath = new Path(path);
            File programFolder = folderPath.toFile();
            
            String[] content = programFolder.list();
            for (int i = 0, length = content.length; i < length; i++) {
                String contenti = content[i];
                if (isKeptJar(contenti)) {
                    Path jariPath = new Path (
                            path + "/" + contenti); //$NON-NLS-1$
                    jarsPath.add(jariPath);
                }
            }   
        }
        
        return jarsPath;
    }
    
    /**
     * Check if the specified jar file is one of those define in the KEPT_JARS constant.
     * 
     * @param pJarName name of the jar file to check
     * @return <code>true</code> if jarName is one of those defined in KEPT_JARS, 
     *         <code>false</code> otherwise.
     */
    private static boolean isKeptJar(String pJarName) {
        
        int i = 0;
        boolean isKept = false;
        
        while (i < KEPT_JARS.length && !isKept) {
            if (pJarName.equals(KEPT_JARS[i])) {
                isKept = true;
            } else {
                i++;
            }
        }
        return isKept;
    }
}
