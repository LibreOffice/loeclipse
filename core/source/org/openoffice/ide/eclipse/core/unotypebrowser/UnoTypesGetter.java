/*************************************************************************
 *
 * $RCSfile: UnoTypesGetter.java,v $
 *
 * $Revision: 1.7 $
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;

import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.loader.FactoryHelper;
import com.sun.star.lang.XMain;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.reflection.TypeDescriptionSearchDepth;
import com.sun.star.reflection.XTypeDescription;
import com.sun.star.reflection.XTypeDescriptionEnumeration;
import com.sun.star.reflection.XTypeDescriptionEnumerationAccess;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.registry.XSimpleRegistry;
import com.sun.star.uno.TypeClass;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Class bootstrapping OpenOffice.org to query its types. This class can't
 * be used directly and should be launched as an external program by the plugin.
 * 
 * @author cedricbosdo
 */
public class UnoTypesGetter implements XMain {

    private static final String SERVICE_NAME = "org.openoffice.ide.eclipse.unotypebrowser.UnoTypesGetter"; //$NON-NLS-1$
    
    private static final int TYPES_MAX = 1023;
    
    private static final HashMap<Integer, TypeClass> TYPES_MAPPING = new HashMap<Integer, TypeClass>();
    
    static {
        TYPES_MAPPING.put(IUnoFactoryConstants.MODULE, TypeClass.MODULE);
        TYPES_MAPPING.put(IUnoFactoryConstants.INTERFACE, TypeClass.INTERFACE);
        TYPES_MAPPING.put(IUnoFactoryConstants.SERVICE, TypeClass.SERVICE);
        TYPES_MAPPING.put(IUnoFactoryConstants.STRUCT, TypeClass.STRUCT);
        TYPES_MAPPING.put(IUnoFactoryConstants.ENUM, TypeClass.ENUM);
        TYPES_MAPPING.put(IUnoFactoryConstants.EXCEPTION, TypeClass.EXCEPTION);
        TYPES_MAPPING.put(IUnoFactoryConstants.TYPEDEF, TypeClass.TYPEDEF);
        TYPES_MAPPING.put(IUnoFactoryConstants.CONSTANT, TypeClass.CONSTANT);
        TYPES_MAPPING.put(IUnoFactoryConstants.CONSTANTS, TypeClass.CONSTANTS);
        TYPES_MAPPING.put(IUnoFactoryConstants.SINGLETON, TypeClass.SINGLETON);
    }
    
    
    private String mRoot;
    private Vector<String> mLocalRegistries;
    private Vector<String> mExternalRegistries;
    private int mTypesMask = TYPES_MAX;
    private TypeClass[] mTypeClasses;
    
    private XComponentContext mCtx;
    
    /**
     * Default constructor used by the URE.
     * 
     * @param pCtx the Component context of the running URE application.
     */
    public UnoTypesGetter(XComponentContext pCtx) {
        this.mCtx = pCtx;
    }
    
    /**
     * Hook launched when this class is used as URE starter component.
     * 
     * @param pArgs Arguments given to fetch the types
     * 
     * @return 0 if the program has completed successfully, 1 otherwise.
     * 
     * @see #execute(String[]) for the method doing the real job
     */
    public int run(String[] pArgs) {
        
        int error = 0;
        
        try {
            execute(pArgs);
        } catch (Exception e) {
            error = 1;
        }

        return error;
    }
    
    /**
     * Hook for used when launched as a normal Java application.
     *
     * @param pArgs Arguments given to fetch the types
     * 
     * @see #execute(String[]) for the method doing the real job
     */
    public static void main(String[] pArgs) {
    
        int error = 0;
        
        try {
            XComponentContext xCtx = bootstrap();
            UnoTypesGetter getter = new UnoTypesGetter(xCtx);
            
            getter.execute(pArgs);
        } catch (Exception e) {
            error = 1;
        }
        
        System.exit(error);
    }

