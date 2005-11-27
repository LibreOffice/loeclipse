/*************************************************************************
 *
 * $RCSfile: ResourceNode.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/11/27 17:48:14 $
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
package org.openoffice.ide.eclipse.model;

import org.eclipse.core.resources.IResource;

public abstract class ResourceNode extends TreeNode implements IOpenable {
	
	public static final String PATH_DELIMITER = "/";
	
	public ResourceNode(TreeNode parent, IResource aResource, String aName) {
		super(parent, aName);
		setResource(aResource);
	}
	
	public String getSeparator() {
		return PATH_DELIMITER;
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
