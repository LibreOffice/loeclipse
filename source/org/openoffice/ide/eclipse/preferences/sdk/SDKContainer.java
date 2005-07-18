/*************************************************************************
 *
 * $RCSfile: SDKContainer.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/07/18 19:36:03 $
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

import java.util.Vector;

import org.openoffice.ide.eclipse.preferences.sdk.SDK;

/**
 * Class containing the sdks
 * 
 * @author cbosdonnat
 *
 */
public class SDKContainer { 
	
	/**
	 * Vector of the SDK container listeners
	 */
	private Vector listeners = new Vector();

	/**
	 * Vector containing the sdk lines
	 */
	private Vector elements = new Vector();
	
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
		return elements.toArray();
	}
	
	/**
	 * Add the sdk given in parameter to the list of the others. Do not use directly 
	 * the private field to handle SDKs
	 * 
	 * @param sdk SDK to add
	 */
	public void addSDK(SDK sdk){
		if (null != sdk){
			if (!elements.contains(sdk)){
				elements.add(sdk);
				fireSDKAdded(sdk);
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
			if (elements.contains(sdk)){
				elements.remove(sdk);
				fireSDKRemoved(sdk);
			}
		}
	}
	
	public void clear(){
		elements.clear();
		fireSDKRemoved(null);
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
	public void updateSDK(int i, SDK sdk){
		if (i<elements.size() && i>=0 && null != sdk){
			elements.set(i, sdk);
			fireSDKUpdated(i, sdk);
		}
	}
	
	private void fireSDKUpdated(int id, SDK sdk) {
		for (int i=0, length=listeners.size(); i<length; i++){
			SDKListener listeneri = (SDKListener)listeners.get(i);
			listeneri.SDKUpdated(id, sdk);
		}
	}
	
	/**
	 * Returns the ith SDK of the list. As other arrays and Vectors in Java, the first
	 * SDK is the number 0.
	 * 
	 * @param i position of the SDK to get.
	 * @return SDK at the ith position in the list
	 */
	public SDK getSDK(int i){
		SDK sdk = null;
		
		if (i<elements.size() && i>=0){
			sdk = (SDK)elements.get(i);
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
	 * Returns the position of the sdk given in parameter
	 * 
	 * @param sdk sdk which position is wanted
	 * @return the position of the sdk
	 */
	public int indexOf(SDK sdk){
		return elements.indexOf(sdk);
	}
	
	/**
	 * Dispose the vector used
	 * 
	 * TODO Check the code for effectively disposing
	 *
	 */
	public void dispose() {
		listeners.clear();
		elements.clear();
	}
}