/*************************************************************************
 *
 * $RCSfile: OOoContainer.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/10 12:07:27 $
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
package org.openoffice.ide.eclipse.preferences.ooo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.preferences.ConfigListener;
import org.openoffice.ide.eclipse.preferences.InvalidConfigException;

/**
 * Singleton object containing the OOo configurations.
 * 
 * @author cbosdonnat
 *
 */
public class OOoContainer { 
	
	private static OOoContainer container;
	
	/**
	 * Vector of the config container listeners
	 */
	private Vector listeners;

	/**
	 * HashMap containing the ooo lines referenced by their name
	 */
	private HashMap elements;
	
	
	/* Methods to manage the listeners */
	
	/**
	 * Add a config listener to the container
	 * 
	 *  @param listener config listener to add 
	 */
	public void addListener(ConfigListener listener){
		if (null != listener){
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes a config listener from the container
	 * 
	 * @param listener config listener to remove
	 */
	public void removeListener(ConfigListener listener){
		if (null != listener){
			listeners.remove(listener);
		}
	}
	
	/* Methods to manage the ooos */
	
	/**
	 * Returns the ooos elements in an array
	 * @return
	 */
	public Object[] toArray(){
		return toVector().toArray(); 
	}
	
	/**
	 * Add the OOo given in parameter to the list of the others. Do not use directly 
	 * the private field to handle OOos
	 * 
	 * @param ooo OOo to add
	 */
	public void addOOo(OOo ooo){
		
		/** 
		 * If there already is a OOo with such an identifier, replace the values,
		 * not the object to keep the references on it
		 */ 
		
		if (null != ooo){
			if (!elements.containsKey(ooo.getId())){
				elements.put(ooo.getId(), ooo);
				fireOOoAdded(ooo);
			} else {
				OOo oooref = (OOo)elements.get(ooo.getId());
				updateOOo(oooref.getId(), ooo);
			}
		}
	}
	
	private void fireOOoAdded(OOo ooo) {
		for (int i=0, length=listeners.size(); i<length; i++){
			ConfigListener listeneri = (ConfigListener)listeners.get(i);
			listeneri.ConfigAdded(ooo);
		}
	}

	/**
	 * remove the given OOo from the list. Do not use directly the private field to 
	 * handle OOos
	 *  
	 * @param ooo OOo to remove
	 */
	public void delOOo(OOo ooo){
		if (null != ooo){
			if (elements.containsKey(ooo.getId())){
				elements.remove(ooo.getId());
				fireOOoRemoved(ooo);
			}
		}
	}
	
	/**
	 * Removes all the OOo contained
	 *
	 */
	public void clear(){
		elements.clear();
		fireOOoRemoved(null);
	}
	
	/**
	 * Returns a vector containing the unique identifiers of the contained OOos
	 * 
	 * @return names of the contained OOos
	 */
	public Vector getOOoKeys(){
		Set names = elements.keySet();
		return new Vector(names);
	}
	
	private void fireOOoRemoved(OOo ooo) {
		for (int i=0, length=listeners.size(); i<length; i++){
			ConfigListener listeneri = (ConfigListener)listeners.get(i);
			listeneri.ConfigRemoved(ooo);
		}
	}
	
	/**
	 * update the ith OOo from the list with the given OOo.
	 * 
	 * @param i position of the ooo to update
	 * @param ooo new value for the OOo
	 */
	public void updateOOo(String oookey, OOo ooo){
		if (elements.containsKey(oookey) && null != ooo){
			
			OOo oooref = (OOo)elements.get(oookey);
			
			// update the attributes
			try {
				oooref.setOOoHome(ooo.getOOoHome());
			} catch (InvalidConfigException e){
				OOEclipsePlugin.logError(e.getLocalizedMessage(), e);  // This message is localized by the OOo class
			}
			
			// Reassign the element in the hashmap
			elements.put(oookey, oooref);
			fireOOoUpdated(ooo);
		}
	}
	
	private void fireOOoUpdated(OOo ooo) {
		for (int i=0, length=listeners.size(); i<length; i++){
			ConfigListener listeneri = (ConfigListener)listeners.get(i);
			listeneri.ConfigUpdated(ooo);
		}
	}
	
	/**
	 * Returns the ooo that corresponds to the given ooo name and buildid.
	 * 
	 * @param oookey unique identifier of the wanted ooo
	 * @return OOo which name equals the one provided
	 */
	public OOo getOOo(String oookey){
		OOo ooo = null;
		
		if (elements.containsKey(oookey)){
			ooo = (OOo)elements.get(oookey);
		} 
		return ooo;
	}
	
	/**
	 * Returns the number of OOo in the list
	 * 
	 * @return number of OOo in the list
	 */
	public int getOOoCount(){
		return elements.size();
	}
		
	/**
	 * Dispose the vector used
	 *
	 */
	public void dispose() {
		listeners.clear();
		elements.clear();
	}

	public static OOoContainer getOOoContainer() {
		
		if (null == container){
			container = new OOoContainer();
			container.loadOOos();
		}
		
		return container;
	}
	
	private Properties getProperties() throws IOException{
		// Loads the ooos config file into a properties object
		String ooos_config_url = OOEclipsePlugin.getDefault().getStateLocation().toString();
		File file = new File(ooos_config_url+"/"+OOEclipsePlugin.OOO_CONFIG);
		if (!file.exists()){
			file.createNewFile();
		}
		
		Properties ooosProperties = new Properties();
	
		ooosProperties.load(new FileInputStream(file));
		
		return ooosProperties;
	}
	
	protected void loadOOos(){
		try {
			// Loads the ooos config file into a properties object
			Properties ooosProperties = getProperties();
			
			int i=0;
			boolean found = false;
			
			do {
				String ooopath = ooosProperties.getProperty(OOEclipsePlugin.OOOPATH_PREFERENCE_KEY+i);
				
				found = (null != ooopath);
				i++;
				
				if (found){
					try {
						OOo ooo = new OOo(ooopath);
						container.addOOo(ooo);
					} catch (InvalidConfigException e){
						OOEclipsePlugin.logError(e.getLocalizedMessage(), e); // This message is localized in SDK class
					}
				}				
			} while (found);
			
		} catch (IOException e) {
			OOEclipsePlugin.logError(
					OOEclipsePlugin.getTranslationString(
							I18nConstants.NOT_READABLE_FILE)+OOEclipsePlugin.OOO_CONFIG, e);
		}
	}
	
	public void saveOOos(){
		
		try {
			Properties ooosProperties = getProperties();
			
			// Load all the existing properties and remove the OOOPATH_PREFERENCE_KEY ones
			int j=0;
			boolean found = false;
			
			do {
				String oooPath = ooosProperties.getProperty(OOEclipsePlugin.OOOPATH_PREFERENCE_KEY+j);
				found = (null != oooPath);
				
				if (found){
					ooosProperties.remove(OOEclipsePlugin.OOOPATH_PREFERENCE_KEY+j);
				}
			} while (found);
			
			// Saving the new OOos 
			Vector vElements = toVector();
			
			for (int i=0, length=getOOoCount(); i<length; i++){
				OOo oooi = (OOo)vElements.get(i);
				ooosProperties.put(OOEclipsePlugin.OOOPATH_PREFERENCE_KEY+i, oooi.getOOoHome());
			}
		
			String ooos_config_url = OOEclipsePlugin.getDefault().getStateLocation().toString();
			File file = new File(ooos_config_url+"/"+OOEclipsePlugin.OOO_CONFIG);
			if (!file.exists()){
				file.createNewFile();
			}
			
			ooosProperties.store(new FileOutputStream(file), "");
		} catch (FileNotFoundException e) {
			OOEclipsePlugin.logError(e.getLocalizedMessage(), e);
		} catch (IOException e){
			OOEclipsePlugin.logError(e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * The SDK Container should not be created by another object
	 *
	 */
	private OOoContainer(){
		
		// Initialize the members
		elements = new HashMap();
		listeners = new Vector();
	}
	
	/**
	 * Returns a unordered vector with the hashmap of the elements
	 *  
	 * @return vector where the elements order isn't guaranteed
	 */
	private Vector toVector(){
		Vector result = new Vector();
		Set entries = elements.entrySet();
		Iterator iter = entries.iterator();
		
		while (iter.hasNext()){
			Object value = ((Map.Entry)iter.next()).getValue();
			result.add(value);
		}
		return result;
	}
}
