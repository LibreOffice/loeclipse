/*************************************************************************
 *
 * $RCSfile: UnoTypeProvider.java,v $
 *
 * $Revision: 1.13 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:49 $
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
package org.openoffice.ide.eclipse.core.unotypebrowser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.office.TypesGetter;
import org.openoffice.ide.eclipse.core.preferences.IOOo;

/**
 * Class providing UNO types from an OpenOffice.org instance and optionally
 * from a UNO project.
 * 
 * @author cedricbosdo
 *
 */
public class UnoTypeProvider {
    
    public static final int ALL_TYPES = 2047;
    
    private static UnoTypeProvider sInstance = new UnoTypeProvider();
    
    private LinkedList<IInitListener> mListeners = new LinkedList<IInitListener>(); 
    private List<InternalUnoType> mCache;
    
    private IOOo mOooInstance;
    private String mPathToRegister;
    
    private boolean mInitialized = false;
    
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
    
    //---------------------------------------------------------- Type management
    
    /**
     * Refresh the cache of Uno types.
     */
    public void refreshCache() {
        mInitialized = false;

        removeAllTypes();
        
        // Start getting the types again
        new UnoTypesGetterThread().start();
    }
    
    /**
     * Checks whether the list contains the given type name.
     * 
     * @param pScopedName the type name to match
     * @return <code>true</code> if the list contains a type with this name
     */
    public boolean contains(String pScopedName) {
        
        boolean result = false;
        pScopedName = pScopedName.replaceAll("::", "."); //$NON-NLS-1$ //$NON-NLS-2$
        
        if (isInitialized()) {
            Iterator<InternalUnoType> iter = mCache.iterator();
            while (iter.hasNext() && !result) {
                InternalUnoType type = iter.next();
                if (type.getFullName().equals(pScopedName)) {
                    result = true;
                }
            }
        }
        
        return result;
    }
    
    //------------------------------------------------------- Project managment
    
    /**
     * Set the UNO project for which to get the UNO types. This project's
     * <code>types.rdb</code> registry will be used as external registry
     * for the types query.
     * 
     * @param pProject the project for which to launch the type query 
     */
    public void setProject(IUnoidlProject pProject) {
        
        if (null != pProject) {
            mOooInstance = pProject.getOOo();
            mPathToRegister = (pProject.getFile(
                    pProject.getTypesPath()).getLocation()).toOSString();
            
            PluginLogger.debug(
                    "UnoTypeProvider initialized with " + pProject); //$NON-NLS-1$
        }
    }
    
    /**
     * Sets the OOo if the new one is different from the old one.
     * 
     *  @param pOOoInstance OpenOffice.org instance to bootstrap
     */
    public void setOOoInstance(IOOo pOOoInstance) {
        
        if (null != pOOoInstance && !pOOoInstance.equals(mOooInstance)) {
            mOooInstance = pOOoInstance;
            PluginLogger.debug(
                    "UnoTypeProvider initialized with " + pOOoInstance); //$NON-NLS-1$
        }
    }
    
    /**
     * @return whether the type provider has been initialized.
     */
    public boolean isInitialized() {
        return mInitialized;
    }
    
    //---------------------------------------------------- TypeGetter launching
    
    /**
     * Register the given listener.
     * 
     * @param pListener the listener to add
     */
    public void addInitListener(IInitListener pListener) {
        mListeners.add(pListener);
    }
    
    /**
     * Makes the given initialization listener stop listening.
     * 
     * @param pListener the listener to remove
     */
    public void removeInitListener(IInitListener pListener) {
        mListeners.remove(pListener);
    }
    
    /**
     * Propagate the news to the listeners that it has been initialized.
     */
    private void setInitialized() {
        mInitialized = true;
        
        for (int i = 0, length = mListeners.size(); i < length; i++) {
            mListeners.get(i).initialized();
        }
    }

    //--------------------------------------------------- Collection management
    
    /**
     * Get a type from its path.
     * 
     * @param pTypePath the type path
     * 
     * @return the corresponding complete type description
     */
    public InternalUnoType getType(String pTypePath) {
        
        Iterator<InternalUnoType> iter = mCache.iterator();
        InternalUnoType result = null;
        
        while (null == result && iter.hasNext()) {
            InternalUnoType type = iter.next();
            if (type.getFullName().equals(pTypePath)) {
                result = type;
            }
        }
        return result;
        
    }
    
    /**
     * Initializes the cache if needed and get the cached data.
     * 
     * @return the types list as an array.
     */
    protected Object[] toArray() {
        if (mCache == null) {
            refreshCache();
        }
        
        Object[] types = new Object[mCache.size()];
        types = mCache.toArray();
        return types;
    }
    
    /**
     * Add a type to the list.
     * 
     * @param pInternalType the type to add
     */
    protected void addType(InternalUnoType pInternalType) {
        mCache.add(pInternalType);
    }

    /**
     * Purge the types list.
     */
    protected void removeAllTypes() {
        if (mCache != null) {
            mCache.clear();
        } else {
            mCache = new LinkedList<InternalUnoType>();
        }
    }
    
    /**
     * The job extracting the types from OpenOffice.org.
     * 
     * @author cedricbosdo
     *
     */
    private class UnoTypesGetterThread extends Thread {

        /**
         * Runs the job.
         */
        public void run() {
            try {
                removeAllTypes();

                // Reads the types and add them to the list
                TypesGetter getter = new TypesGetter();
                getter.setOOo(mOooInstance);
                LinkedList<String> localRegs = new LinkedList<String>();
                localRegs.add(mPathToRegister);
                getter.setLocalRegs(localRegs);
                
                mCache = getter.getTypes(null, ALL_TYPES);
                
                setInitialized();
                PluginLogger.debug("Types fetched"); //$NON-NLS-1$
            } catch (Exception e) {
                PluginLogger.error(Messages.getString("UnoTypeProvider.UnexpectedError"), e); //$NON-NLS-1$
            }
        }
    }
}
