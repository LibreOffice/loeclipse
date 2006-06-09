/*************************************************************************
 *
 * $RCSfile: SDK.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/06/09 06:13:58 $
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
import org.openoffice.ide.eclipse.core.preferences.ISdk;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 * Class representing a SDK instance used in the UNO-IDL projects
 * 
 * @author cbosdonnat
 *
 */
public class SDK implements ISdk, ITableElement {
	
	public static final String NAME = "__sdk_name";
	
	public static final String PATH = "__sdk_path";
	
	/**
	 * private constant that holds the sdk build id key in the dk.mk file
	 */
	private static final String K_SDK_BUILDID = "BUILDID";
	
	/**
	 * private constant that hold the name of the sdk config file (normaly dk.mk)
	 * This is set to easily change if there are future sdk organization changes
	 */
	private static final String F_DK_CONFIG = "dk.mk";

	
	
	/* SDK Members */
	
	private String buildId;
	private String sdkHome;
	
	/**
	 * Standard and only constructor for the SDK object. The name and buildId will be fetched
	 * from the $(SDK_HOME)/settings/dk.mk properties file.
	 * 
	 * @param sdkHome absolute path of the SDK root
	 */
	public SDK (String sdkHome) throws InvalidConfigException {
		
		// Sets the path to the SDK
		setHome(sdkHome);
	}

	//----------------------------------------------------- ISdk Implementation
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.ISdk#setHome(java.lang.String)
	 */
	public void setHome(String home) throws InvalidConfigException {
		try {
		
			// Get the file representing the given sdkHome
			Path homePath = new Path(home);
			File homeFile = homePath.toFile();
			
			// First check the existence of this directory
			if (homeFile.exists() && homeFile.isDirectory()){
				
				/**
				 * <p>If the provided sdk home does not contains <li><code>idl</code></li>
				 * <li><code>settings</code></li> directories, the provided sdk is considered as invalid</p>
				 */
				
				// test for the idl directory
				File idlFile = new File(homeFile, "idl");
				if (! (idlFile.exists() && idlFile.isDirectory()) ) {
					throw new InvalidConfigException(
							OOEclipsePlugin.getTranslationString(I18nConstants.IDL_DIR_MISSING), 
							InvalidConfigException.INVALID_SDK_HOME);
				}
				
				// test for the settings directory
				File settingsFile = new File(homeFile, "settings");
				if (! (settingsFile.exists() && settingsFile.isDirectory()) ) {
					throw new InvalidConfigException(
							OOEclipsePlugin.getTranslationString(I18nConstants.SETTINGS_DIR_MISSING),
							InvalidConfigException.INVALID_SDK_HOME);
				}
				
				
				// If the settings and idl directory both exists, then try to fetch the name and buildId from
				// the settings/dk.mk properties file
				readSettings(settingsFile);
				this.sdkHome = home;
				
			} else {
				throw new InvalidConfigException(
						OOEclipsePlugin.getTranslationString(I18nConstants.NOT_EXISTING_DIR)+home,
						InvalidConfigException.INVALID_SDK_HOME);
			}
		} catch (Throwable e) {
			
			if (e instanceof InvalidConfigException) {
				
				// Rethrow the InvalidSDKException
				InvalidConfigException exception = (InvalidConfigException)e;
				throw exception;
			} else {
				
				// Unexpected exception thrown
				throw new InvalidConfigException(
						OOEclipsePlugin.getTranslationString(I18nConstants.UNEXPECTED_EXCEPTION), 
						InvalidConfigException.INVALID_SDK_HOME, e);
			}
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.ISdk#getId()
	 */
	public String getId(){
		String result = null;
		
		String[] splits = buildId.split("\\(.*\\)");
		if (splits.length > 0){
			result = splits[0];
		}
		
		return result;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.ISdk#getHome()
	 */
	public String getHome(){
		return sdkHome;
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
			result = getId();
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
	
	/**
	 * Reads the dk.mk file to get the sdk name and buildid. They are set in the SDK object if 
	 * they are both fetched. Otherwise an invalide sdk exception is thrown.
	 * 
	 * @param settingsFile
	 * @throws InvalidConfigException Exception thrown when one of the following problems happened
	 *          <ul>
	 *             <li>the given settings file isn't a valid directory</li>
	 *             <li>the settings/dk.mk file doesn't exists or is unreadable</li>
	 *             <li>one or both of the sdk name or buildid key is not set</li>
	 *          </ul>
	 */
	private void readSettings(File settingsFile) throws InvalidConfigException {
		
		if (settingsFile.exists() && settingsFile.isDirectory()) {
		
			// Get the dk.mk file
			File dkFile = new File(settingsFile, F_DK_CONFIG);
			
			Properties dkProperties = new Properties();
			try {
				dkProperties.load(new FileInputStream(dkFile));
				
				// Checks if the name and buildid properties are set
				if (dkProperties.containsKey(K_SDK_BUILDID)){
					
					buildId = dkProperties.getProperty(K_SDK_BUILDID);
				} else if (dkProperties.containsKey(K_SDK_BUILDID)){
					
					buildId = dkProperties.getProperty(K_SDK_BUILDID);
				} else {
					throw new InvalidConfigException(
							OOEclipsePlugin.getTranslationString(I18nConstants.KEYS_NOT_SET) + K_SDK_BUILDID,
							InvalidConfigException.INVALID_SDK_HOME);
				}
				
			} catch (FileNotFoundException e) {
				throw new InvalidConfigException(
						OOEclipsePlugin.getTranslationString(I18nConstants.NOT_EXISTING_FILE)+ "settings/" + F_DK_CONFIG , 
						InvalidConfigException.INVALID_SDK_HOME);
			} catch (IOException e) {
				throw new InvalidConfigException(
						OOEclipsePlugin.getTranslationString(I18nConstants.NOT_READABLE_FILE) + "settings/" + F_DK_CONFIG, 
						InvalidConfigException.INVALID_SDK_HOME);
			}
			
		} else {
			throw new InvalidConfigException(
					OOEclipsePlugin.getTranslationString(I18nConstants.NOT_EXISTING_DIR)+ settingsFile.getAbsolutePath(),
					InvalidConfigException.INVALID_SDK_HOME);
		}
	}
}
