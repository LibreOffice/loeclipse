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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Vector;
import java.util.jar.JarOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.openoffice.ide.eclipse.core.LogLevels;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.UnoPackage;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;
import org.openoffice.ide.eclipse.core.utils.ZipContent;
import org.openoffice.ide.eclipse.java.utils.ZipContentHelper;

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

        // Create the manifest file
        String classpath = ""; //$NON-NLS-1$
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(pUnoProject.getName());
        Vector<File> libs = getLibs(JavaCore.create(prj));
        String prjPath = prj.getLocation().toOSString();
        for (File lib : libs) {
            String relPath = lib.getPath().substring(prjPath.length() + 1);
            classpath += relPath + " "; //$NON-NLS-1$
        }
        if (!classpath.equals("")) { //$NON-NLS-1$
            classpath = "Class-Path: " + classpath + "\r\n"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        String classname = ((JavaProjectHandler)mLanguage.getProjectHandler()).
            getRegistrationClassName(pUnoProject);
        String content = "ManifestVersion: 1.0\r\n" +  //$NON-NLS-1$
                         "RegistrationClassName: " + classname + "\r\n" + //$NON-NLS-1$ //$NON-NLS-2$
                         classpath;
        
        
        File manifestFile = new File(pUnoProject.getFile("MANIFEST.MF"). //$NON-NLS-1$
                getLocation().toOSString());
        StringReader reader = new StringReader(content);
        FileWriter writer = new FileWriter(manifestFile);
        try {
            int c = reader.read();

            while (c != -1) {
                writer.write(c);
                c = reader.read();
            }
        } finally {
            try {
                writer.close();
                reader.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
        
        
        // Create projectname.jar
        File jarFile = ((JavaProjectHandler)mLanguage.getProjectHandler()).
                getJarFile(pUnoProject);
        FileOutputStream out = new FileOutputStream(jarFile);
        JarOutputStream jarOut = new JarOutputStream(out);
        
        // Add the manifest
        ZipContent manifest = new ZipContent("META-INF/MANIFEST.MF", manifestFile); //$NON-NLS-1$
        manifest.writeContentToZip(jarOut);
        
        // Get all the files to write
        File bin = new File(pUnoProject.getFolder("bin"). //$NON-NLS-1$
                getLocation().toOSString());
        // JDT dependent
        ZipContent[] binContent = ZipContentHelper.getFiles(bin);
        
        File build = new File(pUnoProject.getFolder(pUnoProject.getBuildPath()).
                getLocation().toOSString());
        ZipContent[] javamakerContent = ZipContentHelper.getFiles(build);
        
        // write the content of the bin directory to the Jar
        for (int i = 0; i < binContent.length; i++) {
            binContent[i].writeContentToZip(jarOut);
        }
        
        for (int i = 0; i < javamakerContent.length; i++) {
            javamakerContent[i].writeContentToZip(jarOut);
        }
        
        // Close all the streams
        jarOut.close();
        out.close();
        
        return new Path(jarFile.getAbsolutePath());
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
        Vector<File> libs = getLibs(javaPrj);
        for (File lib : libs) {
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
    private Vector<File> getLibs(IJavaProject pJavaPrj) {
        Vector<File> libs = new Vector<File>();
        IPath prjPath = pJavaPrj.getProject().getLocation();
        
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
                        File libFile = prjPath.append(path.removeFirstSegments(1)).toFile();
                        if (libFile.isFile()) {
                            libs.add(libFile);
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
