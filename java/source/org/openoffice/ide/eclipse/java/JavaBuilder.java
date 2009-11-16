/*************************************************************************
 *
 * $RCSfile: JavaBuilder.java,v $
 *
 * $Revision: 1.7 $
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.openoffice.ide.eclipse.core.LogLevels;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.ISdk;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.openoffice.ide.eclipse.core.model.pack.UnoPackage;
import org.openoffice.ide.eclipse.java.build.FilesVisitor;
import org.openoffice.ide.eclipse.java.build.UnoManifestProvider;

/**
 * The language builder implementation for Java.
 * 
 * @author cedricbosdo
 *
 */
public class JavaBuilder implements ILanguageBuilder {

    private Language mLanguage;
    
    /**
     * Constructor.
     * 
     * @param pLanguage the Java Language object
     */
    public JavaBuilder(Language pLanguage) {
        mLanguage = pLanguage;
    }
    
    /**
     * {@inheritDoc}
     */
    public IPath createLibrary(IUnoidlProject pUnoProject) throws Exception {

        IFile jarFile = ((JavaProjectHandler)mLanguage.getProjectHandler()).getJarFile(pUnoProject);
        
        JarPackageData description = new JarPackageData();
        description.setGenerateManifest( true );
        description.setJarLocation( jarFile.getLocation() );
        
        String regClassname = ((JavaProjectHandler)mLanguage.getProjectHandler()).
        getRegistrationClassName(pUnoProject);
        description.setManifestProvider( new UnoManifestProvider( regClassname ) );
        description.setManifestLocation( pUnoProject.getFile( "MANIFEST.MF" ).getFullPath() ); //$NON-NLS-1$
        description.setSaveManifest( true );
        description.setReuseManifest( true );
        description.setExportOutputFolders( true );
        description.setExportClassFiles( true );
        description.setExportWarnings( true );
        description.setOverwrite( true );
        
        // Get the files to export: javamaker output + project classes
        FilesVisitor visitor = new FilesVisitor( );
        visitor.addException( pUnoProject.getFolder( pUnoProject.getUrdPath( ) ) );
        
        IFolder buildDir = pUnoProject.getFolder( pUnoProject.getBuildPath() );
        buildDir.accept( visitor );
        description.setElements( visitor.getFiles() );
        
        // Create the Jar file
        IJarExportRunnable runnable = description.createJarExportRunnable( null );
        runnable.run( new NullProgressMonitor() );
        
        return jarFile.getLocation();
    }


