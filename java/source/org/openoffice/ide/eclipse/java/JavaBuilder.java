/*************************************************************************
 *
 * $RCSfile: JavaBuilder.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/12/06 07:46:43 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.UnoPackage;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;
import org.openoffice.ide.eclipse.core.utils.ZipContent;
import org.openoffice.ide.eclipse.java.utils.ZipContentHelper;

public class JavaBuilder implements ILanguageBuilder {

	private Language mLanguage;
	
	public JavaBuilder(Language language) {
		mLanguage = language;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder#createLibrary(org.openoffice.ide.eclipse.core.model.IUnoidlProject)
	 */
	public IPath createLibrary(IUnoidlProject unoProject) throws Exception {

		// Create the manifest file
		String classpath = ""; //$NON-NLS-1$
		IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(unoProject.getName());
		Vector<File> libs = getLibs(JavaCore.create(prj));
		String prjPath = prj.getLocation().toOSString();
		for (File lib : libs) {
			String relPath = lib.getPath().substring(prjPath.length()+1);
			classpath += relPath + " "; //$NON-NLS-1$
		}
		if (!classpath.equals("")) {
			classpath = "Class-Path: " + classpath + "\r\n"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		String classname = ((JavaProjectHandler)mLanguage.getProjectHandler()).
			getRegistrationClassName(unoProject);
		String content = "ManifestVersion: 1.0\r\n" +  //$NON-NLS-1$
						 "RegistrationClassName: " + classname + "\r\n" + //$NON-NLS-1$ //$NON-NLS-2$
						 classpath;
		
		
		File manifestFile = new File(unoProject.getFile("MANIFEST.MF"). //$NON-NLS-1$
				getLocation().toOSString());
		StringReader reader = new StringReader(content);
		FileWriter writer = new FileWriter(manifestFile);
		try {
			int c = reader.read();

			while (c != -1){
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
				getJarFile(unoProject);
		FileOutputStream out = new FileOutputStream(jarFile);
		JarOutputStream jarOut = new JarOutputStream(out);
		
		// Add the manifest
		ZipContent manifest = new ZipContent("META-INF/MANIFEST.MF", manifestFile); //$NON-NLS-1$
		manifest.writeContentToZip(jarOut);
		
		// Get all the files to write
		File bin = new File(unoProject.getFolder("bin"). //$NON-NLS-1$
				getLocation().toOSString());
		ZipContent[] binContent = ZipContentHelper.getFiles(bin);  // JDT dependent
		
		File build = new File(unoProject.getFolder(unoProject.getBuildPath()).
				getLocation().toOSString());
		ZipContent[] javamakerContent = ZipContentHelper.getFiles(build);
		
		// write the content of the bin directory to the Jar
		for (int i=0; i<binContent.length; i++) {
			binContent[i].writeContentToZip(jarOut);
		}
		
		for (int i=0; i<javamakerContent.length; i++) {
			javamakerContent[i].writeContentToZip(jarOut);
		}
		
		// Close all the streams
		jarOut.close();
		out.close();
		
		return new Path(jarFile.getAbsolutePath());
	}


	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder#generateFromTypes(org.openoffice.ide.eclipse.core.preferences.ISdk, org.openoffice.ide.eclipse.core.preferences.IOOo, org.eclipse.core.resources.IProject, java.io.File, java.io.File, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void generateFromTypes(ISdk sdk, IOOo ooo, IProject prj, File typesFile,
			File buildFolder, String rootModule, IProgressMonitor monitor) {
		
		if (typesFile.exists()){
			
			try {
				
				if (null != sdk && null != ooo){
					
					IPath ooTypesPath = new Path (ooo.getTypesPath());
					
					// TODO What if the user creates other root modules ?
					String firstModule = rootModule.split("::")[0]; //$NON-NLS-1$
					
					String command = "javamaker -T" + firstModule +  //$NON-NLS-1$
						".* -nD -Gc -BUCR " +  //$NON-NLS-1$
						"-O " + buildFolder.getAbsolutePath() + " " + //$NON-NLS-1$
						typesFile.getAbsolutePath() + " " + //$NON-NLS-1$
						"-X\"" + ooTypesPath.toOSString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
					
					IUnoidlProject unoprj = ProjectsManager.getInstance().getProject(prj.getName());
					Process process = sdk.runTool(unoprj,command, monitor);
					
					LineNumberReader lineReader = new LineNumberReader(
							new InputStreamReader(process.getErrorStream()));
					
					// Only for debugging purpose
					if (PluginLogger.isLevel(PluginLogger.DEBUG)){ //$NON-NLS-1$
					
						String line = lineReader.readLine();
						while (null != line){
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

	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder#getBuildEnv(org.openoffice.ide.eclipse.core.model.IUnoidlProject, org.eclipse.core.resources.IProject)
	 */
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
				for (int i=0; i<cpEntry.length; i++) {
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
				// TODO log a problem to fing the JVM associated to the project
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
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder#fillUnoPackage(org.openoffice.ide.eclipse.core.builders.UnoPackage, org.openoffice.ide.eclipse.core.model.IUnoidlProject)
	 */
	public void fillUnoPackage(UnoPackage unoPackage, IUnoidlProject unoPrj) {
				
		// Add the component Jar file
		JavaProjectHandler handler = (JavaProjectHandler)mLanguage.getProjectHandler();
		unoPackage.addComponentFile(handler.getJarFile(unoPrj), "Java");
		
		// Add all the jar dependencies
		IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(unoPrj.getName());
		IJavaProject javaPrj = JavaCore.create(prj);
		Vector<File> libs = getLibs(javaPrj);
		for (File lib : libs) {
			unoPackage.addTypelibraryFile(lib, "Java");
		}
	}
	
	/**
	 * Get the libraries in the classpath that are located in the project
	 * directory or one of its subfolder. 
	 * 
	 * @param javaPrj the project from which to extract the libraries
	 * @return a list of all the File pointing to the libraries.
	 */
	private Vector<File> getLibs(IJavaProject javaPrj) {
		Vector<File> libs = new Vector<File>();
		IPath prjPath = javaPrj.getProject().getLocation();
		
		try {
			IClasspathEntry[] entries = javaPrj.getResolvedClasspath(true);
			for (IClasspathEntry entry : entries) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					/*
					 * At first, add only the libraries located in the project
					 * or one of its children. All others libraries have to be
					 * managed y the user.
					 */
					IPath path = entry.getPath();
					if (!new File(path.toOSString()).exists() && path.isAbsolute()) {
						// This is a workspace relative path
						if (path.toString().startsWith("/" + javaPrj.getProject().getName())) {
							// Relative to the project
							File libFile = prjPath.append(path.removeFirstSegments(1)).toFile();
							if (libFile.isFile()) {
								libs.add(libFile);
							}
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
