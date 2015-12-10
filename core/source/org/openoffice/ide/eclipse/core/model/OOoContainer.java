/*************************************************************************
 *
 * $RCSfile: OOoContainer.java,v $
 *
 * $Revision: 1.9 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/12/26 14:40:25 $
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
package org.openoffice.ide.eclipse.core.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.PropertiesManager;
import org.openoffice.ide.eclipse.core.internal.model.OOo;
import org.openoffice.ide.eclipse.core.internal.model.URE;
import org.openoffice.ide.eclipse.core.model.config.IConfigListener;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.InvalidConfigException;

/**
 * Singleton object containing the LibreOffice configurations.
 *
 * @author cedricbosdo
 *
 */
public class OOoContainer {

    private static OOoContainer sInstance = new OOoContainer();

    /**
     * Vector of the configuration container listeners.
     */
    private Vector<IConfigListener> mListeners;

    /**
     * HashMap containing the ooo lines referenced by their path.
     */
    private HashMap<String, IOOo> mElements;

    /**
     * The SDK Container should not be created by another object.
     */
    private OOoContainer() {

        // Initialize the members
        mElements = new HashMap<String, IOOo>();
        mListeners = new Vector<IConfigListener>();
    }

    // -------------------------- Methods to manage the listeners

    /**
     * Add a configuration listener to the container.
     *
     * @param pListener
     *            configuration listener to add
     */
    public static void addListener(IConfigListener pListener) {
        if (null != pListener) {
            sInstance.mListeners.add(pListener);
        }
    }

    /**
     * Removes a configuration listener from the container.
     *
     * @param pListener
     *            configuration listener to remove
     */
    public static void removeListener(IConfigListener pListener) {
        if (null != pListener) {
            sInstance.mListeners.remove(pListener);
        }
    }

    /* Methods to manage the ooos */

    /**
     * @return the ooos elements in an array.
     */
    public static Object[] toArray() {
        Vector<IOOo> vElements = toVector();
        Object[] elements = vElements.toArray();

        vElements.clear();
        return elements;
    }

    /**
     * Add the OOo given in parameter to the list of the others. Do not use directly the private field to handle OOos
     *
     * @param pOoo
     *            OOo to add
     */
    public static void addOOo(IOOo pOoo) {

        /**
         * If there already is a OOo with such an identifier, replace the values, not the object to keep the references
         * on it
         */

        if (null != pOoo) {
            if (!sInstance.mElements.containsKey(pOoo.getName())) {
                sInstance.mElements.put(pOoo.getName(), pOoo);
                sInstance.fireOOoAdded(pOoo);
            } else {
                IOOo oooref = sInstance.mElements.get(pOoo.getName());
                updateOOo(oooref.getName(), pOoo);
            }
        }
    }

    /**
     * Notify every listener that a LibreOffice instance configuration has been added.
     *
     * @param pOoo
     *            the added OOo
     */
    private void fireOOoAdded(IOOo pOoo) {
        for (int i = 0, length = mListeners.size(); i < length; i++) {
            IConfigListener listeneri = mListeners.get(i);
            listeneri.ConfigAdded(pOoo);
        }
    }

    /**
     * remove the given OOo from the list. Do not use directly the private field to handle OOos
     *
     * @param pOoo
     *            OOo to remove
     */
    public static void delOOo(IOOo pOoo) {
        if (null != pOoo) {
            if (sInstance.mElements.containsKey(pOoo.getName())) {
                sInstance.mElements.remove(pOoo.getName());
                sInstance.fireOOoRemoved(pOoo);
            }
        }
    }

    /**
     * Removes all the OOo contained.
     */
    public static void clear() {
        sInstance.mElements.clear();
        sInstance.fireOOoRemoved(null);
    }

    /**
     * Returns a vector containing the unique identifiers of the contained OOos.
     *
     * @return names of the contained OOos
     */
    public static Vector<String> getOOoKeys() {
        Set<String> paths = sInstance.mElements.keySet();
        return new Vector<String>(paths);
    }

    /**
     * Checks whether the corresponding LibreOffice name already exists.
     *
     * @param pName
     *            the OOo Name to check
     * @return <code>true</code> if the name is already present, <code>false</code> otherwise.
     */
    public static boolean containsName(String pName) {
        return sInstance.mElements.containsKey(pName);
    }

    /**
     * Computes a unique name from the given one.
     *
     * @param pName
     *            the name to render unique
     * @return the unique name
     */
    public static String getUniqueName(String pName) {

        String newName = pName;
        if (containsName(newName)) {
            Matcher m = Pattern.compile("(.*)#([0-9]+)$").matcher(newName); //$NON-NLS-1$

            // initialise as if the name contains no #i at its end
            int number = 0;
            String nameRoot = new String(newName);

            if (m.matches()) {
                number = Integer.parseInt(m.group(2));
                nameRoot = m.group(1);
            }

            // Check for the last number
            do {
                number += 1;
                newName = nameRoot + " #" + number; //$NON-NLS-1$
            } while (containsName(newName));
        }
        return newName;
    }

    /**
     * Notify all the listeners that a LibreOffice instance configuration has been removed.
     *
     * @param pOoo
     *            the removed LibreOffice
     */
    private void fireOOoRemoved(IOOo pOoo) {
        for (int i = 0, length = mListeners.size(); i < length; i++) {
            IConfigListener listeneri = mListeners.get(i);
            listeneri.ConfigRemoved(pOoo);
        }
    }

