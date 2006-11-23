/*************************************************************************
 *
 * $RCSfile: OOo.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/23 18:27:16 $
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
	private static final String  K_PRODUCTKEY = "ProductKey"; //$NON-NLS-1$
	
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
		String sep = System.getProperty("file.separator"); //$NON-NLS-1$
		return getHome() + sep + "program" + sep + "classes"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getLibsPath()
	 */
	public String getLibsPath() {
		String sep = System.getProperty("file.separator"); //$NON-NLS-1$
		return getHome() + sep + "program"; //$NON-NLS-1$
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getTypesPath()
	 */
	public String getTypesPath() {
		String sep = System.getProperty("file.separator"); //$NON-NLS-1$
		return getHome() + sep + "program" + sep +"types.rdb"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getServicesPath()
	 */
	public String getServicesPath() {
		String sep = System.getProperty("file.separator"); //$NON-NLS-1$
		return getHome() + sep + "program" + sep + "services.rdb"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getUnorcPath()
	 */
	public String getUnorcPath() {
		String sep = System.getProperty("file.separator"); //$NON-NLS-1$
		String path = getHome() + sep + "program" + sep + "bootstrap"; //$NON-NLS-1$ //$NON-NLS-2$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			path += ".ini"; //$NON-NLS-1$
		} else {
			path += "rc"; //$NON-NLS-1$
		}
		return path;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getUnoPath()
	 */
	public String getUnoPath() {
		String sep = System.getProperty("file.separator"); //$NON-NLS-1$
		String uno = "uno.bin"; //$NON-NLS-1$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			uno = "uno.exe"; //$NON-NLS-1$
		}
		return getHome() + sep + "program" + sep + uno; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.internal.model.AbstractOOo#setName(java.lang.String)
	 */
	protected void setName(String aName) {
		
		String name = aName;
		if (name == null || name.equals("")) { //$NON-NLS-1$
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
	
	public String toString() {
		return "OOo " + getName(); //$NON-NLS-1$
	}
	
	public String createUnoCommand(String implementationName, String libLocation,
			String[] registriesPaths, String[] args) {
		
		String command = ""; //$NON-NLS-1$
		
		if (libLocation != null && !libLocation.equals("")) { //$NON-NLS-1$
			// Put the args into one string
			String sArgs = ""; //$NON-NLS-1$
			for (int i=0; i<args.length; i++) {
				sArgs += args[i];

				if (i < args.length -1) {
					sArgs += " "; //$NON-NLS-1$
				}
			}

			// Defines OS specific constants
			String pathSeparator = System.getProperty("path.separator"); //$NON-NLS-1$
			String fileSeparator = System.getProperty("file.separator"); //$NON-NLS-1$

			// Constitute the classpath for OOo Boostrapping
			String classpath = "-cp "; //$NON-NLS-1$
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				classpath += "\""; //$NON-NLS-1$
			}

			String oooClassesPath = getClassesPath();
			File oooClasses = new File(oooClassesPath);
			String[] content = oooClasses.list();

			for (int i=0, length=content.length; i<length; i++){
				String contenti = content[i];
				if (contenti.endsWith(".jar")) { //$NON-NLS-1$
					classpath += oooClassesPath + 
						fileSeparator + contenti + pathSeparator;
				}
			}

			classpath += libLocation;
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				classpath += "\""; //$NON-NLS-1$
			}

			command = "java " + classpath + " " + //$NON-NLS-1$ //$NON-NLS-2$
				implementationName + " " + sArgs; //$NON-NLS-1$
		}
			
		return command;
	}
	
	public String getJavaldxPath() {
		String javaldx = null;
		
		if (Platform.getOS().equals(Platform.OS_LINUX)) {
			javaldx = getHome() + "/program/javaldx"; //$NON-NLS-1$
		}
		return  javaldx; 
	}
}
