/*************************************************************************
 *
 * $RCSfile: IdlcBuildVisitor.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/08/20 11:55:51 $
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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * Class visiting each child of the idl folder to generate the corresponding
 * <code>urd</code> file.
 * 
 * @author cbosdonnat
 *
 */
public class IdlcBuildVisitor implements IResourceVisitor {
	
	private IProgressMonitor mProgressMonitor;
	
	/**
	 * Default constructor
	 * 
	 * @param monitor progress monitor
	 */
	public IdlcBuildVisitor(IProgressMonitor monitor) {
		mProgressMonitor = monitor;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
	 */
	public boolean visit(IResource resource) throws CoreException {
		
		boolean visitChildren = false;

		if (IResource.FILE == resource.getType()){
			
			// Try to compile the file if it is an idl file
			if (resource.getFileExtension().equals("idl")){ //$NON-NLS-1$
				
				IdlcBuilder.runIdlcOnFile((IFile)resource, mProgressMonitor);
				if (mProgressMonitor != null) mProgressMonitor.worked(1);
			}
			
		} else if (resource instanceof IContainer){
			
			IUnoidlProject project = ProjectsManager.getInstance().getProject(
					resource.getProject().getName());

			if (resource.getProjectRelativePath().toString().startsWith(
					project.getRootModulePath().segment(0))){
				
				visitChildren = true;
			}
		}
		
		// cleaning
		mProgressMonitor = null;
		
		return visitChildren;
	}
}
