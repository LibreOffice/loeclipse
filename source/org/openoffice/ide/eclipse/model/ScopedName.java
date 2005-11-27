/*************************************************************************
 *
 * $RCSfile: ScopedName.java,v $
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

import java.util.Vector;

/**
 * Class that holds the scoped name of a uno declaration
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class ScopedName {

	public final static String SEPARATOR = "::";
	
	private Vector segments = new Vector();
	
	public ScopedName(String name) {
		changeScopedName(name);
	}
	
	public ScopedName(String root, String name) {
		if (!root.equals("")){
			name = root + SEPARATOR + name;
		}
		changeScopedName(name);
	}
	
	public void changeScopedName(String name){
		
		String[] splittedName = name.split("::");
		segments.clear();
		
		for (int i=0, length=splittedName.length; i<length; i++){
			segments.add(splittedName[i]);
		}
	}
	
	public String[] getSegments(){
		
		String[] aSegments = new String[segments.size()];
		for (int i=0, length=aSegments.length; i<length; i++){
			aSegments[i] = (String)segments.get(i);
		}
		
		return aSegments;
	}
	
	public void appendSegment(String name){
		String[] splittedName = name.split("::");
		
		for (int i=0, length=splittedName.length; i<length; i++){
			segments.add(splittedName[i]);
		}
	}
	
	public String lastSegment(){
		return (String)segments.lastElement();
	}
	
	public String toString() {
		String result = "";
		
		for (int i=0, length=segments.size()-1; i<length; i++){
			result = result + (String)segments.get(i) + "::";
		}
		
		result = result + (String)segments.get(segments.size() - 1);
		
		return result;
	}

}
