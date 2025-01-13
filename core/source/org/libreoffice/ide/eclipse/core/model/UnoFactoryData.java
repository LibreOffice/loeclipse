/*************************************************************************
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
import java.util.Map;
import java.util.Vector;

/**
 * This class contains the data describing a the object to create by the Uno factory.
 */
public class UnoFactoryData {

    private Map<String, Object> mProperties = new HashMap<>();
    private Vector<UnoFactoryData> mInnerData = new Vector<UnoFactoryData>();

    /**
     * Add or replace the property value associated with the key. Nothing happens if the key is <code>null</code> or an
     * empty string.
     *
     * @param key
     *            the name of the property
     * @param value
     *            the value of the property
     */
    public void setProperty(String key, Object value) {
        if (key != null && !key.equals("")) { //$NON-NLS-1$
            mProperties.put(key, value);
        }
    }

    /**
     * @param key
     *            the key of the property to get.
     *
     * @return the property corresponding to the key or <code>null</code> if the key is null or an empty string or if
     *         there is such a key.
     */
    public Object getProperty(String key) {
        Object result = null;
        if (key != null && !key.equals("")) { //$NON-NLS-1$
            result = mProperties.get(key);
        }
        return result;
    }

    /**
     * @return an array of all the contained property keys
     */
    public String[] getKeys() {
        Object[] aKeys = mProperties.keySet().toArray();
        String[] sKeys = new String[aKeys.length];
        for (int i = 0; i < aKeys.length; i++) {
            sKeys[i] = (String) aKeys[i];
        }
        return sKeys;
    }

    /**
     * @return an array of all the data contained by this data.
     */
    public UnoFactoryData[] getInnerData() {
        UnoFactoryData[] data = new UnoFactoryData[mInnerData.size()];
        for (int i = 0, length = mInnerData.size(); i < length; i++) {
            data[i] = mInnerData.get(i);
        }
        return data;
    }

    /**
     * Adds an inner data if it is neither <code>null</code> nor already present in the inner data.
     *
     * @param data
     *            the data to add
     */
    public void addInnerData(UnoFactoryData data) {
        if (data != null && !mInnerData.contains(data)) {
            mInnerData.add(data);
        }
    }

    /**
     * Removes an inner data if it isn't <code>null</code> and already present in the inner data.
     *
     * @param data
     *            the data to remove
     */
    public void removeInnerData(UnoFactoryData data) {
        if (data != null && mInnerData.contains(data)) {
            mInnerData.remove(data);
        }
    }

    /**
     * Destroy the data content before being garbage collected.
     */
    public void dispose() {

        for (int i = 0, length = mInnerData.size(); i < length; i++) {
            mInnerData.get(i).dispose();
        }
        mInnerData.clear();
        mInnerData = null;

        mProperties.clear();
        mProperties = null;
    }
}
