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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;
import org.libreoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.libreoffice.ide.eclipse.core.model.utils.SystemHelper;
import org.libreoffice.ide.eclipse.java.build.FilesVisitor;
import org.libreoffice.ide.eclipse.java.build.Messages;
import org.libreoffice.ide.eclipse.java.build.UnoManifestProvider;
import org.libreoffice.plugin.core.model.UnoPackage;

/**
 * The language builder implementation for Java.
 */
public class JavaBuilder implements ILanguageBuilder {

    private static final String LIBS_DIR_NAME = "libs"; // subdirectory in Project, holds external jars
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
    @Override
    public IFile createLibrary(IUnoidlProject pUnoProject) throws Exception {
        IFile jarFile = ((JavaProjectHandler) mLanguage.getProjectHandler()).getJarFile(pUnoProject);

        // Add all the jar dependencies
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(pUnoProject.getName());
        IJavaProject javaPrj = JavaCore.create(prj);
        List<IFile> externalJars = getLibs(javaPrj);

        JarPackageData description = new JarPackageData();
        description.setGenerateManifest(true);
        description.setJarLocation(jarFile.getLocation());

        String regClassname = ((JavaProjectHandler) mLanguage.getProjectHandler())
            .getRegistrationClassName(pUnoProject);
        description.setManifestProvider(new UnoManifestProvider(regClassname, pUnoProject, externalJars));
        description.setManifestLocation(pUnoProject.getFile("MANIFEST.MF").getFullPath()); //$NON-NLS-1$
        description.setSaveManifest(false);
        description.setReuseManifest(false);
        description.setExportOutputFolders(true);
        description.setExportClassFiles(true);
        description.setExportWarnings(true);
        description.setOverwrite(true);

        // Get the files to export: javamaker output + project classes
        FilesVisitor visitor = new FilesVisitor();
        visitor.addException(pUnoProject.getFolder(pUnoProject.getUrdPath()));

        IFolder buildDir = pUnoProject.getFolder(pUnoProject.getBuildPath());
        buildDir.accept(visitor);

        // Adding the source directory is not strictly necessary
        // (and it has practically no impact on the generated jar).
        // But if the build path is empty, the build fails.
        // So the contract seems to be that setElements must be called with a non-empty list of files.
        IFolder sourceDir = pUnoProject.getFolder(pUnoProject.getSourcePath());
        sourceDir.accept(visitor);
        description.setElements(visitor.getFiles());

        // Create the Jar file
        IJarExportRunnable runnable = description.createJarExportRunnable(null);
        runnable.run(new NullProgressMonitor());

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
            String cmdPattern = "javamaker -T {0}.* -nD -Gc -O \"{1}\" \"{2}\" {3}"; //$NON-NLS-1$
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

            if (errBuf.length() > 0) {
                PluginLogger.debug(errBuf.toString());
            }
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
        // Add the component Jar file
        JavaProjectHandler handler = (JavaProjectHandler) mLanguage.getProjectHandler();
        File libFile = SystemHelper.getFile(handler.getJarFile(pUnoPrj));
        File prjFile = SystemHelper.getFile(pUnoPrj);

        pUnoPackage.addComponentFile(
            UnoPackage.getPathRelativeToBase(libFile, prjFile),
            libFile, "Java"); //$NON-NLS-1$

        // Add all the jar dependencies
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(pUnoPrj.getName());
        IJavaProject javaPrj = JavaCore.create(prj);
        List<IFile> libs = getLibs(javaPrj);
        for (IFile lib : libs) {
            File jarFile = SystemHelper.getFile(lib);
            pUnoPackage.addOtherFile(UnoPackage.getPathRelativeToBase(jarFile, prjFile), jarFile);
        }

    }

    /**
     * either get list from libs dir when exist, or from the classpath
     * @param pJavaPrj
     * @return
     */
    private List<IFile> getLibs(IJavaProject pJavaPrj) {
        if (pJavaPrj.getProject().getFolder(LIBS_DIR_NAME).exists()) {
            return getLibsFromLibsDir(pJavaPrj);
        }
        return getLibsFromClasspath(pJavaPrj);
    }

    /**
     * Get the libraries in the classpath that are located in the project
     * directory or one of its subfolder.
     *
     * @param pJavaPrj the project from which to extract the libraries
     * @return a list of all the File pointing to the libraries.
     */
    private List<IFile> getLibsFromClasspath(IJavaProject pJavaPrj) {
        PluginLogger.debug("Collecting Jars from .classpath");

        ArrayList<IFile> libs = new ArrayList<>();
        try {
            IClasspathEntry[] entries = pJavaPrj.getResolvedClasspath(true);
            for (IClasspathEntry entry : entries) {
                if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                    /*
                     * At first, add only the libraries located in the project
                     * or one of its children. All others libraries have to be
                     * managed by the user.
                     */
                    IPath path = entry.getPath();
                    if (!new File(path.toOSString()).exists() && path.isAbsolute() &&
                        path.toString().startsWith("/" + pJavaPrj.getProject().getName())) { //$NON-NLS-1$
                        // Relative to the project
                        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                        if (file != null && file.exists()) {
                            libs.add(file);
                        }
                    }
                }
            }

        } catch (JavaModelException e) {
            // Enable to add some missing library
        }
        return libs;
    }

    /**
     * when an libs dir exist in the project then return a list of jars
     * @param pJavaPrj
     * @return list of jar files
     */
    private List<IFile> getLibsFromLibsDir(IJavaProject javaProject) {
        PluginLogger.debug("Collecting Jars from " + LIBS_DIR_NAME);

        List<IFile> libs = new ArrayList<>();
        IFolder libFolder = javaProject.getProject().getFolder(LIBS_DIR_NAME);
        if (libFolder.exists()) {
            java.nio.file.Path pathLibs = Paths.get(libFolder.getRawLocation().toOSString());
            try (Stream<java.nio.file.Path> walk = Files.walk(pathLibs)) {
                libs = walk.map(jarFile -> {
                    java.nio.file.Path pathRelative = pathLibs.relativize(jarFile);
                    return libFolder.getFile(pathRelative.toString());
                }).filter(f -> f.getFileExtension() != null && f.getFileExtension().equalsIgnoreCase("jar"))
                    .collect(Collectors.toList());
            } catch (IOException e) {
                PluginLogger.error(
                    Messages.getString("JavaBuilder.GetExternalLibsFailed"), e);
            }
        }
        PluginLogger.debug("Found " + libs.size() + " Jars");
        return libs;
    }
}
