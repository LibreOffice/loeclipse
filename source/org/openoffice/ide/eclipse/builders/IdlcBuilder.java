/*************************************************************************
 *
 * $RCSfile: IdlcBuilder.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/30 13:24:27 $
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
import org.eclipse.core.resources.IncrementalProjectBuilder;
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
public class IdlcBuilder extends IncrementalProjectBuilder {

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
			IFolder idlFolder = unoidlProject.getProject().getFolder(unoidlProject.getUnoidlPrefixPath());
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
			UnoidlProject project = (UnoidlProject)file.getProject().getNature(
					OOEclipsePlugin.UNO_NATURE_ID);
			SDK sdk = project.getSdk();
			
			if (null != sdk){
				
				// Get local references to the SDK used members
				String sdkHome = sdk.getSDKHome();
				
				Path sdkPath = new Path(sdkHome);
				int segmentCount = UnoidlProject.IDL_BASIS.split("/").length;
				
				IPath outputLocation = project.getUrdLocation().append(
						file.getProjectRelativePath().removeLastSegments(1).
								removeFirstSegments(segmentCount));
				
				String command = "idlc -O " + outputLocation.toOSString() +
								     " -I " + sdkPath.append("idl").toOSString() +
								     " -I " + project.getUnoidlLocation().toOSString() + 
								     " " + file.getProjectRelativePath().toString(); 

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
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
					I18nConstants.NOT_UNO_PROJECT), e);
		}
	}
}
