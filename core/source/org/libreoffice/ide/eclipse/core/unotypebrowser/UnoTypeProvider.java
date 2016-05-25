/*************************************************************************
 *
 * $RCSfile: UnoTypeProvider.java,v $
 *
 * $Revision: 1.14 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2009/04/20 06:16:01 $
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
package org.libreoffice.ide.eclipse.core.unotypebrowser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.office.TypesGetter;

/**
 * Class providing UNO types from a LibreOffice instance and optionally from a UNO project.
*/
public class UnoTypeProvider {

    public static final int ALL_TYPES = 2047;
    public static final String BASIC_TYPES_KEY = "basic-types"; //$NON-NLS-1$

    private static final InternalUnoType[] SIMPLE_TYPES = { InternalUnoType.STRING, InternalUnoType.VOID,
        InternalUnoType.BOOLEAN, InternalUnoType.BYTE, InternalUnoType.SHORT, InternalUnoType.LONG,
        InternalUnoType.HYPER, InternalUnoType.FLOAT, InternalUnoType.DOUBLE, InternalUnoType.CHAR,
        InternalUnoType.TYPE, InternalUnoType.ANY, InternalUnoType.USHORT, InternalUnoType.ULONG,
        InternalUnoType.UHYPER };

    private static UnoTypeProvider sInstance = new UnoTypeProvider();

    private LinkedList<IInitListener> mListeners = new LinkedList<IInitListener>();
    private Map<String, List<InternalUnoType>> mCache;

    private IOOo mOooInstance;
    private String mPathToRegister;

    private TypeProviderState mState = TypeProviderState.EMPTY;

    /**
     * Only to restrict the use of the default constructor: this is a singleton.
     */
    private UnoTypeProvider() {
    }

    /**
     * @return the {@link UnoTypeProvider} singleton instance.
     */
    public static UnoTypeProvider getInstance() {
        return sInstance;
    }

    // ---------------------------------------------------------- Type management

    /**
     * Refresh the cache of Uno types.
     */
    public void refreshCache() {
        if (mOooInstance != null) {
            mState = TypeProviderState.INITIALIZING;

            removeAllTypes();

            // Start getting the types again
            new UnoTypesGetterThread().start();
        }
    }

    /**
     * Checks whether the list contains the given type name.
     *
     * @param pScopedName
     *            the type name to match
     * @param pContainers
     *            the UNO types containers to look in. These have to be either the path to a project RDB file or a
     *            LibreOffice name
     *
     * @return <code>true</code> if the list contains a type with this name
     */
    public boolean contains(String pScopedName, String[] pContainers) {
        boolean result = false;
        pScopedName = pScopedName.replaceAll("::", "."); //$NON-NLS-1$ //$NON-NLS-2$

        if (getState().equals(TypeProviderState.INITIALIZED)) {
            for (String container : pContainers) {
                List<InternalUnoType> types = mCache.get(container);
                if (types != null) {
                    Iterator<InternalUnoType> iter = types.iterator();
                    while (iter.hasNext() && !result) {
                        InternalUnoType type = iter.next();
                        if (type.getFullName().equals(pScopedName)) {
                            result = true;
                        }
                    }
                }
            }
        }

        return result;
    }

    // ------------------------------------------------------- Project management

    /**
     * Set the UNO project for which to get the UNO types. This project's <code>types.rdb</code> registry will be used
     * as external registry for the types query.
     *
     * @param pProject
     *            the project for which to launch the type query
     */
    public void setProject(IUnoidlProject pProject) {

        if (null != pProject) {
            mOooInstance = pProject.getOOo();
            mPathToRegister = pProject.getFile(pProject.getTypesPath()).getLocation().toOSString();

            PluginLogger.debug("UnoTypeProvider initialized with " + pProject); //$NON-NLS-1$
        }
    }