    /**
     * Bootstraps OOo before getting its types.
     *
     * @return the context of the bootstrapped OOo.
     *
     * @throws Exception when something wrong happened
     */
    private static XComponentContext bootstrap() throws Exception {

        return Bootstrap.bootstrap();
    }
    
    /**
     * Common function used either by URE run and Java main callbacks.
     * 
     * @param pArgs arguments given to fetch the types
     *    <ul>
     *      <li>-Lfile:///path/to/a/local/registry</li>
     *      <li>-Efile:///path/to/an/external/registry</li>
     *      <li>-B<code>root</code></li>
     *      <li>-T<code>mask</code> where <code>mask</code> is the integer type mask
     *          to apply.</li>
     *    </ul>
     *    <p>If the types' root isn't specified, then all the tree is parsed.</p>
     *
     * @throws Exception is thrown when something happened during types querying.
     */
    public void execute(String[] pArgs) throws Exception {
        if (1 < pArgs.length) {

            Vector<String> localRegistries = new Vector<String>();
            Vector<String> externalRegistries = new Vector<String>();
            mTypesMask = TYPES_MAX;
            String root = ""; //$NON-NLS-1$
            
            // Gets the optional arguments that defines the types to search.
            for (int i = 0, length = pArgs.length; i < length; i++) {

                if (pArgs[i].startsWith("-L")) { //$NON-NLS-1$
                    // Local registry option
                    String localregistry = pArgs[i].substring(2);
                    
                    // First, test if the file exists...
                    if (localregistry.startsWith("file:///")) { //$NON-NLS-1$
                        String path = localregistry.replace("%20", " "); //$NON-NLS-1$ //$NON-NLS-2$
                        path = path.substring("file:///".length()); //$NON-NLS-1$
                        
                        localRegistries.add(pArgs[i].substring(2));
                    }
                    
                } else if (pArgs[i].startsWith("-E")) { //$NON-NLS-1$
                    // External registry option
                    externalRegistries.add(pArgs[i].substring(2));
                    
                } else if (pArgs[i].startsWith("-B")) { //$NON-NLS-1$
                    // Root name option
                    root = pArgs[i].substring(2);
                    
                } else if (pArgs[i].startsWith("-T")) { //$NON-NLS-1$
                    mTypesMask = Integer.parseInt(pArgs[i].substring(2));
                }
            }
            
            if ((localRegistries.size() + externalRegistries.size()) > 0) {
                initialize(localRegistries, externalRegistries, root, mTypesMask);

                Vector<InternalUnoType> unoTypes = queryTypes();
                printTypes(unoTypes);
                
                unoTypes.clear();
            }
            
            localRegistries.clear();
            externalRegistries.clear();
        }
    }
    
    /**
     * Method called to initialize the types getter before querying the types.
     * 
     * @param pLocalRegistries the local registries to set for reading
     * @param pExternalRegistries the external registries to set for reading
     * @param pRoot the registries root key
     * @param pTypesMask the types mask for the types to query
     */
    public void initialize (Vector<String> pLocalRegistries, 
            Vector<String> pExternalRegistries, String pRoot, int pTypesMask) {
        
        mLocalRegistries = pLocalRegistries;
        mExternalRegistries = pExternalRegistries;
        
        // Sets the root to a quite correct value
        if (pRoot.equals("/")) { //$NON-NLS-1$
            mRoot = ""; //$NON-NLS-1$
        } else {
            mRoot = pRoot;
        }
        
        // Sets the typesMask
        if (pTypesMask >= TYPES_MAX + 1) {
            pTypesMask = TYPES_MAX;
        }
        if (pTypesMask >= 0 && (TYPES_MAX + 1) > pTypesMask) {            
            mTypesMask = pTypesMask;
            mTypeClasses = convertToTypeClasses();
        }
    }