    /**
     * Update the with OOo from the list with the given OOo.
     *
     * @param pOookey
     *            position of the ooo to update
     * @param pOoo
     *            new value for the OOo
     */
    public static void updateOOo(String pOookey, IOOo pOoo) {
        if (sInstance.mElements.containsKey(pOookey) && null != pOoo) {

            IOOo oooref = sInstance.mElements.get(pOookey);

            // update the attributes
            try {
                oooref.setHome(pOoo.getHome());
            } catch (InvalidConfigException e) {
                PluginLogger.error(e.getLocalizedMessage(), e);
            }

            // Reassign the element in the hashmap
            sInstance.mElements.put(pOookey, oooref);
            sInstance.fireOOoUpdated(pOoo);
        }
    }

    /**
     * Notify every listener that a LibreOffice instance configuration has been updated.
     *
     * @param pOoo
     *            the updated LibreOffice
     */
    private void fireOOoUpdated(IOOo pOoo) {
        for (int i = 0, length = mListeners.size(); i < length; i++) {
            IConfigListener listeneri = mListeners.get(i);
            listeneri.ConfigUpdated(pOoo);
        }
    }

    /**
     * Returns the ooo that corresponds to the given ooo name and buildid.
     *
     * @param pOookey
     *            unique identifier of the wanted ooo
     * @return OOo which name equals the one provided
     */
    public static IOOo getOOo(String pOookey) {
        IOOo ooo = null;

        if (sInstance.mElements.containsKey(pOookey)) {
            ooo = sInstance.mElements.get(pOookey);
        }
        return ooo;
    }

    /**
     * Leniently return an OOo instance descriptor from a given value.
     *
     * <p>
     * This method will try several ways to find an OOo. These are the following:
     * <ol>
     * <li>Check if there is a configured OOo with a name like <code>pValue</code></li>
     * <li>Check if there is a configured OOo at a path like <code>pValue</code></li>
     * <li>Check if there is an OOo at the given path and configure it if necessary</li>
     * <li>Get an OOo instance from the configured ones</li>
     * </ol>
     * If no OOo instance can be found using one of the previous ways, <code>null</code> will be returned.
     * </p>
     *
     * @param pValue
     *            the value helping to find the OOo instance.
     * @return the OOo instance or <code>null</code> if not found
     */
    public static IOOo getSomeOOo(String pValue) {
        IOOo found = null;

        // First attempt: try to look by OOo name.
        found = getOOo(pValue);

        // Second attempt: try by path amongst the registered OOos
        if (found == null) {
            Iterator<IOOo> iter = sInstance.mElements.values().iterator();
            while (iter.hasNext() && found == null) {
                IOOo ooo = iter.next();
                if (ooo.getHome().equals(pValue)) {
                    found = ooo;
                }
            }
        }

        // Third attempt: Try to create a new OOo an register it.
        if (found == null) {
            try {
                found = new OOo(pValue);
            } catch (Exception errOoo) {
                try {
                    found = new URE(pValue);
                } catch (Exception errUre) {
                    // Still not found: nothing to log
                }
            }

            // Register the OOo
            if (found != null) {
                addOOo(found);
            }
        }

        // Fourth attempt: Get a registered OOo
        if (found == null && sInstance.mElements.size() > 0) {
            found = sInstance.mElements.values().iterator().next();
        }

        return found;
    }

    /**
     * Returns the number of OOo in the list.
     *
     * @return number of OOo in the list
     */
    public static int getOOoCount() {
        return sInstance.mElements.size();
    }

    /**
     * Dispose the vector used.
     *
     */
    public static void dispose() {
        sInstance.mListeners.clear();
        sInstance.mElements.clear();
    }

    /**
     * Loads the LibreOffice already configured instances from the preferences.
     */
    public static void load() {

        IOOo[] ooos = PropertiesManager.loadOOos();
        for (int i = 0; i < ooos.length; i++) {
            addOOo(ooos[i]);
        }
    }

    /**
     * Saves the LibreOffice already configured instances to the preferences.
     */
    public static void saveOOos() {

        // Saving the new OOos
        Vector<IOOo> vElements = toVector();
        IOOo[] ooos = new IOOo[getOOoCount()];

        for (int i = 0, length = getOOoCount(); i < length; i++) {
            ooos[i] = vElements.get(i);
        }

        // clean vector
        vElements.clear();

        PropertiesManager.saveOOos(ooos);
    }

    /**
     * @return the OOoContainer singleton instance
     */
    public static OOoContainer getInstance() {
        if (sInstance == null) {
            sInstance = new OOoContainer();
        }
        return sInstance;
    }

    /**
     * Returns a unordered vector with the hash map of the elements.
     *
     * @return vector where the elements order isn't guaranteed
     */
    private static Vector<IOOo> toVector() {
        Vector<IOOo> result = new Vector<IOOo>();
        Set<Entry<String, IOOo>> entries = sInstance.mElements.entrySet();
        Iterator<Entry<String, IOOo>> iter = entries.iterator();

        while (iter.hasNext()) {
            IOOo value = iter.next().getValue();
            result.add(value);
        }
        return result;
    }
}
