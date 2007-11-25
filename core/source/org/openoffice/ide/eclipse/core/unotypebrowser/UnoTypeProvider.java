/*************************************************************************
 *
 * $RCSfile: UnoTypeProvider.java,v $
 *
 * $Revision: 1.12 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:27 $
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.model.OOo;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.preferences.IOOo;

/**
 * Class providing UNO types from an OpenOffice.org instance and optionally
 * from a UNO project.
 * 
 * @author cedricbosdo
 *
 */
public class UnoTypeProvider {
    
    private static UnoTypeProvider sInstance = new UnoTypeProvider();
    
    private static final int[] ALLOWED_TYPES = {
        IUnoFactoryConstants.BASICS,
        IUnoFactoryConstants.MODULE,
        IUnoFactoryConstants.INTERFACE,
        IUnoFactoryConstants.SERVICE,
        IUnoFactoryConstants.STRUCT,
        IUnoFactoryConstants.ENUM,
        IUnoFactoryConstants.EXCEPTION,
        IUnoFactoryConstants.TYPEDEF,
        IUnoFactoryConstants.CONSTANT,
        IUnoFactoryConstants.CONSTANTS,
        IUnoFactoryConstants.SINGLETON
    };

    private static final int ALL_TYPES = 2047;

    private static final int MAX_BITS_LENGTH = 11;
    
    private UnoTypesGetterThread mGetTypesThread = new UnoTypesGetterThread();
    
    private Vector<IInitListener> mListeners = new Vector<IInitListener>(); 
    
    private Vector<InternalUnoType> mInternalTypes = new Vector<InternalUnoType>();
    
    
    private int mTypes = ALL_TYPES;
    
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
    
    /**
     * Initializes the type provider from a UNO project.
     * 
     * @param pProject the UNO project to query (with its OOo parameter)
     * @param pTypes the types to get
     */
    public void initialize(IUnoidlProject pProject, int pTypes) {
        setTypes(pTypes);
        setProject(pProject);
    }
    
    /**
     * Initializes the UNO type provider from an OpenOffice.org instance.
     * 
     * @param pOOoInstance the OOo instance to query
     * @param pTypes the types to get
     */
    public void initialize(IOOo pOOoInstance, int pTypes) {
        setTypes(pTypes);
        setOOoInstance(pOOoInstance);
    }
    
    /**
     * Stop the type provider.
     */
    public void stopProvider() {
        removeAllTypes();
        
        mInternalTypes = null;
        mOooInstance = null;
        mPathToRegister = null;
        
        if (mGetTypesThread != null && mGetTypesThread.isAlive()) {
            // Not sure it stops when running
            mGetTypesThread.shutdown();
            mGetTypesThread = null;
            PluginLogger.debug("UnoTypeProvider stopped"); //$NON-NLS-1$
        }
    }
    
    //---------------------------------------------------------- Type management
    
    /**
     * Method changing all the '1' into '0' and the '0' into '1' but only
     * on the interesting bytes for the types.
     * 
     * @param pType the type to negate
     * @return the negated type
     */
    public static int invertTypeBits(int pType) {
        int result = 0;
        
        String sInv = Integer.toBinaryString(pType);
        int length = ALLOWED_TYPES.length - sInv.length();
        
        if (length <= MAX_BITS_LENGTH) {
            
            for (int i = 0; i < length; i++) {
                sInv = '0' + sInv;
            }
            
            sInv = sInv.replace('0', '2').replace('1', '0');
            sInv = sInv.replace('2', '1');
            result = Integer.parseInt(sInv, 2);
        }
        
        return result;
    }
    
    /**
     * Set one or more types. To specify more than one types give the bit or
     * of all the types, e.g. <code>INTERFACE | SERVICE</code>
     * 
     * @param pTypes the bit or of the types
     */
    public void setTypes(int pTypes) {
        
        // Only 10 bits available
        if (pTypes >= 0 && pTypes <= InternalUnoType.ALL_TYPES) {
            if (mTypes != pTypes) {
                mTypes = pTypes;
                IOOo ooo = mOooInstance;
                mOooInstance = null;
                setOOoInstance(ooo);
            }
        }
    }
    
    /**
     * @return the types set as an integer. The types field is a bit or of all the
     *          types set.
     *          
     * @see #setTypes(int)
     */
    public int getTypes() {
        return mTypes;
    }
    
