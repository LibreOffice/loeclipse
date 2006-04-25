/*************************************************************************
 *
 * $RCSfile: OOoContainer.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/25 19:10:05 $
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
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

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
	public void addListener(IConfigListener listener){
		if (null != listener){
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes a config listener from the container
	 * 
	 * @param listener config listener to remove
	 */
	public void removeListener(IConfigListener listener){
		if (null != listener){
			listeners.remove(listener);
		}
	}
	
	/* Methods to manage the ooos */
	
	/**
	 * Returns the ooos elements in an array
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
	public void addOOo(IOOo ooo){
		
		/** 
		 * If there already is a OOo with such an identifier, replace the values,
		 * not the object to keep the references on it
		 */ 
		
		if (null != ooo){
			if (!elements.containsKey(ooo.getId())){
				elements.put(ooo.getId(), ooo);
				fireOOoAdded(ooo);
			} else {
				IOOo oooref = (IOOo)elements.get(ooo.getId());
				updateOOo(oooref.getId(), ooo);
			}
		}
	}
	
	private void fireOOoAdded(IOOo ooo) {
		for (int i=0, length=listeners.size(); i<length; i++){
			IConfigListener listeneri = (IConfigListener)listeners.get(i);
			listeneri.ConfigAdded(ooo);
		}
	}

	/**
	 * remove the given OOo from the list. Do not use directly the private field to 
	 * handle OOos
	 *  
	 * @param ooo OOo to remove
	 */
	public void delOOo(IOOo ooo){
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
	
	private void fireOOoRemoved(IOOo ooo) {
		for (int i=0, length=listeners.size(); i<length; i++){
			IConfigListener listeneri = (IConfigListener)listeners.get(i);
			listeneri.ConfigRemoved(ooo);
		}
	}
	
	/**
	 * update the ith OOo from the list with the given OOo.
	 * 
	 * @param oookey position of the ooo to update
	 * @param ooo new value for the OOo
	 */
	public void updateOOo(String oookey, IOOo ooo){
		if (elements.containsKey(oookey) && null != ooo){
			
			IOOo oooref = (IOOo)elements.get(oookey);
			
			// update the attributes
			try {
				oooref.setHome(ooo.getHome());
			} catch (InvalidConfigException e){
				PluginLogger.getInstance().error(e.getLocalizedMessage(), e);  
				// This message is localized by the OOo class
			}
			
			// Reassign the element in the hashmap
			elements.put(oookey, oooref);
			fireOOoUpdated(ooo);
		}
	}
	
	private void fireOOoUpdated(IOOo ooo) {
		for (int i=0, length=listeners.size(); i<length; i++){
			IConfigListener listeneri = (IConfigListener)listeners.get(i);
			listeneri.ConfigUpdated(ooo);
		}
	}
	
	/**
	 * Returns the ooo that corresponds to the given ooo name and buildid.
	 * 
	 * @param oookey unique identifier of the wanted ooo
	 * @return OOo which name equals the one provided
	 */
	public IOOo getOOo(String oookey){
		IOOo ooo = null;
		
		if (elements.containsKey(oookey)){
			ooo = (IOOo)elements.get(oookey);
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
	
	protected void loadOOos(){
		
		IOOo[] ooos = PropertiesManager.loadOOos();
		for (int i=0; i < ooos.length; i++) {
			addOOo(ooos[i]);
		}
	}
	
	public void saveOOos(){
		
		// Saving the new OOos 
		Vector vElements = toVector();
		IOOo[] ooos = new IOOo[getOOoCount()];
		
		for (int i=0, length=getOOoCount(); i<length; i++){
			ooos[i] = (IOOo)vElements.get(i);
		}
		
		PropertiesManager.saveOOos(ooos);
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
