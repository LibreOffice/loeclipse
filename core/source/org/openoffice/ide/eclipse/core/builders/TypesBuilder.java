/*************************************************************************
 *
 * $RCSfile: TypesBuilder.java,v $
 *
 * $Revision: 1.11 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/07/17 21:01:02 $
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
package org.openoffice.ide.eclipse.core.builders;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.openoffice.ide.eclipse.core.preferences.ISdk;

/**
 * <p>Main builder of the UNO-IDL projects, it computes the language specific
 * type files and types registry from the <code>idl</code> files. In order
 * to split the work, the different tasks have been split into several builders:
 *   <ul>
 *     <li>{@link RegmergeBuilder} merging the urd files into the types 
 *     	   registry</li>
 *     <li>{@link ILanguageBuilder#generateFromTypes(ISdk, org.openoffice.ide.eclipse.core.preferences.IOOo, IProject, File, File, String, IProgressMonitor)}
 *     	   generating the language specific type files</li>
 *   </ul>
 * </p>
 * 
 * @author cbosdonnat
 *
 */
public class TypesBuilder extends IncrementalProjectBuilder {

	/**
	 * The builder ID as set in the <code>plugin.xml</code> file
	 */
	public static final String BUILDER_ID = OOEclipsePlugin.OOECLIPSE_PLUGIN_ID + ".types"; //$NON-NLS-1$
	
	private boolean changedIdl = false;
	static int sBuildState = -1;
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		changedIdl = false;
		
		if (sBuildState < 0) {
			IResourceDelta delta = getDelta(getProject());
			if (delta != null) {
				delta.accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta) throws CoreException {

						boolean visitChildren = false;

						IProject prj = getProject();
						IUnoidlProject unoprj = ProjectsManager.getProject(prj.getName());
						IPath idlPath = unoprj.getIdlPath();
						IPath resPath = delta.getResource().getProjectRelativePath();

						if (delta.getResource() instanceof IContainer && 
								resPath.segmentCount() < idlPath.segmentCount()) {
							visitChildren = true;
						} else if (delta.getResource() instanceof IContainer && 
								resPath.toString().startsWith(idlPath.toString())) {
							visitChildren = true;
						} else if (delta.getResource() instanceof IFile) {
							if (resPath.getFileExtension().equals("idl")) { //$NON-NLS-1$
								visitChildren = false;
								changedIdl = true;
							} else if (resPath.toString().endsWith(unoprj.getTypesPath().toString())) {
								sBuildState = 4;
							}
						}
						return visitChildren;
					}
				});
			} else {
				changedIdl = true;
			}

			if (changedIdl && sBuildState < 0) {
				try {
					build(getProject(), monitor);
				} catch (CoreException e) {
					sBuildState = -1;
					throw e;
				}
				sBuildState = -1;
			} else if (sBuildState == 4) {
				sBuildState = -1;
			}
		}
		
		return null;
	}
	
	public static void build(IProject prj, IProgressMonitor monitor) 
			throws CoreException {
		
		IUnoidlProject unoprj = ProjectsManager.getProject(
				prj.getName());
		
		// Clears the registries before beginning
		sBuildState = 1;
		removeAllRegistries(prj);
		buildIdl(unoprj, monitor);
		
		sBuildState = 2;
		RegmergeBuilder.build(unoprj, monitor);
		
		sBuildState = 3;
		File types = prj.getLocation().append(unoprj.getTypesPath()).toFile();
		File build = prj.getLocation().append(unoprj.getBuildPath()).toFile();
		
		unoprj.getLanguage().getLanguageBuidler().generateFromTypes(
				unoprj.getSdk(),
				unoprj.getOOo(),
				prj,
				types, build,
				unoprj.getRootModule(), monitor);
		
		prj.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		sBuildState = -1;
		
	}
	
	/**
	 * Removes all the registries, ie <code>.urd</code> and 
	 * <code>types.rdb</code> files.
	 * 
	 * @param unoproject the Uno project from which to remove the registries
	 */
	private static void removeAllRegistries(IProject prj) {
		
		IUnoidlProject unoprj = ProjectsManager.getProject(
				prj.getName());
		
		try {
			IPath rdbPath = unoprj.getTypesPath();
			IFile rdbFile = prj.getFile(rdbPath);
			if (rdbFile.exists()) {
				rdbFile.delete(true, null);
			}
			
			IPath urdPath = unoprj.getUrdPath();
			IFolder urdFolder = prj.getFolder(urdPath);
			IResource[] members = urdFolder.members();
			
			for (int i=0, length=members.length; i<length; i++) {
				IResource resi = members[i];
				if (resi.exists()) {
					resi.delete(true, null);
				}
			}
			
		} catch (CoreException e) {
			PluginLogger.debug(e.getMessage());
		}
	}
	
	/**
	 * Runs the idl files compilation
	 * 
	 * @param project the uno project to build
	 * @param monitor a monitor to watch the progress
	 * @throws CoreException if anything wrong happened
	 */
	public static void buildIdl(IUnoidlProject project, IProgressMonitor monitor)
			throws CoreException {

		// Build each idlc file
		try {
			// compile each idl file
			IFolder idlFolder = project.getFolder(
					project.getRootModulePath());
			idlFolder.accept(new IdlcBuildVisitor(monitor));
			

		} catch (CoreException e) {
			PluginLogger.error(
					Messages.getString("IdlcBuilder.IdlcError"), e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Convenience method to execute the <code>idlc</code> tool on a given
	 * file.
	 * 
	 * @param file the file to run <code>idlc</code> on.
	 * @param monitor a progress monitor
	 */
	static void runIdlcOnFile(IFile file, IProgressMonitor monitor){
		
		IUnoidlProject project = ProjectsManager.getProject(file.getProject().getName());
		
		ISdk sdk = project.getSdk();
		
		if (null != sdk){
			
			// Get local references to the SDK used members
			String sdkHome = sdk.getHome();
			
			Path sdkPath = new Path(sdkHome);
			int segmentCount = project.getIdlPath().segmentCount();
			
			IPath outputLocation = project.getUrdPath().append(
					file.getProjectRelativePath().removeLastSegments(1).
					removeFirstSegments(segmentCount));
			
			String command = "idlc -O \"" + outputLocation.toOSString() + "\"" + //$NON-NLS-1$
				" -I \"" + sdkPath.append("idl").toOSString() + "\"" + //$NON-NLS-1$ //$NON-NLS-2$
				" -I \"" + project.getIdlPath().toOSString() + "\"" +  //$NON-NLS-1$
				" " + file.getProjectRelativePath().toOSString();  //$NON-NLS-1$
			
			Process process = project.getSdk().runTool(project, command, monitor);
			
			IdlcErrorReader errorReader = new IdlcErrorReader(
					process.getErrorStream(), file);
			errorReader.readErrors();
		}
	}
}
