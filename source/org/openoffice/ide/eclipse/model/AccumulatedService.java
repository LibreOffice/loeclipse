/*************************************************************************
 *
 * $RCSfile: AccumulatedService.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/30 13:24:29 $
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

import java.util.Vector;

public class AccumulatedService extends SingleFileDeclaration {
	
	public final static String SEPARATOR = "$";
	
	public AccumulatedService(TreeNode node, String aName, UnoidlFile file) {
		this(node, aName, file, new Vector());
	}
	
	public AccumulatedService(TreeNode node, String aName, 
			UnoidlFile file, Vector members) {
		
		super(node, aName, file, T_SERVICE);
		
		// The Declaration.addNode() method will add only the valid members 
		for (int i=0, length=members.size(); i<length; i++) {
			addNode((Declaration) members.get(i));
		}
	}

	//------------------------------------------------------TreeNode overriding
	
	public String computeBeforeString(TreeNode callingNode) {
		
		String output = "";
		
		if (isPublished()){
			output = output + "[published] ";
		}
		
		output = output + "service " + getScopedName().lastSegment() + "{\n";
	
		return indentLine(output);
	}
	
	
	public String computeAfterString(TreeNode callingNode) {
		return indentLine("};\n");
	}
	
	public String getSeparator() {
		return SEPARATOR;
	}
	
	//-------------------------------------------------- Declaration overriding
	
	public int[] getValidTypes() {
		return new int[] {T_SERVICE_INHERITANCE,
					      T_INTERFACE_INHERITANCE, 
					      T_PROPERTY
		};
	}
	
	//-------------------------------------------------------- Member managment
	
	private boolean published = false;
	
	public boolean isPublished(){
		return published;
	}
	
	public void setPublished(boolean published){
		this.published = published;
	}
}