    /**
     * Query the types and return them in a vector of {@link InternalUnoType}.
     * 
     * @return the types
     * 
     * @throws Exception if anywrong happens
     */
    protected Vector<InternalUnoType> queryTypes() throws Exception {
            
        Vector<InternalUnoType> results = new Vector<InternalUnoType>();
        
        for (int i = 0, length = mLocalRegistries.size(); i < length; i++) {
            String registryPath = mLocalRegistries.get(i);
            results.addAll(getTypesFromRegistry(registryPath, true));
        }

        for (int i = 0, length = mExternalRegistries.size(); i < length; i++) {
            String registryPath = mExternalRegistries.get(i);
            results.addAll(getTypesFromRegistry(registryPath, true));
        }

        return results;
    }

    /**
     * Get all the types from a registry and return an {@link InternalUnoType}
     * vector.
     * 
     * @param pRegistryPath the path to the types registry from which to extract the types.
     * @param pIsLocal <code>true</code> if the types registry is local to the project,
     *      <code>false</code> otherwise.
     * 
     * @return the types from the registry
     * 
     * @throws Exception is thrown if the registry reading fails
     */
    private Vector<InternalUnoType> getTypesFromRegistry(String pRegistryPath, 
            boolean pIsLocal) throws Exception {
        
        Vector<InternalUnoType> result = new Vector<InternalUnoType>();
                
        if (null != pRegistryPath && pRegistryPath.startsWith("file:///")) { //$NON-NLS-1$
                
            // Get the UNO Type enumeration access    
            XMultiComponentFactory xMCF = mCtx.getServiceManager();
            XSimpleRegistry xReg = (XSimpleRegistry)UnoRuntime.queryInterface(
                    XSimpleRegistry.class,
                    xMCF.createInstanceWithContext(
                        "com.sun.star.registry.SimpleRegistry", mCtx)); //$NON-NLS-1$
                
            xReg.open(pRegistryPath, true, false);
            
            Object[] seqArgs = { xReg };
                
            Object oTDMgr = xMCF.createInstanceWithArgumentsAndContext(
                    "com.sun.star.reflection.TypeDescriptionProvider", //$NON-NLS-1$
                    seqArgs, mCtx);
                
            // Set the local Type Description Manager
            XTypeDescriptionEnumerationAccess localTDMgr = 
                (XTypeDescriptionEnumerationAccess)UnoRuntime.queryInterface(
                    XTypeDescriptionEnumerationAccess.class,
                    oTDMgr);

            // Query the types from the enumeration access
            XTypeDescriptionEnumeration xLocalTypeEnum = localTDMgr.
                    createTypeDescriptionEnumeration(
                            mRoot,
                            mTypeClasses,
                            TypeDescriptionSearchDepth.INFINITE);

            // Convert the enumeration into a Vector
            while (xLocalTypeEnum.hasMoreElements()) {

                XTypeDescription xType = xLocalTypeEnum.nextTypeDescription();
                result.add(createInternalType(xType, pIsLocal));
            }
        }
       
        return result;
    }
    
    /**
     * Convenient method to check if the mask includes a type.
     *
     * @param pMask the mask to check
     * @param pType the type to find in the mask
     *
     * @return <code>true</code> if the mask contains the type, 
     *      <code>false</code> otherwise.
     */
    private boolean isOfType(int pMask, int pType) {
        return (pMask & pType) != 0;
    }
    
    /**
     *  Convenient method to convert the types mask into an array of UNO 
     *  TypeClasses.
     *
     *  @return the corresponding TypeClass array
     */
    private TypeClass[] convertToTypeClasses() {
            
        // Creates the TypeClass[] array from the given types names
        Vector<TypeClass> typeClasses = new Vector<TypeClass>();
        
        tryAddingType(IUnoFactoryConstants.MODULE, typeClasses);
        tryAddingType(IUnoFactoryConstants.INTERFACE, typeClasses);
        tryAddingType(IUnoFactoryConstants.SERVICE, typeClasses);
        tryAddingType(IUnoFactoryConstants.STRUCT, typeClasses);
        tryAddingType(IUnoFactoryConstants.ENUM, typeClasses);
        tryAddingType(IUnoFactoryConstants.EXCEPTION, typeClasses);
        tryAddingType(IUnoFactoryConstants.TYPEDEF, typeClasses);
        tryAddingType(IUnoFactoryConstants.CONSTANT, typeClasses);
        tryAddingType(IUnoFactoryConstants.CONSTANTS, typeClasses);
        tryAddingType(IUnoFactoryConstants.SINGLETON, typeClasses);
        
        TypeClass[] types = typeClasses.toArray(new TypeClass[typeClasses.size()]);
        typeClasses.clear();

        return types;
    }
   
