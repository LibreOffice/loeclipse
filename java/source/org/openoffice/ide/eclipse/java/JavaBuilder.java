/*************************************************************************
 *
 * $RCSfile: JavaBuilder.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/11 18:39:35 $
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
import java.util.jar.JarOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
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
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;
import org.openoffice.ide.eclipse.java.utils.ZipContent;

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
		
		String classname = ((JavaProjectHandler)mLanguage.getProjectHandler()).
			getRegistrationClassName(unoProject);
		String content = "ManifestVersion: 1.0\r\n" +  //$NON-NLS-1$
						 "RegistrationClassName: " + classname + "\r\n"; //$NON-NLS-1$ //$NON-NLS-2$
		
		
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
		ZipContent[] binContent = ZipContent.getFiles(bin);  // JDT dependent
		
		File build = new File(unoProject.getFolder(unoProject.getBuildPath()).
				getLocation().toOSString());
		ZipContent[] javamakerContent = ZipContent.getFiles(build);
		
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
	 * @see org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder#generateFromTypes(org.openoffice.ide.eclipse.core.preferences.ISdk, org.openoffice.ide.eclipse.core.preferences.IOOo, org.eclipse.core.resources.IFile, org.eclipse.core.resources.IFolder, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void generateFromTypes(ISdk sdk, IOOo ooo, IFile typesFile,
			IFolder buildFolder, String rootModule, IProgressMonitor monitor) {
		
		if (typesFile.exists()){
			
			try {
				
				if (null != sdk && null != ooo){
					
					IPath ooTypesPath = new Path (ooo.getTypesPath());
					
					// TODO What if the user creates other root modules ?
					String firstModule = rootModule.split("::")[0]; //$NON-NLS-1$
					
					// HELP quotes are placed here to prevent Windows path 
					// names with spaces
					String command = "javamaker -T" + firstModule +  //$NON-NLS-1$
						".* -nD -Gc -BUCR " +  //$NON-NLS-1$
						"-O ." + System.getProperty("file.separator") +  //$NON-NLS-1$ //$NON-NLS-2$
						buildFolder.getProjectRelativePath().toOSString() + " " + //$NON-NLS-1$
						typesFile.getProjectRelativePath().toOSString() + " " + //$NON-NLS-1$
						"-X\"" + ooTypesPath.toOSString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
					
					
					Process process = OOEclipsePlugin.runTool(
							ProjectsManager.getInstance().getProject(
									typesFile.getProject().getName()),
							command, monitor);
					
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
	public String[] getBuildEnv(IUnoidlProject unoProject, IProject project) {
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

}
