/*************************************************************************
 *
 * $RCSfile: InterfaceService.java,v $
 *
 * $Revision: 1.2 $
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

public class InterfaceService extends SingleFileDeclaration 
							  implements IPublishable {
	
	public InterfaceService(TreeNode node, String aName, UnoidlFile file, 
							ScopedName interfaceName) {
		super(node, aName, file,T_SERVICE);
		
		setInterfaceName(interfaceName);
	}

	//------------------------------------------------------TreeNode overriding
	
	public String computeBeforeString(TreeNode callingNode) {
		
		String output = "";
		
		if (isPublished()){
			output = output + "published ";
		}
		
		output = output + "service " + getScopedName().lastSegment() + " : ";
		
		if (null != getInterfaceName()){
			output = output + computeInterfaceNameString();
		}
	
		output = output + " {\n";
		
		return indentLine(output);
	}
	
	public String computeAfterString(TreeNode callingNode) {
		return indentLine("};\n");
	}
	
	//-------------------------------------------------- Declaration overriding
	
	public int[] getValidTypes() {
		return new int[] {T_CONSTRUCTOR};
	}
	
	//-------------------------------------------------------- Member managment
	
	private ScopedName interfaceName;
	
	private boolean published = false;
	
	public ScopedName getInterfaceName(){
		return interfaceName;
	}
	
	/**
	 * Returns the relative scoped name to the interface if possible. Otherwise
	 * the full scoped name is returned.  
	 * 
	 * @return String conversion of the interface scoped name.
	 */
	private String computeInterfaceNameString(){
		
		// By default the whole scoped name is returned
		String result = getInterfaceName().toString();
		
		if (null != getScopedName() && null != getInterfaceName()){
			// Check if the interface scoped name
			// begins with the service scoped name 
			
			String[] segments = getScopedName().getSegments();
			String[] interfaceSegments = getInterfaceName().getSegments();
			
			boolean matches = true;
			int i = 0;
			
			while (matches && i < Math.min(segments.length-1,
										   interfaceSegments.length-1)){
				
				if (!segments[i].equals(interfaceSegments[i])){
					matches = false;
				} else {
					i++;
				}
			}
			
			if (i == segments.length-1 && matches){
				result = interfaceSegments[i];
				
				for (i++; i<interfaceSegments.length; i++) {
					result = result + ScopedName.SEPARATOR + interfaceSegments[i];
				}
			}
		}
		
		
		return result;
	}
	
	public void setInterfaceName(ScopedName aInterfaceName){
		interfaceName = aInterfaceName;
	}
	
	public boolean isPublished(){
		return published;
	}
	
	public void setPublished(boolean published){
		this.published = published;
	}
}
