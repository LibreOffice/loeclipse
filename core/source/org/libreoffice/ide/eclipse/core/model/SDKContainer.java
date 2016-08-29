/*************************************************************************
 *
 * $RCSfile: SDKContainer.java,v $
 *
 * $Revision: 1.8 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:30 $
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
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
package org.libreoffice.ide.eclipse.core.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.internal.helpers.PropertiesManager;
import org.libreoffice.ide.eclipse.core.model.config.IConfigListener;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;
import org.libreoffice.ide.eclipse.core.model.config.InvalidConfigException;

/**
 * Singleton object containing the SDK instances.
 *
 */
public class SDKContainer {

    private static SDKContainer sInstance = new SDKContainer();

    /**
     * Vector of the SDK container listeners.
     */
    private Vector<IConfigListener> mListeners;

    /**
     * HashMap containing the SDK lines referenced by their name.
     */
    private HashMap<String, ISdk> mElements;

    /**
     * The SDK Container should not be created by another object.
     */
    private SDKContainer() {

        // Initialize the members
        mElements = new HashMap<String, ISdk>();
        mListeners = new Vector<IConfigListener>();
    }

    /* Methods to manage the listeners */

    /**
     * Add a SDK listener to the container.
     *
     * @param pListener
     *            SDK listener to add
     */
    public static void addListener(IConfigListener pListener) {
        if (null != pListener) {
            sInstance.mListeners.add(pListener);
        }
    }

    /**
     * Removes a SDK listener from the container.
     *
     * @param pListener
     *            SDK listener to remove
     */
    public static void removeListener(IConfigListener pListener) {
        if (null != pListener) {
            sInstance.mListeners.remove(pListener);
        }
    }

    // -------------------------------------- Methods to manage the sdks

    /**
     * @return the sdks elements in an array.
     */
    public static Object[] toArray() {
        Vector<ISdk> vElements = toVector();
        Object[] elements = vElements.toArray();

        vElements.clear();
        return elements;
    }

    /**
     * Add the SDK given in parameter to the list of the others. Do not use directly the private field to handle SDKs
     *
     * @param pSdk
     *            SDK to add
     */
    public static void addSDK(ISdk pSdk) {

        /**
         * If there already is a SDK with such an identifier, replace the values, not the object to keep the references
         * on it
         */

        if (null != pSdk) {
            if (!sInstance.mElements.containsKey(pSdk.getName())) {
                sInstance.mElements.put(pSdk.getName(), pSdk);
                sInstance.fireSDKAdded(pSdk);
            } else {
                ISdk sdkref = sInstance.mElements.get(pSdk.getName());
                updateSDK(sdkref.getName(), pSdk);
            }
        }
    }

    /**
     * Notify every listener that an SDK instance configuration has been added.
     *
     * @param pSdk
     *            the added SDK
     */
    private void fireSDKAdded(ISdk pSdk) {
        for (int i = 0, length = mListeners.size(); i < length; i++) {
            IConfigListener listeneri = mListeners.get(i);
            listeneri.ConfigAdded(pSdk);
        }
    }

    /**
     * remove the given SDK from the list. Do not use directly the private field to handle SDKs
     *
     * @param pSdk
     *            SDK to remove
     */
    public static void delSDK(ISdk pSdk) {
        if (null != pSdk) {
            if (sInstance.mElements.containsKey(pSdk.getName())) {
                sInstance.mElements.remove(pSdk.getName());
                sInstance.fireSDKRemoved(pSdk);
            }
        }
    }

    /**
     * Removes all the SDK contained.
     *
     */
    public static void clear() {
        sInstance.mElements.clear();
        sInstance.fireSDKRemoved(null);
    }

    /**
     * Returns a vector containing the unique identifiers of the contained SDKs.
     *
     * @return names of the contained SDKs
     */
    public static Vector<String> getSDKKeys() {
        Set<String> names = sInstance.mElements.keySet();
        return new Vector<String>(names);
    }

