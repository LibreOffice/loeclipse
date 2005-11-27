/*************************************************************************
 *
 * $RCSfile: Attribute.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/11/27 17:48:13 $
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

import java.util.Vector;

/**
 * Classe representant les attributs d'interfaces tels que definis dans la 
 * grammaire de UNO-IDL pour OpenOffice.org 2.0
 * 
 * @author cbosdonnat
 *
 */
public class Attribute extends TypedDeclaration {
	
	public final static String F_BOUND = "bound";
	
	public final static String F_READONLY = "readonly";
	
	public static final String[] FLAGS = {
		F_BOUND,
		F_READONLY
	};

	public Attribute(TreeNode node, String aName, UnoidlFile file,
			String aDeclarationType) {
		
		super(node, aName, file, T_ATTRIBUTE, aDeclarationType, false, false);
	}

	//------------------------------------------------------ TreeNode overriding
	
	public int[] getValidTypes() {
		return new int[] {T_ATTRIBUTE_ACCESS};
	}
	
	public String computeBeforeString(TreeNode callingNode) {
		
		String result = "[attribute";
		
		for (int i=0, length= flags.size(); i<length; i++) {
			result = result + ", " + (String)flags.get(i);
		}
			
		result = result + "] " + getDeclarationType() + " " + getName() + " {\n";
		
		return indentLine(result);
	}
	
	public String computeAfterString(TreeNode callingNode) {
		return indentLine("};\n");
	}
	
	//-------------------------------------------------------- Member management
	
	private Vector flags = new Vector();
	
	public void addFlag(String aFlag){
		if (!flags.contains(aFlag) && isFlag(aFlag)) {
			flags.add(aFlag);
		}
	}
	
	public void removeFlag(String aFlag){
		flags.remove(aFlag);
	}
	
	public Vector getFlags(){
		return flags;
	}
	
	public static boolean isFlag(String aFlag){
		boolean result = false;
		
		int i = 0;
		
		while (i < FLAGS.length && !result){
			if (aFlag.equals(FLAGS[i])) {
				result = true;
			} else {
				i++;
			}
		}
		return result;
	}
}