    /**
     * {@inheritDoc}
     */
    public void generateFromTypes(ISdk pSdk, IOOo pOoo, IProject pPrj, File pTypesFile,
            File pBuildFolder, String pRootModule, IProgressMonitor pMonitor) {
        
        if (pTypesFile.exists()) {
            
            try {
                
                if (null != pSdk && null != pOoo) {
                    
                    String[] paths = pOoo.getTypesPath();
                    String oooTypesArgs = ""; //$NON-NLS-1$
                    for (String path : paths) {
                        IPath ooTypesPath = new Path (path);
                        oooTypesArgs += " -X\"" + ooTypesPath.toOSString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    
                    
                    // TODO What if the user creates other root modules ?
                    String firstModule = pRootModule.split("::")[0]; //$NON-NLS-1$
                    
                    String command = "javamaker -T" + firstModule +  //$NON-NLS-1$
                        ".* -nD -Gc -BUCR " +  //$NON-NLS-1$
                        "-O \"" + pBuildFolder.getAbsolutePath() + "\" \"" + //$NON-NLS-1$ //$NON-NLS-2$
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
                }
            } catch (InterruptedException e) {
                PluginLogger.error(
                        Messages.getString("Language.CreateCodeError"), e); //$NON-NLS-1$
            } catch (IOException e) {
                PluginLogger.warning(
                        Messages.getString("Language.UnreadableOutputError")); //$NON-NLS-1$
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String[] getBuildEnv(IUnoidlProject pUnoProject) {
        
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(pUnoProject.getName());
        
        String[] env = new String[2];
        
        // compute the classpath for the project's OOo instance
        String classpath = "CLASSPATH="; //$NON-NLS-1$
        String sep = System.getProperty("path.separator"); //$NON-NLS-1$
        
        File javaHomeFile = null;
        
        // Compute the classpath for the project dependencies
        IJavaProject javaProject = JavaCore.create(project);
        if (javaProject != null) {
            try {
                IClasspathEntry[] cpEntry = javaProject.getResolvedClasspath(true);
                for (int i = 0; i < cpEntry.length; i++) {
                    IClasspathEntry entry = cpEntry[i];
                    
                    // Transform into the correct path for the entry.
                    if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
                        classpath += entry.getPath().toOSString();
                    }
                    if (i < cpEntry.length - 1) {
                        classpath += sep;
                    }
                }
                
                IVMInstall vmInstall = JavaRuntime.getVMInstall(javaProject);
                javaHomeFile = vmInstall.getInstallLocation();
                
            } catch (JavaModelException e) {
                PluginLogger.error(
                        Messages.getString("Language.GetClasspathError"), e); //$NON-NLS-1$
            } catch (CoreException e) {
                // TODO log a problem to find the JVM associated to the project
            }
        }
        
        env[0] = classpath;
        if (javaHomeFile != null) {
            String libs = ""; //$NON-NLS-1$
            String filesep = System.getProperty("file.separator"); //$NON-NLS-1$
            try {
                String arch = System.getProperty("os.arch"); //$NON-NLS-1$
                libs = javaHomeFile.getCanonicalPath() + filesep + "lib" + filesep + arch; //$NON-NLS-1$
            } catch (IOException e) {
            }
            env[1] = "LD_LIBRARY_PATH=" + libs; //$NON-NLS-1$
        }
        
        return env;
    }
    
    /**
     * {@inheritDoc}
     */
    public void fillUnoPackage(UnoPackage pUnoPackage, IUnoidlProject pUnoPrj) {
                
        // Add the component Jar file
        JavaProjectHandler handler = (JavaProjectHandler)mLanguage.getProjectHandler();
        pUnoPackage.addComponentFile(handler.getJarFile(pUnoPrj), "Java"); //$NON-NLS-1$
        
        // Add all the jar dependencies
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(pUnoPrj.getName());
        IJavaProject javaPrj = JavaCore.create(prj);
        ArrayList<IFile> libs = getLibs(javaPrj);
        for (IFile lib : libs) {
            pUnoPackage.addTypelibraryFile(lib, "Java"); //$NON-NLS-1$
        }
    }
    
    /**
     * Get the libraries in the classpath that are located in the project
     * directory or one of its subfolder. 
     * 
     * @param pJavaPrj the project from which to extract the libraries
     * @return a list of all the File pointing to the libraries.
     */
    private ArrayList<IFile> getLibs(IJavaProject pJavaPrj) {
        ArrayList<IFile> libs = new ArrayList<IFile>();
        try {
            IClasspathEntry[] entries = pJavaPrj.getResolvedClasspath(true);
            for (IClasspathEntry entry : entries) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                    /*
                     * At first, add only the libraries located in the project
                     * or one of its children. All others libraries have to be
                     * managed y the user.
                     */
                    IPath path = entry.getPath();
                    if (!new File(path.toOSString()).exists() && path.isAbsolute() &&
                            path.toString().startsWith("/" + pJavaPrj.getProject().getName())) { //$NON-NLS-1$
                        // Relative to the project
                        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile( path );
                        if ( file != null ) {
                            libs.add( file );
                        }
                    }
                }
            }
            
        } catch (JavaModelException e) {
            // Enable to add some missing library
        }
        
        return libs;
    }
}
