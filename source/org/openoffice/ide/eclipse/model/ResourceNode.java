/*************************************************************************
 *
 * $RCSfile: ResourceNode.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/10 12:07:20 $
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

import org.eclipse.core.resources.IResource;

public abstract class ResourceNode extends TreeNode implements IOpenable {
	
	public static final String PATH_DELIMITER = "/";
	
	public ResourceNode(TreeNode parent, IResource aResource, String aPath) {
		super(parent, aPath);
		setResource(aResource);
	}

	public String getName() {
		String name = getPath();
		
		String[] splittedPath = getPath().split(PATH_DELIMITER);
		if (1 < splittedPath.length){
			name = splittedPath[splittedPath.length - 1];
		}
		return name;
	}
	
	//------------------------------------------------------ Resource managment
	
	private IResource resource;
	
	public IResource getResource(){
		return resource;
	}
	
	private void setResource(IResource aResource){
		resource = aResource;
	}

	//---------------------------------------------------- Implements IOpenable
	
	/**
	 * This method should create all the children nodes of this one. This 
	 * mechanism is used to gain memory on huge trees.
	 * 
	 */
	public abstract void open();

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IOpenable#close()
	 */
	public void close() {
		removeAllNodes();
	}
}
