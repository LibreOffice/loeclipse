/*************************************************************************
 *
 * $RCSfile: SDKContainer.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/06/09 06:14:08 $
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
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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
	public void addListener(IConfigListener listener){
		if (null != listener){
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes a SDK listener from the container
	 * 
	 * @param listener sdk listener to remove
	 */
	public void removeListener(IConfigListener listener){
		if (null != listener){
			listeners.remove(listener);
		}
	}
	
	/* Methods to manage the sdks */
	
	/**
	 * Returns the sdks elements in an array
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
	public void addSDK(ISdk sdk){
		
		/** 
		 * If there already is a SDK with such an identifier, replace the values,
		 * not the object to keep the references on it
		 */ 
		
		if (null != sdk){
			if (!elements.containsKey(sdk.getId())){
				elements.put(sdk.getId(), sdk);
				fireSDKAdded(sdk);
			} else {
				ISdk sdkref = (ISdk)elements.get(sdk.getId());
				updateSDK(sdkref.getId(), sdk);
			}
		}
	}
	
	private void fireSDKAdded(ISdk sdk) {
		for (int i=0, length=listeners.size(); i<length; i++){
			IConfigListener listeneri = (IConfigListener)listeners.get(i);
			listeneri.ConfigAdded(sdk);
		}
	}

	/**
	 * remove the given SDK from the list. Do not use directly the private field to 
	 * handle SDKs
	 *  
	 * @param sdk SDK to remove
	 */
	public void delSDK(ISdk sdk){
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
	
	private void fireSDKRemoved(ISdk sdk) {
		for (int i=0, length=listeners.size(); i<length; i++){
			IConfigListener listeneri = (IConfigListener)listeners.get(i);
			listeneri.ConfigRemoved(sdk);
		}
	}
	
	/**
	 * update the ith SDK from the list with the given SDK.
	 * 
	 * @param sdkkey position of the sdk to update
	 * @param sdk new value for the SDK
	 */
	public void updateSDK(String sdkkey, ISdk sdk){
		if (elements.containsKey(sdkkey) && null != sdk){
			
			ISdk sdkref = (ISdk)elements.get(sdkkey);
			
			// update the attributes
			try {
				sdkref.setHome(sdk.getHome());
			} catch (InvalidConfigException e){
				PluginLogger.getInstance().error(e.getLocalizedMessage(), e);  
				// This message is localized by the SDK class
			}
			
			// Reassign the element in the hashmap
			elements.put(sdkkey, sdkref);
			fireSDKUpdated(sdk);
		}
	}
	
	private void fireSDKUpdated(ISdk sdk) {
		for (int i=0, length=listeners.size(); i<length; i++){
			IConfigListener listeneri = (IConfigListener)listeners.get(i);
			listeneri.ConfigUpdated(sdk);
		}
	}
	
	/**
	 * Returns the sdk that corresponds to the given sdk name and buildid.
	 * 
	 * @param sdkkey unique identifier of the wanted sdk
	 * @return SDK which name equals the one provided
	 */
	public ISdk getSDK(String sdkkey){
		ISdk sdk = null;
		
		if (elements.containsKey(sdkkey)){
			sdk = (ISdk)elements.get(sdkkey);
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

	/**
	 * Singleton accessor, named <code>getInstance</code> in many other
	 * singleton pattern implementations
	 */
	public static SDKContainer getSDKContainer() {
		
		if (null == container){
			container = new SDKContainer();
			container.loadSDKs();
		}
		
		return container;
	}
	
	/**
	 * Loads the SDK already configured instances from the 
	 * preferences.
	 */
	protected void loadSDKs(){
		
		ISdk[] sdks = PropertiesManager.loadSDKs();
		for (int i=0; i < sdks.length; i++) {
			addSDK(sdks[i]);
		}
	}
	
	/**
	 * Saves the SDK already configured instances to the 
	 * preferences.
	 */
	public void saveSDKs(){
		
		// Saving the new SDKs 
		Vector vElements = toVector();
		ISdk[] sdks = new ISdk[getSDKCount()];
		
		for (int i=0, length=getSDKCount(); i<length; i++){
			sdks[i] = (ISdk)vElements.get(i);
		}
		
		PropertiesManager.saveSDKs(sdks);
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