/*************************************************************************
 *
 * $RCSfile: OOo.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/06/09 06:13:59 $
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
package org.openoffice.ide.eclipse.core.internal.model;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 * Representing an OpenOffice.org instance for use in the UNO-IDL projects.
 * 
 * <p>An OpenOffice.org instance is recognized to the following files:
 * 	<ul>
 * 		<li><code>program/classes</code> directory</li>
 * 		<li><code>program/types.rdb</code> registry</li>
 * 		<li><code>program/bootstraprc</code> file</li>
 * 	</ul>
 * </p>
 * 
 * @author cbosdonnat
 *
 */
public class OOo extends AbstractOOo {
	
	/**
	 * private constant that holds the ooo name key in the bootstraprc file
	 */
	private static final String  K_PRODUCTKEY = "ProductKey";
	
	/**
	 * Creating a new OOo instance specifying its home directory
	 * 
	 * @param oooHome the OpenOffice.org home directory
	 * @throws InvalidConfigException is thrown if the home directory doesn't
	 * 		contains the required files and directories
	 */
	public OOo(String oooHome) throws InvalidConfigException {
		super(oooHome);
	}
	
	public OOo(String oooHome, String oooName) throws InvalidConfigException {
		super(oooHome, oooName);
	}

	//----------------------------------------------------- IOOo Implementation
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.IOOo#getClassesPath()
	 */
	public String getClassesPath(){
		return getHome() + "/program/classes";
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getLibsPath()
	 */
	public String getLibsPath() {
		return getHome() + "/program";
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getTypesPath()
	 */
	public String getTypesPath() {
		return getHome() + "/program/types.rdb";
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getServicesPath()
	 */
	public String getServicesPath() {
		return getHome() + "/program/services.rdb";
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getUnorcPath()
	 */
	public String getUnorcPath() {
		String path = getHome() + "/program/bootstrap";
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			path += ".ini";
		} else {
			path += "rc";
		}
		return path;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getUnoPath()
	 */
	public String getUnoPath() {
		return getHome() + "/program/uno";
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.internal.model.AbstractOOo#setName(java.lang.String)
	 */
	protected void setName(String aName) {
		
		String name = aName;
		if (name == null || name.equals("")) {
			name = getOOoName();
		}
		
		super.setName(name);
	}
	
	/**
	 * @return The OOo name as defined in Bootstraprc or <code>null</code>.
	 */
	private String getOOoName() {
		
		String oooname = null;
		
		Path unorcPath = new Path(getUnorcPath());
		File unorcFile = unorcPath.toFile();
		
		if (unorcFile.exists() && unorcFile.isFile()) {
		
			Properties bootstraprcProperties = new Properties();
			try {
				bootstraprcProperties.load(
						new FileInputStream(unorcFile));
				
				// Checks if the name and buildid properties are set
				if (bootstraprcProperties.containsKey(K_PRODUCTKEY)){
					
					// Sets the both value
					oooname = bootstraprcProperties.getProperty(K_PRODUCTKEY);
				}
				
			} catch (Exception e) {
				// Nothing to report
			}
		}
		
		return oooname;
	}
}
