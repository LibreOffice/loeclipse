/*************************************************************************
 *
 * $RCSfile: Interface.java,v $
 *
 * $Revision: 1.1 $
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

/**
 * Class that represents the Interface declaration as specified in the UNO-IDL
 * grammar defined in the OpenOffice.org 2.0 SDK.
 * 
 * @author cbosdonnat
 *
 */
public class Interface extends SingleFileDeclaration
                       implements IPublishable {

	public Interface(TreeNode node, String aName, UnoidlFile file) {
		super(node, aName, file, T_INTERFACE);
	}

	//----------------------------------------------------- Tree Node overriding
	
	public String computeBeforeString(TreeNode callingNode) {
		
		String result = "interface " + getName();
		if (isPublished()) {
			result = "published " + result;
		}

		if (!isForward()) {
			result = result + "{\n";
		} else {
			result = result + ";\n";
		}
		
		
		return indentLine(result);
	}
	
	public String computeAfterString(TreeNode callingNode) {
		String result = "";
		if (!isForward()) {
			result = "};\n";
		}
		
		return indentLine(result);
	}
	
	public String toString(TreeNode callingNode) {
		String result = "";
		
		if (isForward()) {
			result = computeBeforeString(callingNode);
		} else {
			result = super.toString(callingNode);
		}
		return result;
	}
	
	//--------------------------------------------------- Declaration overriding
	
	public int[] getValidTypes() {
		return new int[] {T_ATTRIBUTE, T_INTERFACE_INHERITANCE, T_METHOD}; 
	}
	
	//-------------------------------------------------------- Members managment
	
	private boolean forward = false;
	
	private boolean published = false;
	
	/**
	 * Sets if the interface declaration is a forward one or not. If the interface
	 * already has children and is set as forward, it's attributes are kept.
	 * They aren't printed in the declaration, but the attributes
	 * will not be lost.
	 * 
	 * @param forward <code>true</code> sets the interface as forward, <code>false
	 * 				</code> unset the forward flag of the interface.
	 */
	public void setForward(boolean forward) {
		this.forward = forward;
	}
	
	/**
	 * Return whether the inteface is a forward one or not.
	 * 
	 * @return <code>true</code> if the interface is forward, <code>false</code>
	 * 				otherwise.
	 */
	public boolean isForward() {
		return forward;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IPublishable#setPublished(boolean)
	 */
	public void setPublished(boolean published) {
		this.published = published;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IPublishable#isPublished()
	 */
	public boolean isPublished() {
		return published;
	}
}