    /**
     * Notify all the listeners that an SDK instance configuration has been removed.
     *
     * @param pSdk
     *            the removed SDK
     */
    private void fireSDKRemoved(ISdk pSdk) {
        for (int i = 0, length = mListeners.size(); i < length; i++) {
            IConfigListener listeneri = mListeners.get(i);
            listeneri.ConfigRemoved(pSdk);
        }
    }

    /**
     * update the ith SDK from the list with the given SDK.
     *
     * @param pSdkkey
     *            position of the sdk to update
     * @param pSdk
     *            new value for the SDK
     */
    public static void updateSDK(String pSdkkey, ISdk pSdk) {
        if (sInstance.mElements.containsKey(pSdkkey) && null != pSdk) {

            ISdk sdkref = sInstance.mElements.get(pSdkkey);

            // update the attributes
            try {
                sdkref.initialize(pSdk.getHome(), pSdkkey);
            } catch (InvalidConfigException e) {
                PluginLogger.error(e.getLocalizedMessage(), e);
                // This message is localized by the SDK class
            }

            // Reassign the element in the hashmap
            sInstance.mElements.put(pSdkkey, sdkref);
            sInstance.fireSDKUpdated(pSdk);
        }
    }

    /**
     * Notify every listener that an SDK instance configuration has been updated.
     *
     * @param pSdk
     *            the updated SDK
     */
    private void fireSDKUpdated(ISdk pSdk) {
        for (int i = 0, length = mListeners.size(); i < length; i++) {
            IConfigListener listeneri = mListeners.get(i);
            listeneri.ConfigUpdated(pSdk);
        }
    }

    /**
     * Returns the sdk that corresponds to the given sdk name and buildid.
     *
     * @param pSdkkey
     *            unique identifier of the wanted sdk
     * @return SDK which name equals the one provided
     */
    public static ISdk getSDK(String pSdkkey) {
        ISdk sdk = null;

        if (sInstance.mElements.containsKey(pSdkkey)) {
            sdk = sInstance.mElements.get(pSdkkey);
        }
        return sdk;
    }

    /**
     * Returns the number of SDK in the list.
     *
     * @return number of SDK in the list
     */
    public static int getSDKCount() {
        return sInstance.mElements.size();
    }

    /**
     * Dispose the vector used.
     */
    public static void dispose() {
        sInstance.mListeners.clear();
        sInstance.mElements.clear();
    }

    /**
     * Singleton accessor, named <code>getInstance</code> in many other singleton pattern implementations.
     *
     * @return the {@link SDKContainer} singleton instance.
     */
    public static SDKContainer getInstance() {

        if (null == sInstance) {
            sInstance = new SDKContainer();
        }
        return sInstance;
    }

    /**
     * Loads the SDK already configured instances from the preferences.
     */
    public static void load() {

        ISdk[] sdks = PropertiesManager.loadSDKs();
        for (int i = 0; i < sdks.length; i++) {
            addSDK(sdks[i]);
        }
    }

    /**
     * Saves the SDK already configured instances to the preferences.
     */
    public static void saveSDKs() {

        // Saving the new SDKs
        Vector<ISdk> vElements = toVector();
        ISdk[] sdks = vElements.toArray(new ISdk[getSDKCount()]);

        // vector cleaning
        vElements.clear();

        PropertiesManager.saveSDKs(sdks);
    }

    /**
     * Returns a unordered vector with the hashmap of the elements.
     *
     * @return vector where the elements order isn't guaranteed
     */
    private static Vector<ISdk> toVector() {
        Vector<ISdk> result = new Vector<ISdk>();
        Set<Entry<String, ISdk>> entries = sInstance.mElements.entrySet();
        Iterator<Entry<String, ISdk>> iter = entries.iterator();

        while (iter.hasNext()) {
            result.add(iter.next().getValue());
        }
        return result;
    }
}