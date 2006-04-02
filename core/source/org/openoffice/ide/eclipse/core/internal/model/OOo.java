/*************************************************************************
 *
 * $RCSfile: OOo.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/02 20:13:08 $
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.gui.ITableElement;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 * TODOC
 * @author cbosdonnat
 *
 */
public class OOo implements IOOo, ITableElement {
	
	public static final String NAME = "__ooo_name";
	
	public static final String PATH = "__ooo_path";

	/**
	 * private constant that holds the ooo name key in the bootstraprc file
	 */
	private static final String  K_PRODUCTKEY = "ProductKey";
	
	private String name;
	private String oooHome;
	
	public OOo(String oooHome) throws InvalidConfigException {
		super();
		setHome(oooHome);
	}

	//----------------------------------------------------- IOOo Implementation
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.IOOo#getName()
	 */
	public String getName(){
		return name;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.IOOo#getId()
	 */
	public String getId(){
		return name;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.IOOo#getHome()
	 */
	public String getHome(){
		return oooHome;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.IOOo#getClassesPath()
	 */
	public String getClassesPath(){
		return oooHome + "/program/classes";
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.IOOo#setHome(java.lang.String)
	 */
	public void setHome(String home) throws InvalidConfigException {
		
		try {
			// Get the file representing the given OOo home path
			Path homePath = new Path(home);
			File homeFile = homePath.toFile();
				
			// Check for the program directory
			File programFile = new File(homeFile, "program");
			if (programFile.exists() && programFile.isDirectory()){
				
				// checks for types.rdb
				File typesFile = new File(programFile, "types.rdb");
				if (! (typesFile.exists() && typesFile.isFile()) ){
					throw new InvalidConfigException(
							OOEclipsePlugin.getTranslationString(I18nConstants.NOT_EXISTING_FILE)+ typesFile.getAbsolutePath(), 
							InvalidConfigException.INVALID_OOO_HOME);
				}
				
				// checks for classes directory
				File classesFile = new File (programFile, "classes");
				if (! (classesFile.exists() && classesFile.isDirectory()) ){
					throw new InvalidConfigException(
							OOEclipsePlugin.getTranslationString(I18nConstants.NOT_EXISTING_DIR) + classesFile.getAbsolutePath(), 
							InvalidConfigException.INVALID_OOO_HOME);
				}
				
				this.oooHome = home;
				readSettings(programFile);
				
			} else {
				throw new InvalidConfigException(
						OOEclipsePlugin.getTranslationString(I18nConstants.NOT_EXISTING_DIR) + programFile.getAbsolutePath(), 
						InvalidConfigException.INVALID_OOO_HOME);
			}
				
			
			
		} catch (Throwable e){
			
			if (e instanceof InvalidConfigException) {
				
				// Rethrow the invalidSDKException
				InvalidConfigException exception = (InvalidConfigException)e;
				throw exception;
			} else {

				// Unexpected exception thrown
				throw new InvalidConfigException(
						OOEclipsePlugin.getTranslationString(I18nConstants.UNEXPECTED_EXCEPTION),
						InvalidConfigException.INVALID_OOO_HOME, e);
			}
		}
	}
	
	/**
	 * Reads the bootstraprc file to get the ooo name and buildid. They are set 
	 * in the OOo object if they are both fetched. Otherwise an invalid config 
	 * exception is thrown.
	 * 
	 * @param programFile
	 * @throws InvalidConfigException Exception thrown when one of the following problems happened
	 *          <ul>
	 *             <li>the given program file isn't a valid directory</li>
	 *             <li>the program/bootstraprc file doesn't exists or is unreadable</li>
	 *             <li>the product key is not set</li>
	 *          </ul>
	 */
	private void readSettings(File programFile) throws InvalidConfigException {
		
		if (programFile.exists() && programFile.isDirectory()) {
		
			// Get the bootstrap configuration file
			String bootstrapName = "bootstraprc";
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")){
				bootstrapName = "bootstrap.ini";
			}
			
			File bootstraprcFile = new File(programFile, bootstrapName);
			
			Properties bootstraprcProperties = new Properties();
			try {
				bootstraprcProperties.load(new FileInputStream(bootstraprcFile));
				
				// Checks if the name and buildid properties are set
				if (bootstraprcProperties.containsKey(K_PRODUCTKEY)){
					
					// Sets the both value
					name = bootstraprcProperties.getProperty(K_PRODUCTKEY);
				} else {
					throw new InvalidConfigException(
							OOEclipsePlugin.getTranslationString(I18nConstants.KEYS_NOT_SET) + 
									K_PRODUCTKEY,
							InvalidConfigException.INVALID_SDK_HOME);
				}
				
			} catch (FileNotFoundException e) {
				throw new InvalidConfigException(
						OOEclipsePlugin.getTranslationString(I18nConstants.NOT_EXISTING_FILE)+
								"program/" + bootstrapName , 
						InvalidConfigException.INVALID_OOO_HOME);
			} catch (IOException e) {
				throw new InvalidConfigException(
						OOEclipsePlugin.getTranslationString(I18nConstants.NOT_READABLE_FILE) + 
								"program/" + bootstrapName, 
						InvalidConfigException.INVALID_OOO_HOME);
			}
			
		} else {
			throw new InvalidConfigException(
					OOEclipsePlugin.getTranslationString(I18nConstants.NOT_EXISTING_DIR)+ programFile.getAbsolutePath(),
					InvalidConfigException.INVALID_OOO_HOME);
		}
	}

	//-------------------------------------------- ITableElement Implementation
	
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.ITableElement#getImage(java.lang.String)
	 */
	public Image getImage(String property) {
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.ITableElement#getLabel(java.lang.String)
	 */
	public String getLabel(String property) {
		String result = "";
		if (property.equals(NAME)) {
			result = getName();
		} else if (property.equals(PATH)) {
			result = getHome();
		}
		return result;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.ITableElement#getProperties()
	 */
	public String[] getProperties() {
		return new String[] {NAME, PATH};
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.ITableElement#canModify(java.lang.String)
	 */
	public boolean canModify(String property) {
		return false;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.ITableElement#getValue(java.lang.String)
	 */
	public Object getValue(String property) {
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.ITableElement#setValue(java.lang.String, java.lang.Object)
	 */
	public void setValue(String property, Object value) {
		// Nothing to do
	}
}