    /**
     * Add the <code>TypeClass</code> corresponding to the given type if it
     * is present in the getter types mask.
     * 
     * @param pType the type to add from {@link IUnoFactoryConstants}
     * @param pTypeClasses the type classes list.
     */
    private void tryAddingType(int pType, Vector<TypeClass> pTypeClasses) {
        if (isOfType(mTypesMask, pType)) {
            pTypeClasses.add(TYPES_MAPPING.get(pType));
        } 
    }

    /**
     * Creates an {@link InternalUnoType} from the UNO TypeDescription
     * and a flag to know whether the type is local or external.
     *
     * <p>Note: this method isn't very useful yet, but it prepares future
     * evolutions currently impossible.</p>
     * 
     * @param pType the type description to convert to an {@link InternalUnoType}
     * @param pIsLocal <code>true</code> if the file is local to the project, 
     *      <code>false</code> otherwise.
     *      
     * @return the created {@link InternalUnoType}
     */
    private InternalUnoType createInternalType(XTypeDescription pType, 
            boolean pIsLocal) {
        
        // convert the type into an integer
        TypeClass typeClass = pType.getTypeClass();
        int type = 0;
        
        Iterator<Entry<Integer, TypeClass>> iter = TYPES_MAPPING.entrySet().iterator();
        boolean found = false;
        while (iter.hasNext() && !found) {
            Entry<Integer, TypeClass> entry = iter.next();
            if (entry.getValue().equals(typeClass)) {
                type = entry.getKey().intValue();
                found = true;
            }
        }
            
        return new InternalUnoType(pType.getName(), type, pIsLocal);
    }
    
    /**
     * Prints the vector to the standard output: thus the types can be given to
     * another process using simple parsing.
     *
     * @param pUnoTypes vector of InternalUnoTypes to print
     */
    private void printTypes(Vector<InternalUnoType> pUnoTypes) {
        for (int i = 0, length = pUnoTypes.size(); i < length; i++) {
            InternalUnoType type = pUnoTypes.get(i);

            System.out.println(type.toString());
        }
    }
    
    /**
     * Method used for the UNO service registration.
     * 
     * @param pImplName the implementation name to register
     * @param pMultiFactory the multi-service factory
     * @param pRegKey the registration key to use with the Multi-service factory
     * 
     * @return the service factory
     */
    public static XSingleServiceFactory __getServiceFactory(
            String pImplName, XMultiServiceFactory pMultiFactory, 
            XRegistryKey pRegKey) {
        
        XSingleServiceFactory xSingleServiceFactory = null;
        
        if (pImplName.equals(UnoTypesGetter.class.getName())) {
            xSingleServiceFactory = FactoryHelper.getServiceFactory(
                    UnoTypesGetter.class, UnoTypesGetter.SERVICE_NAME, 
                    pMultiFactory, pRegKey);
        }
        
        return xSingleServiceFactory;
    }
    
    /**
     * Writes the service registry informations.
     * 
     * @param pRegKey the registry key where to add the informations
     * 
     * @return <code>true</code> if the method succeeds, <code>false</code> otherwise.
     */
    public static boolean __writeRegistryServiceInfo(XRegistryKey pRegKey) {
        boolean b = FactoryHelper.writeRegistryServiceInfo(
                UnoTypesGetter.class.getName(), 
                UnoTypesGetter.SERVICE_NAME, pRegKey);
        
        return b;
    }
}
