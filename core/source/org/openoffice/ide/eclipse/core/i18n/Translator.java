/*************************************************************************
 *
 * $RCSfile: Translator.java,v $
 *
 * $Revision: 1.3 $
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
package org.openoffice.ide.eclipse.core.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.openoffice.ide.eclipse.core.PluginLogger;

/**
 * The translator is an object returning an internationalized text from a key
 * depending on the system localee. The keys are described in the 
 * {@link I18nConstants} class and the associated properties file is 
 * <code>Translator.properties</code>.
 * 
 * @author cbosdonnat
 *
 */
public class Translator {
	
	private ResourceBundle bundle;
	
	/**
	 * Default Constructor
	 */
	public Translator(){
		
		try {
			bundle = ResourceBundle.getBundle(
					"org.openoffice.ide.eclipse.core.i18n.Translator", 
					Locale.getDefault());
			
		} catch (NullPointerException e){
			PluginLogger.getInstance().debug(
					"Call to getBundle is incorrect: " +
					"NullPointerException catched");
			
		} catch (MissingResourceException e) {
			
			String message = "Translation file not found for locale :" + 
					Locale.getDefault().toString();
			PluginLogger.getInstance().error(message);
		}
	}
	
	/**
	 * <p>Returns the localized translation for the key given in parameter. This value
	 * is extracted from the <code>Translator.properties</code>
	 * file located in the plugin directory.</p>
	 * 
	 * @param key wanted localized translation key
	 * @return localized translation for key
	 */
	public String getString(String key){
		String result = "";

		try {
			result = bundle.getString(key);
		} catch (Exception e){
			PluginLogger.getInstance().error("Key not found : " + key, e);
			result = key;
		}
		
		return result;
	}
}
