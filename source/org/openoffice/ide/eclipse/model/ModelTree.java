/*************************************************************************
 *
 * $RCSfile: ModelTree.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/11/27 17:48:15 $
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

import java.util.Iterator;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class ModelTree extends TreeNode {

	public ModelTree() {
		super(null, "");
	}

	/**
	 * The tree root has no name
	 * 
	 * @see org.openoffice.ide.eclipse.model.TreeNode#getName()
	 */
	public String getName() {
		return null;
	}
	
	/**
	 * Try to find to node with the given path in this part of the tree. If
	 * the node can't be found, <code>null</code> is returned.
	 * 
	 * @param path path to the searched node
	 * @return existing node or <code>null</code> if not found.
	 */
	public TreeNode findNode(String path){
		TreeNode result = null;
		
		if (null != path) {
			Iterator iter = getNodes().iterator();
			
			while (null == result && iter.hasNext()){
				TreeNode childi = (TreeNode)iter.next();
				
				if (path.startsWith(childi.getName() + childi.getSeparator())) {
					// The right way has been found
					result = childi.findNode(path);
					
				} else if (path.equals(childi.getName())) {
					// The node is found
					result = childi;
				}
			}
		} else {
			result = this;
		}
		return result;
	}
	
	/**
	 * The tree root can't be moved. this method doesn't perform any action 
	 */
	public void move(String newParentPath, String newName) throws TreeException {
	}
	
	public ModelTree getTreeRoot() {
		return this;
	}
}
