/*************************************************************************
 *
 * $RCSfile: IdlcBuilder.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/10 12:07:17 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the following licenses
 *
 *     - GNU Lesser General Public License Version 2.1
 *     - Sun Industry Standards Source License Version 1.1
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
 *
 * Sun Industry Standards Source License Version 1.1
 * =================================================
 * The contents of this file are subject to the Sun Industry Standards
 * Source License Version 1.1 (the "License"); You may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.openoffice.org/license.html.
 *
 * Software provided under this License is provided on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 * MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 * See the License for the specific provisions governing your rights and
 * obligations concerning the Software.
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
package org.openoffice.ide.eclipse.builders;

import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.model.UnoidlProject;
import org.openoffice.ide.eclipse.preferences.sdk.SDK;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class IdlcBuilder extends IncrementalProjectBuilder implements IResourceChangeListener {

	/**
	 * Identifier defined in the <code>plugin.xml</code> file for thi kind of builder
	 */
	public final static String BUILDER_ID = OOEclipsePlugin.OOECLIPSE_PLUGIN_ID + ".idlc";
	
	/**
	 * Identifier defined in the <code>plugin.xml</code> file for the 
	 * marker associated with this builder errors.
	 */
	public final static String IDLERROR_MARKER_ID = OOEclipsePlugin.OOECLIPSE_PLUGIN_ID + ".idlcerrormarker";
	
	/**
	 * UNOI-IDL project handled. This is a quick access to the project nature 
	 */
	private UnoidlProject unoidlProject;
	
	/**
	 * Vector of the urd files to delete after the deletion of their idl file reference
	 */
	private Vector urdToDelete = new Vector();
	
	/**
	 * Constructor for the idlc builder
	 *
	 */
	public IdlcBuilder(UnoidlProject project) {
		super();
		
		unoidlProject = project;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		// Removes the urd to delete
		for (int i=0, length=urdToDelete.size(); i<length; i++){
			IResource resource = (IResource)urdToDelete.get(i);
			if (resource.exists()){
				resource.delete(true, monitor);
			}
		}
		urdToDelete.clear();
			
		fullBuild(monitor);

		return null;
	}

	/**
	 * Method that perform the full build of the project.
	 * TODOC further documentation to write
	 * 
	 * @param monitor
	 */
	private void fullBuild(IProgressMonitor monitor) {

		try {
			// compile each idl file
			IFolder idlFolder = unoidlProject.getProject().getFolder(unoidlProject.getUnoidlLocation());
			idlFolder.accept(new IdlcBuildVisitor(monitor));
			
		} catch (CoreException e) {
			OOEclipsePlugin.logError("Error raised during the idlc compilation", e);
		}
	}
	
	/**
	 * 
	 * @param file
	 * @param monitor
	 */
	static void runIdlcOnFile(IFile file, IProgressMonitor monitor){
		
		try {
			UnoidlProject project = (UnoidlProject)file.getProject().getNature(OOEclipsePlugin.UNO_NATURE_ID);
			SDK sdk = project.getSdk();
			
			if (null != sdk){
				
				// Get local references to the SDK used members
				String sdkHome = sdk.getSDKHome();
				
				Path sdkPath = new Path(sdkHome);
				IPath outputLocation = project.getUrdLocation().append(
						file.getProjectRelativePath().removeLastSegments(1));
				
				String command = "idlc -O " + outputLocation.toOSString() +
								     " -I " + sdkPath.append("idl").toOSString() + " " +
								     file.getProjectRelativePath().toString(); 

				Process process = OOEclipsePlugin.runTool(project.getProject(), command, monitor);
				
				IdlcErrorReader errorReader = new IdlcErrorReader(process.getErrorStream(), file);
				errorReader.readErrors();
				
				// Do not forget to destroy the process
				process.destroy();
				
			} else {
				// TODO Toggle sdk error marker if it doesn't exist
			}
		} catch (CoreException e) {
			// Not a uno nature
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(I18nConstants.NOT_UNO_PROJECT), e);
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		
		if (null != unoidlProject){
			
			switch (event.getType()) {
				case IResourceChangeEvent.POST_CHANGE:
					
					// Visit the delta only if it has been removed
					if (IResourceDelta.REMOVED == event.getDelta().getKind() || 
							IResourceDelta.CHANGED == event.getDelta().getKind()){
						
						// Handle idl file deletion to delete their associated urd files if they exists
						try {
							event.getDelta().accept(new IdlDeleted1Visitor());
						} catch (CoreException e) {
							// In the worst case, the urd file will not be deleted: do not log the error
						}
					}
					break;
			}
		}
	} 
	
	class IdlDeleted1Visitor implements IResourceDeltaVisitor {
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			boolean visitChildren = true;
			
			// If the host resource path begins with the unoidl location
			if (delta.getResource().getProjectRelativePath().toString().
							startsWith(unoidlProject.getUnoidlLocation().toString())){
				
				if (IResource.FILE == delta.getResource().getType() && 
						delta.getResource().getFileExtension().equals("idl") || 
				 		IResource.FOLDER == delta.getResource().getType()){
					
					// Check if the resource is deleted
					if (IResourceDelta.REMOVED == delta.getKind()){
						
						// Compute the name of the urd file / folder
						
						IPath idlPath = delta.getResource().getProjectRelativePath();
						IPath urdPath = unoidlProject.getUrdLocation();
						if (IResource.FILE == delta.getResource().getType()){
							urdPath = urdPath.append(idlPath.removeFileExtension().addFileExtension("urd"));
							IFile urdFile = getProject().getFile(urdPath);
							
							if (urdFile.exists()){
								urdToDelete.add(urdFile);
							}
						} else {
							// Necessarily a folder
							urdPath = urdPath.append(idlPath);
							if (null != getProject()){
								IFolder urdFolder = getProject().getFolder(urdPath);
							
							
								if (urdFolder.exists()){
									urdToDelete.add(urdFolder);
								}
							}
							
							visitChildren = false;
						}
					}
				}
			}
			
			if (delta.getResource().getProjectRelativePath().toString().
									startsWith(unoidlProject.getUrdLocation().toString()) || 
					delta.getResource().getProjectRelativePath().toString().
									startsWith(unoidlProject.getCodeLocation().toString())){
				visitChildren = false;
			}
			
			return visitChildren;
		}
		
	}
}