    /**
     * Checks if the given type will be queried.
     * 
     * @param pType the type to match
     * @return <code>true</code> if the type is one of the types set
     */
    public boolean isTypeSet(int pType) {
        return (getTypes() & pType) == pType;
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
            Iterator<InternalUnoType> iter = mInternalTypes.iterator();
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
    private void setProject(IUnoidlProject pProject) {
        
        if (null != pProject) {
            
            // Stop the provider before everything
            stopProvider();
            mInternalTypes = new Vector<InternalUnoType>();
            
            mOooInstance = pProject.getOOo();
            mPathToRegister = (pProject.getFile(
                    pProject.getTypesPath()).getLocation()).toOSString();
            
            PluginLogger.debug(
                    "UnoTypeProvider initialized with " + pProject); //$NON-NLS-1$
            
            mInitialized = false;
            askUnoTypes();
        }
    }
    
    /**
     * Sets the OOo if the new one is different from the old one.
     * 
     *  @param pOOoInstance OpenOffice.org instance to bootstrap
     */
    private void setOOoInstance(IOOo pOOoInstance) {
        
        if (null != pOOoInstance && !pOOoInstance.equals(mOooInstance)) {
            
            // Stop the provider before everything
            stopProvider();
            mInternalTypes = new Vector<InternalUnoType>();
            
            mOooInstance = pOOoInstance;
            PluginLogger.debug(
                    "UnoTypeProvider initialized with " + pOOoInstance); //$NON-NLS-1$
            
            mInitialized = false;
            askUnoTypes();
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
     * @return the command to launch to get all the types.
     * 
     * @throws IOException is thrown if the application fails.
     */
    private String computeGetterCommand() throws IOException {
        String command = null;
        
        if (null != mOooInstance) {
            // Compute the library location (UnoTypesGetter.jar file)
            URL pluginURL = OOEclipsePlugin.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
            // NOTE not replaced by FileLocator to avoid dependency on Eclipse 3.2 
            URL libURL = FileLocator.toFileURL(pluginURL);

            // Compute the types mask argument
            String typesMask = "-T" + mTypes; //$NON-NLS-1$
            
            // Get the OOo types.rdb registry path as external registry
            String typesPath = new Path(mOooInstance.getTypesPath()).toString();
            typesPath = "-Efile:///" + typesPath.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            
            // Add the local registry path
            String localRegistryPath = ""; //$NON-NLS-1$
            // If the path to the registry isn't set, don't take
            // it into account in the command build
            if (null != mPathToRegister) {
                localRegistryPath = " -Lfile:///" +  //$NON-NLS-1$
                    mPathToRegister.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
                localRegistryPath = localRegistryPath.replace('\\', '/');
            }
            
            // compute the arguments array
            String[] args = new String[] {
                typesPath,
                localRegistryPath,
                typesMask
            };
            
            // Computes the command to execute if oooInstance isn't the URE
            if (mOooInstance instanceof OOo) {
                
                String libPath = new Path(libURL.getPath()).toOSString();
                libPath = libPath + "UnoTypesGetter.jar"; //$NON-NLS-1$
                
                command = mOooInstance.createUnoCommand(
                        "org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypesGetter",  //$NON-NLS-1$
                        libPath, new String[0], args);
            } else {

                String libPath = new Path(libURL.getPath()).toString();
                libPath = libPath + "UnoTypesGetter.jar"; //$NON-NLS-1$
                libPath = libPath.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
                
                command = mOooInstance.createUnoCommand(
                        "org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypesGetter",  //$NON-NLS-1$
                        "file:///" + libPath, new String[]{}, args); //$NON-NLS-1$
            }
        }
        return command;
    }
    
    /**
     * Launches the UNO type query process.
     */
    private void askUnoTypes() {
        
        if (null == mGetTypesThread || !mGetTypesThread.isAlive()) {
            
            mInternalTypes = new Vector<InternalUnoType>();
            
            mGetTypesThread = new UnoTypesGetterThread();
            mGetTypesThread.start();
        }
    }
    
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
        
        Iterator<InternalUnoType> iter = mInternalTypes.iterator();
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
     * @return the types list as an array.
     */
    protected Object[] toArray() {
        Object[] types = new Object[0];
        if (mInternalTypes != null) {
            types = mInternalTypes.toArray();
        }
        return types;
    }
    
    /**
     * Add a type to the list.
     * 
     * @param pInternalType the type to add
     */
    protected void addType(InternalUnoType pInternalType) {
        mInternalTypes.add(pInternalType);
    }

    /**
     * Purge the types list.
     */
    protected void removeAllTypes() {
        if (mInternalTypes != null) {
            mInternalTypes.clear();
        }
    }
    
    /**
     * The job extracting the types from OpenOffice.org.
     * 
     * @author cedricbosdo
     *
     */
    private class UnoTypesGetterThread extends Thread {

        private Process mProcess;
        private boolean mStop = false;
        
        /**
         * Stops the process.
         */
        public void shutdown() {
            if (mProcess != null) {
                mProcess.destroy();
            }
            mProcess = null;
            mStop = true;
        }

        /**
         * Runs the job.
         */
        public void run() {
            try {
                removeAllTypes();
                String command = computeGetterCommand();

                // Computes the environment variables

                mProcess = Runtime.getRuntime().exec(command);

                if (!mStop) {
                    // Reads the types and add them to the list
                    InputStreamReader in = new InputStreamReader(mProcess.getInputStream());
                    LineNumberReader reader = new LineNumberReader(in);

                    try {
                        String line = reader.readLine();

                        while (null != line) {
                            InternalUnoType internalType = new InternalUnoType(line);
                            addType(internalType);
                            line = reader.readLine();
                        }
                    } finally {
                        reader.close();
                        in.close();
                    }
                    setInitialized();
                    PluginLogger.debug("Types fetched"); //$NON-NLS-1$
                }

            } catch (IOException e) {                
                PluginLogger.error(Messages.getString("UnoTypeProvider.IOError"), e); //$NON-NLS-1$
            } catch (Exception e) {
                PluginLogger.error(Messages.getString("UnoTypeProvider.UnexpectedError"), e); //$NON-NLS-1$
            }
        }
    }
}