    /**
     * Sets the OOo if the new one is different from the old one.
     *
     * @param pOOoInstance
     *            LibreOffice instance to bootstrap
     */
    public void setOOoInstance(IOOo pOOoInstance) {

        if (null != pOOoInstance && !pOOoInstance.equals(mOooInstance)) {
            mOooInstance = pOOoInstance;
            PluginLogger.debug("UnoTypeProvider initialized with " + pOOoInstance); //$NON-NLS-1$
        }
    }

    /**
     * @return the status of the UNO type provider.
     */
    public TypeProviderState getState() {
        return mState;
    }

    // ---------------------------------------------------- TypeGetter launching

    /**
     * Register the given listener.
     *
     * @param pListener
     *            the listener to add
     */
    public void addInitListener(IInitListener pListener) {
        mListeners.add(pListener);
    }

    /**
     * Makes the given initialization listener stop listening.
     *
     * @param pListener
     *            the listener to remove
     */
    public void removeInitListener(IInitListener pListener) {
        mListeners.remove(pListener);
    }

    /**
     * Propagate the news to the listeners that it has been initialized.
     */
    private void setInitialized() {
        mState = TypeProviderState.INITIALIZED;

        for (int i = 0, length = mListeners.size(); i < length; i++) {
            mListeners.get(i).initialized();
        }
    }

    // --------------------------------------------------- Collection management

    /**
     * Initializes the cache if needed and get the cached data.
     *
     * @param pContainers
     *            the container from which to get the types.
     *
     * @return the types list as an array.
     *
     * @see org.libreoffice.ide.eclipse.core.internal.office.TypesGetter
     */
    protected Object[] toArray(String[] pContainers) {
        // Fill in the cache if necessary
        if (mCache == null) {
            refreshCache();
        }

        List<String> containers = new ArrayList<String>();
        containers.addAll(Arrays.asList(pContainers));

        // Use the set OOo and project as containers
        if (mPathToRegister != null) {
            containers.add(mPathToRegister);
        }

        if (mOooInstance != null) {
            containers.add(mOooInstance.toString());
        }

        LinkedList<InternalUnoType> types = new LinkedList<InternalUnoType>();
        for (String container : containers) {
            List<InternalUnoType> regTypes = mCache.get(container);
            if (regTypes != null) {
                types.addAll(regTypes);
            }
        }

        return types.toArray();
    }

    /**
     * Purge the types list.
     */
    protected void removeAllTypes() {
        if (mCache != null) {
            mCache.clear();
        } else {
            mCache = new HashMap<>();
        }
    }

    /**
     * The job extracting the types from LibreOffice.
    */
    private class UnoTypesGetterThread extends Thread {

        /**
         * Runs the job.
         */
        @Override
        public void run() {
            try {
                removeAllTypes();

                // Reads the types and add them to the list
                TypesGetter getter = new TypesGetter();
                getter.setOOo(mOooInstance);
                LinkedList<String> localRegs = new LinkedList<String>();
                localRegs.add(mPathToRegister);
                getter.setLocalRegs(localRegs);

                Map<String, List<InternalUnoType>> data = getter.getTypes(null, ALL_TYPES);
                Iterator<String> iter = data.keySet().iterator();
                while (iter.hasNext()) {
                    String key = iter.next();

                    // Clears the cache for the fetched registries
                    List<InternalUnoType> types = mCache.get(key);
                    if (types != null) {
                        types.clear();
                        mCache.remove(key);
                    }

                    // Put the new types
                    mCache.put(key, data.get(key));
                }

                // Add the basic types
                mCache.put(BASIC_TYPES_KEY, Arrays.asList(SIMPLE_TYPES));

                setInitialized();
                PluginLogger.debug("Types fetched"); //$NON-NLS-1$
            } catch (Exception e) {
                PluginLogger.error(Messages.getString("UnoTypeProvider.UnexpectedError"), e); //$NON-NLS-1$
            }
        }
    }
}
