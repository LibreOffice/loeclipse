/*************************************************************************
 *
 * $RCSfile: Include.java,v $
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

/**
 * This class represents an include line in an idl file. It has a name
 * and could be either local to the project or in a library (ie: in a
 * file pointed by the include path in idlc)
 * 
 * @author cbosdonnat
 *
 */
public class Include {

	private String name;
	
	private boolean library = false;
	
	public Include(String aName, boolean aLibrary) {
		setName(aName);
		setLibrary(aLibrary);
	}
	
	public static Include createInclude(String type, boolean isLibrary){
		// Local types are handled as libraries : as one type is one file
		
		String file = type.replace("::", "/");
		file = file + ".idl";
		
		boolean library = isLibrary;
		
		return new Include(file, library);
	}
	
	public String getName(){
		return name;
	}
	
	public boolean isLibrary(){
		return library;
	}
	
	public void setName(String aName){
		name = aName;
	}
	
	public void setLibrary(boolean aLibrary){
		library = aLibrary;
	}

	/**
	 * Two includes are equals if they contains the same data, not only if they
	 * have the same reference.
	 */
	public boolean equals(Object arg0) {
		boolean equals = false;
		
		if (arg0 instanceof Include){
			Include other = (Include)arg0;
			
			if (getName().equals(other.getName()) && 
			 	isLibrary() == other.isLibrary()) {
				
				equals = true;
			}
		}
		return equals;
	}
	
	public String toString() {
		String openToken = "<";
		String closeToken = ">";
		
		if (!isLibrary()){
			openToken = "\"";
			closeToken = "\"";
		}
		
		return "#include " + openToken + getName() + closeToken + "\n";
	}
	
}
