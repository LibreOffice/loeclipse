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
package org.libreoffice.ide.eclipse.python;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;
import org.libreoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.libreoffice.ide.eclipse.core.model.utils.SystemHelper;
import org.libreoffice.ide.eclipse.python.build.FilesVisitor;
import org.libreoffice.plugin.core.model.UnoPackage;

/**
 * The language builder implementation for Java.
 */
public class PythonBuilder implements ILanguageBuilder {

    private Language mLanguage;

    /**
     * Constructor.
     *
     * @param pLanguage the Java Language object
     */
    public PythonBuilder(Language pLanguage) {
        mLanguage = pLanguage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFile createLibrary(IUnoidlProject pUnoProject) throws Exception {
        IFile jarFile = ((PythonProjectHandler) mLanguage.getProjectHandler()).getJarFile(pUnoProject);

        //        // Add all the jar dependencies
        //        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(pUnoProject.getName());
        //        IJavaProject javaPrj = JavaCore.create(prj);
        //        List<IFile> externalJars = getLibs(javaPrj);
        //
        //        JarPackageData description = new JarPackageData();
        //        description.setGenerateManifest(true);
        //        description.setJarLocation(jarFile.getLocation());
        //
        //        String regClassname = ((PythonProjectHandler) mLanguage.getProjectHandler())
        //            .getRegistrationClassName(pUnoProject);
        //        description.setManifestProvider(new UnoManifestProvider(regClassname, pUnoProject, externalJars));
        //        description.setManifestLocation(pUnoProject.getFile("MANIFEST.MF").getFullPath()); //$NON-NLS-1$
        //        description.setSaveManifest(false);
        //        description.setReuseManifest(false);
        //        description.setExportOutputFolders(true);
        //        description.setExportClassFiles(true);
        //        description.setExportWarnings(true);
        //        description.setOverwrite(true);
        //
        //        // Get the files to export: javamaker output + project classes
        //        FilesVisitor visitor = new FilesVisitor();
        //        visitor.addException(pUnoProject.getFolder(pUnoProject.getUrdPath()));
        //
        //        IFolder buildDir = pUnoProject.getFolder(pUnoProject.getBuildPath());
        //        buildDir.accept(visitor);
        //        description.setElements(visitor.getFiles());
        //
        //        // Create the Jar file
        //        IJarExportRunnable runnable = description.createJarExportRunnable(null);
        //        runnable.run(new NullProgressMonitor());
        //
        return jarFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateFromTypes(ISdk pSdk, IOOo pOoo, IProject pPrj, File pTypesFile,
        File pBuildFolder, String pRootModule, IProgressMonitor pMonitor) {

        if (pTypesFile.exists()) {

            if (null != pSdk && null != pOoo) {

                String[] paths = pOoo.getTypesPath();
                String oooTypesArgs = ""; //$NON-NLS-1$
                for (String path : paths) {
                    IPath ooTypesPath = new Path(path);
                    oooTypesArgs += " -X\"" + ooTypesPath.toOSString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
                }

                // TODO What if the user creates other root modules ?
                String firstModule = pRootModule.split("::")[0]; //$NON-NLS-1$

                runJavamaker(firstModule, oooTypesArgs, pSdk, pPrj, pTypesFile, pBuildFolder, pMonitor);
            }
        }
    }

    private void runJavamaker(String firstModule, String oooTypesArgs,
        ISdk pSdk, IProject pPrj, File pTypesFile,
        File pBuildFolder, IProgressMonitor pMonitor) {

        StringBuffer errBuf = new StringBuffer();
        try {
            String cmdPattern = "javamaker -T {0}.* -nD -Gc -O {1} \"{2}\" {3}"; //$NON-NLS-1$
            String command = MessageFormat.format(cmdPattern, firstModule,
                pBuildFolder.getAbsolutePath(),
                pTypesFile.getAbsolutePath(),
                oooTypesArgs);

            IUnoidlProject unoprj = ProjectsManager.getProject(pPrj.getName());
            Process process = pSdk.runTool(unoprj, command, pMonitor);

            process.waitFor();

            LineNumberReader lineReader = new LineNumberReader(
                new InputStreamReader(process.getErrorStream()));

            String line = lineReader.readLine();
            while (null != line) {
                errBuf.append(line + '\n');
                line = lineReader.readLine();
            }

            PluginLogger.debug(errBuf.toString());
        } catch (InterruptedException e) {
            PluginLogger.error(
                Messages.getString("Language.CreateCodeError"), e); //$NON-NLS-1$
        } catch (IOException e) {
            PluginLogger.warning(
                Messages.getString("Language.UnreadableOutputError")); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public void fillUnoPackage(UnoPackage pUnoPackage, IUnoidlProject pUnoPrj) {
        //All the constituent Python files of the project are added
        File prjFile = SystemHelper.getFile(pUnoPrj);
        IFolder sourceFolder = pUnoPrj.getFolder(pUnoPrj.getSourcePath());
        ArrayList<IFile> pythonFiles = new ArrayList<IFile>();
        getPythonFiles(sourceFolder, pythonFiles, pUnoPrj);

        for (IFile pythonFile : pythonFiles) {
            File eachFile = SystemHelper.getFile(pythonFile);
            pUnoPackage.addComponentFile(
                UnoPackage.getPathRelativeToBase(eachFile, prjFile),
                eachFile, "Python"); //$NON-NLS-1$
        }

    }

    /**
     * Get the Python files that are located in the project
     * directory or one of its sub-folder.
     *
     * @param pPythonPrj the project from which to get the Python files
     * @return a list of all the those Python Files 
     */
    private void getPythonFiles(IFolder sourceFolder, ArrayList<IFile> pythonFiles, IUnoidlProject pUnoPrj) {
        try {
            for (IResource member : sourceFolder.members()) {
                if (member.getType() == 2) { // '1' is for file and '2' is for folder
                    IFolder subSourceFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(member.getFullPath());
                    getPythonFiles(subSourceFolder, pythonFiles, pUnoPrj);
                } else {
                    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(member.getFullPath());
                    pythonFiles.add(file);
                }

            }
        } catch (Exception e) {
            PluginLogger.error(
                Messages.getString("PythonExport.SourceFolderError"), e);

        }

    }
}
