/*************************************************************************
 *
 * $RCSfile: SDK.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/07/18 19:36:02 $
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
package org.openoffice.ide.eclipse.preferences.sdk;

/**
 * SDK Class used as model for a line of the table. It's properties are:
 * <ul>
 *   <li><b>name</b>: uncontrolled name of the SDK</li>
 *   <li><b>version</b>: uncontrolled version number of the OOo suite associated with this SDK</li>
 *   <li><b>path</b>: system absolute path where the SDK root is located</li>
 *   <li><b>oooProgramPath</b>: system absolute path to the $OOO_HOME/program directory</li>
 * </ul>
 * This class is not indended to be used outside the SDKTable object
 * 
 * @author cbosdonnat
 *
 */
public class SDK {
	
	public String name;
	public String version;
	public String path;
	public String oooProgramPath;
	
	/**
	 * Standard and only constructor for the SDK object. Properties may be null except the name.
	 * If the name is null, it will be replaced by "SDK"
	 * 
	 * @param name arbitrary name of the SDK
	 * @param version version of the OOo suite
	 * @param path absolute path of the SDK root
	 * @param oooPath absolute path to the $OOO_HOME/program directory
	 */
	public SDK (String name, String version, String path, String oooPath){
		this.name = name;
		this.version = version;
		this.path = path;
		oooProgramPath = oooPath;
	}
}
