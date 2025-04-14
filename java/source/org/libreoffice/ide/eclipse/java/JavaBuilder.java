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
import org.libreoffice.ide.eclipse.java.build.FilesVisitor;
import org.libreoffice.ide.eclipse.java.build.Messages;
import org.libreoffice.ide.eclipse.java.build.UnoManifestProvider;
import org.libreoffice.plugin.core.model.UnoPackage;

/**
 * The language builder implementation for Java.
 */
public class JavaBuilder implements ILanguageBuilder {

    // XXX: directory in the project that contains the external
    // XXX: jar files and in sub-directories the source jar files
    private static final String LIB_DIR_NAME = "lib";
    private static final String LIBS_DIR_NAME = "libs";
    private Language mLanguage;

    /**
     * Constructor.
     *
     * @param language the Java Language object
     */
    public JavaBuilder(Language language) {
        mLanguage = language;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFile createLibrary(IUnoidlProject unoProject) throws Exception {
        IFile jarFile = ((JavaProjectHandler) mLanguage.getProjectHandler()).getJarFile(unoProject);

        // Add all the jar dependencies
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(unoProject.getName());
        IJavaProject javaPrj = JavaCore.create(prj);
        List<IResource> externalJars = getProjectLibs(unoProject, javaPrj);

        JarPackageData description = new JarPackageData();
        description.setGenerateManifest(true);
        description.setJarLocation(jarFile.getLocation());

        String regClassname = ((JavaProjectHandler) mLanguage.getProjectHandler())
            .getRegistrationClassName(unoProject);
        description.setManifestProvider(new UnoManifestProvider(regClassname, unoProject, externalJars));
        description.setManifestLocation(unoProject.getFile("MANIFEST.MF").getFullPath()); //$NON-NLS-1$
        description.setSaveManifest(false);
        description.setReuseManifest(false);
        description.setExportOutputFolders(true);
        description.setExportClassFiles(true);
        description.setExportWarnings(true);
        description.setOverwrite(true);

        // Get the files to export: javamaker output + project classes
        FilesVisitor visitor = new FilesVisitor();
        visitor.addException(unoProject.getFolder(unoProject.getUrdPath()));
        // XXX: retrieve value from configuration
        visitor.addException(unoProject.getFolder(unoProject.getBuildPath().append("idl")));
        visitor.addException(unoProject.getFolder(unoProject.getBuildPath().append("classes")));

        IFolder buildDir = unoProject.getFolder(unoProject.getBuildPath());
        buildDir.accept(visitor);

        // Adding the source directory is not strictly necessary
        // (and it has practically no impact on the generated jar).
        // But if the build path is empty, the build fails.
        // So the contract seems to be that setElements must be called with a non-empty list of files.
        IFolder sourceDir = unoProject.getFolder(unoProject.getSourcePath());
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
    public void generateFromTypes(ISdk sdk, IOOo ooo, IProject prj,
                                  File typesFile, File buildFolder,
                                  String rootModule, IProgressMonitor monitor) {

        if (typesFile.exists()) {

            if (null != sdk && null != ooo) {

                String[] paths = ooo.getTypesPath();
                String oooTypesArgs = ""; //$NON-NLS-1$
                for (String path : paths) {
                    IPath ooTypesPath = new Path(path);
                    oooTypesArgs += " -X\"" + ooTypesPath.toOSString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
                }

                // Todo: What if the user creates other root modules ?
                String firstModule = rootModule.split("::")[0]; //$NON-NLS-1$

                runJavamaker(firstModule, oooTypesArgs, sdk, prj, typesFile, buildFolder, monitor);
            }
        }
    }

    private void runJavamaker(String firstModule, String typesArgs,
                              ISdk sdk, IProject prj, File typesFile,
                              File buildFolder, IProgressMonitor monitor) {

        StringBuffer errBuf = new StringBuffer();
        try {
            String template = "{0} -T {1}.* -nD -Gc -O \"{2}\" \"{3}\" {4}"; //$NON-NLS-1$
            String command = MessageFormat.format(template,
                sdk.getCommand("javamaker"), //$NON-NLS-1$
                firstModule,
                buildFolder.getAbsolutePath(),
                typesFile.getAbsolutePath(),
                typesArgs);

            IUnoidlProject unoprj = ProjectsManager.getProject(prj.getName());
            Process process = sdk.runTool(unoprj, command, monitor);

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
    public String[] getBuildEnv(IUnoidlProject unoProject) {

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(unoProject.getName());

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
                // Todo log a problem to find the JVM associated to the project
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
    public void fillUnoPackage(UnoPackage unoPackage, IUnoidlProject unoPrj) {
        // Add the component Jar file
        JavaProjectHandler handler = (JavaProjectHandler) mLanguage.getProjectHandler();
        File libFile = SystemHelper.getFile(handler.getJarFile(unoPrj));
        File prjFile = SystemHelper.getFile(unoPrj);

        unoPackage.addComponentFile(
            UnoPackage.getPathRelativeToBase(libFile, prjFile),
            libFile, "Java"); //$NON-NLS-1$

        // Add all the external jar dependencies
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(unoPrj.getName());
        IJavaProject javaPrj = JavaCore.create(prj);
        List<IResource> libs = getProjectLibs(unoPrj, javaPrj);
        for (IResource lib : libs) {
            File jarFile = SystemHelper.getFile(lib);
            unoPackage.addOtherFile(UnoPackage.getPathRelativeToBase(jarFile, prjFile), jarFile);
        }

    }

    /**
     * either get list from lib or libs dir when exist, or from the classpath.
     * 
     * @param unoPrj the UNO project from which to extract the libraries
     * 
     * @param javaPrj the Java project from which to extract the libraries
     * 
     * @return list of IFile
     */
    private List<IResource> getProjectLibs(IUnoidlProject unoPrj, IJavaProject javaPrj) {
        List<IResource> libs = new ArrayList<>();
        if (javaPrj.getProject().getFolder(LIB_DIR_NAME).exists()) {
            libs = getLibsFromDir(javaPrj, LIB_DIR_NAME);
        } else if (javaPrj.getProject().getFolder(LIBS_DIR_NAME).exists()) {
            libs = getLibsFromDir(javaPrj, LIBS_DIR_NAME);
        } else {
            libs = JavaClassPathProvider.getProjectLibs(unoPrj, javaPrj);
        }
        PluginLogger.debug("Found " + libs.size() + " Jars");
        return libs;
    }

    /**
     * Return all jar files contained in the folder without taking into account sub folders.
     * 
     * @param javaProject the java project
     * 
     * @param folder the folder in which we must search
     * 
     * @return list of jar files
     */
    private List<IResource> getLibsFromDir(IJavaProject javaProject, String folder) {
        PluginLogger.debug("Collecting Jars from: /" + folder);

        List<IResource> libs = new ArrayList<>();
        IFolder libFolder = javaProject.getProject().getFolder(folder);
        if (libFolder.exists()) {
            java.nio.file.Path pathLibs = Paths.get(libFolder.getRawLocation().toOSString());
            try (Stream<java.nio.file.Path> walk = Files.walk(pathLibs)) {
                libs = walk.map(jarFile -> {
                    java.nio.file.Path pathRelative = pathLibs.relativize(jarFile);
                    return libFolder.getFile(pathRelative.toString());
                }).filter(f -> f.getType() == IResource.FILE)
                  .filter(f -> "jar".equals(f.getFileExtension()))
                  .collect(Collectors.toList());
            } catch (IOException e) {
                PluginLogger.error(Messages.getString("JavaBuilder.GetLibsFromDirFailed"), e);
            }
        }
        return libs;
    }
}
