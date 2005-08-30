/*************************************************************************
 *
 * $RCSfile: InternalUnoType.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/30 13:24:40 $
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

package org.openoffice.ide.eclipse.unotypebrowser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class InternalUnoType {

	private String path;
	private int type;
	private boolean local = false;
	
	public InternalUnoType(String typeString) {
		if (null != typeString) {
			Matcher typeMatcher = Pattern.compile(
					"("+UnoTypesGetter.UNO_TAG  +"|" + UnoTypesGetter.LOCAL_TAG +
					") ([^\\s]*) (.*)").matcher(typeString);
			if (typeMatcher.matches() && 3 == typeMatcher.groupCount()){
				setLocal(typeMatcher.group(1));
				setType(typeMatcher.group(2));
				path = typeMatcher.group(3);
			}
		}
	}
	
	public String getName() {
		String name = "";
		
		String[] splittedPath = path.split("\\.");
		if (splittedPath.length > 0) {
			name = splittedPath[splittedPath.length - 1];
		}
		return name;
	}
	
	public String getPath(){
		return path;
	}
	
	public int getType(){
		return type;
	}
	
	public boolean isLocalType(){
		return local;
	}
	
	private void setType(String aType) {
		type = UnoTypeProvider.convertTypeToInt(aType);
	}
	
	private void setLocal(String tag){
		if (tag.equals(UnoTypesGetter.LOCAL_TAG)) {
			local = true;
		} else {
			local = false;
		}
	}
}
