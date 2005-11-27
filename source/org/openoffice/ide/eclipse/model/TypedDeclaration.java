/*************************************************************************
 *
 * $RCSfile: TypedDeclaration.java,v $
 *
 * $Revision: 1.2 $
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

/**
 * <p>This class is provided only to avoid duplicate code for all the elements
 * that are typed. This class is not usable as is. It have to be subclassed and
 * the following methods should be overridden:</p>
 * <ul>
 *   <li>computeBeforeString() to print the whole string</li>
 * </ul>
 * 
 * @author cbosdonnat
 *
 */
public class TypedDeclaration extends SingleFileDeclaration {
	
	public final static String[] BASIC_TYPES = {
		"void",
		"boolean",
		"byte",
		"short",
		"unsigned short",
		"long",
		"unsigned long",
		"hyper",
		"unsigned hyper",
		"float",
		"double",
		"char",
		"string",
		"type",
		"any"
	};

	public TypedDeclaration(TreeNode node, String aName, UnoidlFile file, 
			int aType, String aDeclarationType, boolean canBeVoid, 
			boolean canBeRest) {
		
		super(node, aName, file, aType);
		setDeclarationType(aDeclarationType);
		
		this.canBeVoid = canBeVoid;
		this.canBeRest = canBeRest;
	}
	
	//-------------------------------------------------- Declaration overriding
	
	public int[] getValidTypes() {
		return new int[]{};
	}
	
	public String computeAfterString(TreeNode callingNode) {
		return "";
	}
	
	//-------------------------------------------------------- Member managment

	private Object declarationType;
	
	private boolean canBeVoid = true;
	
	private boolean canBeRest = true;
	
	public Object getDeclarationType(){
		return declarationType;
	}
	
	public boolean isBasicDeclarationType(){
		return declarationType instanceof String;
	}
	
	public void setDeclarationType(String aDeclarationType){
		if (isBasicType(aDeclarationType)){
			declarationType = aDeclarationType;
		} else {
			declarationType = new ScopedName(aDeclarationType);
		}
	}
	
	private boolean isBasicType(String aType) {
		boolean result = false;
		
		if (aType.matches("any[\\t ]*\\.\\.\\.") && canBeRest) {
			result = true;
		} else {
			int i=0;
			while (i < BASIC_TYPES.length && !result){
				if (BASIC_TYPES[i].equals(aType) && canBeVoid){
					result = true;
				} else {
					i++;
				}
			}
		}
		
		return result;
	}
}
