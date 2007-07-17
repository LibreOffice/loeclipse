/*************************************************************************
 *
 * $RCSfile: SDKContainer.java,v $
 *
 * $Revision: 1.7 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/07/17 21:01:00 $
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
package org.openoffice.ide.eclipse.core.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.PropertiesManager;
import org.openoffice.ide.eclipse.core.preferences.IConfigListener;
import org.openoffice.ide.eclipse.core.preferences.ISdk;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 * Singleton object containing the SDK instances.
 * 
 * @author cbosdonnat
 */
public class SDKContainer { 
	
	private static SDKContainer sInstance = new SDKContainer();
	
	/**
	 * Vector of the SDK container listeners
	 */
	private Vector<IConfigListener> mListeners;

	/**
	 * HashMap containing the sdk lines referenced by their name
	 */
	private HashMap<String, ISdk> mElements;
	
	
	/* Methods to manage the listeners */
	
	/**
	 * Add a SDK listener to the container
	 * 
	 *  @param listener sdk listener to add 
	 */
	public static void addListener(IConfigListener listener){
		if (null != listener){
			sInstance.mListeners.add(listener);
		}
	}
	
	/**
	 * Removes a SDK listener from the container
	 * 
	 * @param listener sdk listener to remove
	 */
	public static void removeListener(IConfigListener listener){
		if (null != listener){
			sInstance.mListeners.remove(listener);
		}
	}
	
	/* Methods to manage the sdks */
	
	/**
	 * Returns the sdks elements in an array
	 */
	public static Object[] toArray(){
		Vector<ISdk> vElements = toVector();
		Object[] elements = vElements.toArray();
		
		vElements.clear();
		return elements;
	}
	
	/**
	 * Add the sdk given in parameter to the list of the others. Do not use directly 
	 * the private field to handle SDKs
	 * 
	 * @param sdk SDK to add
	 */
	public static void addSDK(ISdk sdk){
		
		/** 
		 * If there already is a SDK with such an identifier, replace the values,
		 * not the object to keep the references on it
		 */ 
		
		if (null != sdk){
			if (!sInstance.mElements.containsKey(sdk.getId())){
				sInstance.mElements.put(sdk.getId(), sdk);
				sInstance.fireSDKAdded(sdk);
			} else {
				ISdk sdkref = sInstance.mElements.get(sdk.getId());
				updateSDK(sdkref.getId(), sdk);
			}
		}
	}
	
	private void fireSDKAdded(ISdk sdk) {
		for (int i=0, length=mListeners.size(); i<length; i++){
			IConfigListener listeneri = mListeners.get(i);
			listeneri.ConfigAdded(sdk);
		}
	}

	/**
	 * remove the given SDK from the list. Do not use directly the private field to 
	 * handle SDKs
	 *  
	 * @param sdk SDK to remove
	 */
	public static void delSDK(ISdk sdk){
		if (null != sdk){
			if (sInstance.mElements.containsKey(sdk.getId())){
				sInstance.mElements.remove(sdk.getId());
				sInstance.fireSDKRemoved(sdk);
			}
		}
	}
	
	/**
	 * Removes all the SDK contained
	 *
	 */
	public static void clear(){
		sInstance.mElements.clear();
		sInstance.fireSDKRemoved(null);
	}
	
	/**
	 * Returns a vector containing the unique identifiers of the contained SDKs
	 * 
	 * @return names of the contained SDKs
	 */
	public static Vector<String> getSDKKeys(){
		Set<String> names = sInstance.mElements.keySet();
		return new Vector<String>(names);
	}
	
	private void fireSDKRemoved(ISdk sdk) {
		for (int i=0, length=mListeners.size(); i<length; i++){
			IConfigListener listeneri = mListeners.get(i);
			listeneri.ConfigRemoved(sdk);
		}
	}
	
	/**
	 * update the ith SDK from the list with the given SDK.
	 * 
	 * @param sdkkey position of the sdk to update
	 * @param sdk new value for the SDK
	 */
	public static void updateSDK(String sdkkey, ISdk sdk){
		if (sInstance.mElements.containsKey(sdkkey) && null != sdk){
			
			ISdk sdkref = sInstance.mElements.get(sdkkey);
			
			// update the attributes
			try {
				sdkref.setHome(sdk.getHome());
			} catch (InvalidConfigException e){
				PluginLogger.error(e.getLocalizedMessage(), e);  
				// This message is localized by the SDK class
			}
			
			// Reassign the element in the hashmap
			sInstance.mElements.put(sdkkey, sdkref);
			sInstance.fireSDKUpdated(sdk);
		}
	}
	
	private void fireSDKUpdated(ISdk sdk) {
		for (int i=0, length=mListeners.size(); i<length; i++){
			IConfigListener listeneri = mListeners.get(i);
			listeneri.ConfigUpdated(sdk);
		}
	}
	
	/**
	 * Returns the sdk that corresponds to the given sdk name and buildid.
	 * 
	 * @param sdkkey unique identifier of the wanted sdk
	 * @return SDK which name equals the one provided
	 */
	public static ISdk getSDK(String sdkkey){
		ISdk sdk = null;
		
		if (sInstance.mElements.containsKey(sdkkey)){
			sdk = sInstance.mElements.get(sdkkey);
		} 
		return sdk;
	}
	
	/**
	 * Returns the number of SDK in the list
	 * 
	 * @return number of SDK in the list
	 */
	public static int getSDKCount(){
		return sInstance.mElements.size();
	}
		
	/**
	 * Dispose the vector used
	 *
	 */
	public static void dispose() {
		sInstance.mListeners.clear();
		sInstance.mElements.clear();
	}

	/**
	 * Singleton accessor, named <code>getInstance</code> in many other
	 * singleton pattern implementations
	 */
	public static SDKContainer getInstance() {
		
		if (null == sInstance){
			sInstance = new SDKContainer();
		}
		return sInstance;
	}
	
	/**
	 * Loads the SDK already configured instances from the 
	 * preferences.
	 */
	public static void load(){
		
		ISdk[] sdks = PropertiesManager.loadSDKs();
		for (int i=0; i < sdks.length; i++) {
			addSDK(sdks[i]);
		}
	}
	
	/**
	 * Saves the SDK already configured instances to the 
	 * preferences.
	 */
	public static void saveSDKs(){
		
		// Saving the new SDKs 
		Vector<ISdk> vElements = toVector();
		ISdk[] sdks = vElements.toArray(new ISdk[getSDKCount()]);
		
		// vector cleaning
		vElements.clear();
		
		PropertiesManager.saveSDKs(sdks);
	}
	
	/**
	 * The SDK Container should not be created by another object
	 *
	 */
	private SDKContainer(){
		
		// Initialize the members
		mElements = new HashMap<String, ISdk>();
		mListeners = new Vector<IConfigListener>();
	}
	
	/**
	 * Returns a unordered vector with the hashmap of the elements
	 *  
	 * @return vector where the elements order isn't guaranteed
	 */
	private static Vector<ISdk> toVector(){
		Vector<ISdk> result = new Vector<ISdk>();
		Set<Entry<String, ISdk>> entries = sInstance.mElements.entrySet();
		Iterator<Entry<String, ISdk>> iter = entries.iterator();
		
		while (iter.hasNext()){
			result.add(iter.next().getValue());
		}
		return result;
	}
}