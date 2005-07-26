/*************************************************************************
 *
 * $RCSfile: SDKContainer.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/07/26 06:24:00 $
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
import org.openoffice.ide.eclipse.preferences.sdk.SDK;

/**
 * Singleton object containing the sdks.
 * 
 * @author cbosdonnat
 *
 */
public class SDKContainer { 
	
	/**
	 * Plugin home relative path for the sdks configuration file
	 */
	private final static String SDKS_CONFIG = ".sdks_config";
	
	private static SDKContainer container;
	
	/**
	 * Vector of the SDK container listeners
	 */
	private Vector listeners;

	/**
	 * HashMap containing the sdk lines referenced by their name
	 */
	private HashMap elements;
	
	
	/* Methods to manage the listeners */
	
	/**
	 * Add a SDK listener to the container
	 * 
	 *  @param listener sdk listener to add 
	 */
	public void addListener(SDKListener listener){
		if (null != listener){
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes a SDK listener from the container
	 * 
	 * @param listener sdk listener to remove
	 */
	public void removeListener(SDKListener listener){
		if (null != listener){
			listeners.remove(listener);
		}
	}
	
	/* Methods to manage the sdks */
	
	/**
	 * Returns the sdks elements in an array
	 * @return
	 */
	public Object[] toArray(){
		return toVector().toArray(); 
	}
	
	/**
	 * Add the sdk given in parameter to the list of the others. Do not use directly 
	 * the private field to handle SDKs
	 * 
	 * @param sdk SDK to add
	 */
	public void addSDK(SDK sdk){
		
		/** 
		 * If there already is a SDK with such an identifier, replace the values,
		 * not the object to keep the references on it
		 */ 
		
		if (null != sdk){
			if (!elements.containsKey(sdk.getId())){
				elements.put(sdk.getId(), sdk);
				fireSDKAdded(sdk);
			} else {
				SDK sdkref = (SDK)elements.get(sdk.getId());
				updateSDK(sdkref.getId(), sdk);
			}
		}
	}
	
	private void fireSDKAdded(SDK sdk) {
		for (int i=0, length=listeners.size(); i<length; i++){
			SDKListener listeneri = (SDKListener)listeners.get(i);
			listeneri.SDKAdded(sdk);
		}
	}

	/**
	 * remove the given SDK from the list. Do not use directly the private field to 
	 * handle SDKs
	 *  
	 * @param sdk SDK to remove
	 */
	public void delSDK(SDK sdk){
		if (null != sdk){
			if (elements.containsKey(sdk.getId())){
				elements.remove(sdk.getId());
				fireSDKRemoved(sdk);
			}
		}
	}
	
	/**
	 * Removes all the SDK contained
	 *
	 */
	public void clear(){
		elements.clear();
		fireSDKRemoved(null);
	}
	
	/**
	 * Returns a vector containing the unique identifiers of the contained SDKs
	 * 
	 * @return names of the contained SDKs
	 */
	public Vector getSDKKeys(){
		Set names = elements.keySet();
		return new Vector(names);
	}
	
	private void fireSDKRemoved(SDK sdk) {
		for (int i=0, length=listeners.size(); i<length; i++){
			SDKListener listeneri = (SDKListener)listeners.get(i);
			listeneri.SDKRemoved(sdk);
		}
	}
	
	/**
	 * update the ith SDK from the list with the given SDK.
	 * 
	 * @param i position of the sdk to update
	 * @param sdk new value for the SDK
	 */
	public void updateSDK(String sdkkey, SDK sdk){
		if (elements.containsKey(sdkkey) && null != sdk){
			
			SDK sdkref = (SDK)elements.get(sdkkey);
			
			// update the attributes
			try {
				sdkref.setSDKHome(sdk.getSDKHome());
				sdkref.setOOoHome(sdk.getOOoHome());
			} catch (InvalidSDKException e){
				OOEclipsePlugin.logError(e.getLocalizedMessage(), e);  // This message is localized by the SDK class
			}
			
			// Reassign the element in the hashmap
			elements.put(sdkkey, sdkref);
			fireSDKUpdated(sdk);
		}
	}
	
	private void fireSDKUpdated(SDK sdk) {
		for (int i=0, length=listeners.size(); i<length; i++){
			SDKListener listeneri = (SDKListener)listeners.get(i);
			listeneri.SDKUpdated(sdk);
		}
	}
	
	/**
	 * Returns the sdk that corresponds to the given sdk name and buildid.
	 * 
	 * @param sdkkey unique identifier of the wanted sdk
	 * @return SDK which name equals the one provided
	 */
	public SDK getSDK(String sdkkey){
		SDK sdk = null;
		
		if (elements.containsKey(sdkkey)){
			sdk = (SDK)elements.get(sdkkey);
		} 
		return sdk;
	}
	
	/**
	 * Returns the number of SDK in the list
	 * 
	 * @return number of SDK in the list
	 */
	public int getSDKCount(){
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

	public static SDKContainer getSDKContainer() {
		
		if (null == container){
			container = new SDKContainer();
			container.loadSDKs();
		}
		
		return container;
	}
	
	protected void loadSDKs(){
		try {
			// Loads the sdks config file into a properties object
			String sdks_config_url = OOEclipsePlugin.getDefault().getStateLocation().toString();
			File file = new File(sdks_config_url+"/"+SDKS_CONFIG);
			if (!file.exists()){
				file.createNewFile();
			}
			
			Properties sdksProperties = new Properties();
		
			sdksProperties.load(new FileInputStream(file));
			
			int i=0;
			boolean found = false;
			
			do {
				String path = sdksProperties.getProperty(OOEclipsePlugin.SDKPATH_PREFERENCE_KEY+i);
				String ooopath = sdksProperties.getProperty(OOEclipsePlugin.OOOPATH_PREFERENCE_KEY+i);
				
				found = !(null == path || null == ooopath);
				i++;
				
				if (found){
					try {
						SDK sdk = new SDK(path, ooopath);
						container.addSDK(sdk);
					} catch (InvalidSDKException e){
						OOEclipsePlugin.logError(e.getLocalizedMessage(), e); // This message is localized in SDK class
					}
				}				
			} while (found);
			
		} catch (IOException e) {
			OOEclipsePlugin.logError(
					OOEclipsePlugin.getTranslationString(I18nConstants.NOT_READABLE_FILE)+SDKS_CONFIG, e);
		}
	}
	
	public void saveSDKs(){
		Properties sdksProperties = new Properties();
		
		// Saving the new SDKs 
		Vector vElements = toVector();
		
		for (int i=0, length=getSDKCount(); i<length; i++){
			SDK sdki = (SDK)vElements.get(i);
			sdksProperties.put(OOEclipsePlugin.SDKPATH_PREFERENCE_KEY+i, sdki.getSDKHome());
			sdksProperties.put(OOEclipsePlugin.OOOPATH_PREFERENCE_KEY+i, sdki.getOOoHome());
		}
		
		try {
			String sdks_config_url = OOEclipsePlugin.getDefault().getStateLocation().toString();
			File file = new File(sdks_config_url+"/"+SDKS_CONFIG);
			if (!file.exists()){
				file.createNewFile();
			}
			
			sdksProperties.store(new FileOutputStream(file), "");
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
	private SDKContainer(){
		
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