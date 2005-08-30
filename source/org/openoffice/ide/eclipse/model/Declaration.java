/*************************************************************************
 *
 * $RCSfile: Declaration.java,v $
 *
 * $Revision: 1.2 $
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

public abstract class Declaration extends TreeNode {
	
	/**
	 * initialized at -1 to avoid first line indentation
	 */
	public static int indentLevel = -1;

	public final static String SEPARATOR = "::";
	
	//------------------------------------------------------- Declaration types
	
	public final static int T_MODULE = 0;
	
	public final static int T_SERVICE = 1;
	
	public final static int T_INTERFACE = 2;
	
	public final static int T_CONSTRUCTOR = 100;
	
	public final static int T_SERVICE_INHERITANCE = 101;
	
	public final static int T_INTERFACE_INHERITANCE = 102;
	
	public final static int T_PROPERTY = 103;
	
	//------------------------------------------------------------ Constructors
	
	public Declaration(TreeNode node, String aName, int aType) {
		super(node, aName);
		
		setType(aType);
		setScopedName(aName);
	}
	
	public String getSeparator() {
		return SEPARATOR;
	}

	//------------------------------------------------------- Members managment
	
	private int type;
	
	private ScopedName scopedName;
	
	public ScopedName getScopedName(){
		
		return scopedName;
	}

	public int getType() {
		return type;
	}

	private void setScopedName(String aName){
		
		String baseName = "";
		if (getParent() instanceof Declaration){
			Declaration declaration = (Declaration)getParent();
			baseName = declaration.getScopedName().toString();
		}
		
		scopedName = new ScopedName(baseName, aName);
		setName(scopedName.lastSegment());
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	//----------------------------------------- Node declaration file managment
	
	private Vector declarationFiles = new Vector();
	
	public void addFile(UnoidlFile file){
		if (!containsFile(file)){
			declarationFiles.add(file);
		}
	}
	
	public void removeFile(UnoidlFile file){
		declarationFiles.remove(file);
	}
	
	public void removeAllFiles(){
		declarationFiles.removeAllElements();
	}
	
	public boolean containsFile(UnoidlFile file){
		return declarationFiles.contains(file);
	}
	
	public Vector getFiles() {
		return declarationFiles;
	}
	
	//--------------------------------------------------- Node reimplementation
	
	/**
	 * Returns the valid types of the children. This method should be 
	 * reimplemented by each subclass.
	 */
	public int[] getValidTypes(){
		return new int[] {T_MODULE, T_SERVICE, T_INTERFACE};
	}
	
	
	/**
	 * A node is valid for a declaration if it is a declaration and one of the 
	 * valid types declared in the parent. Subclasses do not intend to override
	 * this method. Simply override the VALID_TYPES field will be enougth
	 * to restrict the types of the children
	 */
	protected boolean isValidNode(TreeNode node) {

		boolean isValid = false;
		
		if (node instanceof Declaration){
		
			int nodeType = ((Declaration)node).getType();
			int i = 0;
			
			while (i<getValidTypes().length && !isValid){
				if (getValidTypes()[i] == nodeType){
					isValid = true;
				} else {
					i++;
				}
			}
		}			
		return isValid;
	}
	
	public String computeBeforeString(TreeNode callingNode) {
		String output = "";
		
		if (callingNode instanceof UnoidlFile){
			
			UnoidlFile file = (UnoidlFile)callingNode;
			if (containsFile(file)){
				output = output + computeBeforeString(file);
			}
		}
		return output;
	}
	
	public String toString(TreeNode callingNode) {
		
		String output = "";
		if (callingNode instanceof UnoidlFile){
			
			UnoidlFile file = (UnoidlFile)callingNode;
			if (containsFile(file)){
				indentLevel++;
				output = super.toString(file);
				indentLevel--;
			}
		}
		
		return output;
	}
	
	public String computeAfterString(TreeNode callingNode) {
		String output = "";
		
		if (callingNode instanceof UnoidlFile){
			
			UnoidlFile file = (UnoidlFile)callingNode;
			if (containsFile(file)){
				output = computeAfterString(file);
			}
		}
		return output;
	}
	
	protected String indentLine(String line){
		String output = "";
		
		for (int i=0; i<indentLevel; i++){
			output = output + "\t";
		}
		return output + line;
	}
}
