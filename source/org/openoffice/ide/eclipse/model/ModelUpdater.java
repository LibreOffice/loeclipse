/*************************************************************************
 *
 * $RCSfile: ModelUpdater.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/30 13:24:30 $
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

package org.openoffice.ide.eclipse.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.openoffice.ide.eclipse.OOEclipsePlugin;

public class ModelUpdater implements IResourceDeltaVisitor {

	public boolean visit(IResourceDelta delta) throws CoreException {
		
		boolean visitChildren = true;
		
		try {
			performMovingUnoProject(delta);
			performRemovingUnoProject(delta);
			performRemovingIdlFile(delta);
			
		} catch (DoneException e) {
			visitChildren = false;
		}
		
			
		
		// TODO Listen on file rename, replace, delete and move actions
		
		return visitChildren;
	}

	
	/**
	 * Returns the UnoidlModel associated with the resource if possible,
	 * otherwise <code>null</code> is returned.
	 * 
	 * @param delta Resource delta from which to get the unoidl project 
	 * @return UnoidlProject or <code>null</code>;
	 */
	private UnoidlProject getUnoProject(IResourceDelta delta) {
		
		UnoidlProject unoProject = null;
		IResource res = delta.getResource();
		IProject project = res.getProject();
		
		try {
			if (project.hasNature(OOEclipsePlugin.UNO_NATURE_ID)) {
				unoProject = (UnoidlProject)project.getNature(
						OOEclipsePlugin.UNO_NATURE_ID);
			}
		} catch (CoreException e) {
			// Do nothing
		}
			
		return unoProject;
	}
	
	private boolean isIdlFile(IResourceDelta delta) {
		
		boolean result = false;
		
		IResource res = delta.getResource();
		if (res instanceof IFile && 
				res.getName().endsWith(".idl") && 
				null != getUnoProject(delta)) {
			result = true;
		}
		
		return result;
	}
	
	private void performMovingUnoProject(IResourceDelta delta) throws DoneException {
		
		IResource res = delta.getResource();
		if (res instanceof IProject) {
			UnoidlProject unoProject = getUnoProject(delta);
			if (null != unoProject) {
				
				int flags = delta.getFlags();
				int kind = delta.getKind();
				int movedFlag = IResourceDelta.MOVED_TO;
				int replacedFlag = IResourceDelta.REPLACED;
				
				boolean isMovedTo = (movedFlag == (flags & movedFlag));
				boolean isReplaced = (replacedFlag ==(flags & replacedFlag));
				
				if ((IResourceDelta.REMOVED==kind && isMovedTo) || 
						(IResourceDelta.CHANGED)==kind && isReplaced) {
					
					try {
						unoProject.move(null, 
								delta.getMovedToPath().segment(0));
						throw new DoneException();
					} catch (TreeException e) {
						e.printStackTrace(); // TODO Handle the error
					}
				}
			}
		}
	}
	
	private void performRemovingUnoProject(IResourceDelta delta) throws DoneException{
		
		IResource res = delta.getResource();
		if (res instanceof IProject) {
			UnoidlProject unoProject = getUnoProject(delta);
			if (null != unoProject) {
				
				int flags = delta.getFlags();
				int kind = delta.getKind();
				int movedFlag = IResourceDelta.MOVED_TO;
				
				boolean isMovedTo = (movedFlag == (flags & movedFlag));
				
				if ((IResourceDelta.REMOVED==kind && !isMovedTo)) {
					
					unoProject.dispose();
					throw new DoneException();
				}
			}
		}
	}
	
	private void performRemovingIdlFile(IResourceDelta delta) throws DoneException {
		
		if (isIdlFile(delta)) {
			
			int flags = delta.getFlags();
			int kind = delta.getKind();
			int movedFlag = IResourceDelta.MOVED_TO;
			
			boolean isMovedTo = (movedFlag == (flags & movedFlag));
			
			if ((IResourceDelta.REMOVED==kind && !isMovedTo)) {
				
				IFile file = (IFile)delta.getResource();
				UnoidlProject unoProject = getUnoProject(delta);
				
				String path = unoProject.getPath() + unoProject.getSeparator() +
					unoProject.getIdlRelativePath(file).
						toString().replace('.', '_');
				
				TreeNode node = unoProject.findNode(path);
				if (null != node) {
					node.dispose();
				}
				
				throw new DoneException();
			}
		}
	}
	
	/**
	 * Exception used to indicate that an action has been performed and 
	 * force the visitor to avoid the others
	 * 
	 * @author cbosdonnat
	 *
	 */
	public class DoneException extends Exception {

		private static final long serialVersionUID = 5354983581871198186L;

		public DoneException() {
			super();
		}
	}
}
