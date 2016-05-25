/*************************************************************************
 *
 * $RCSfile: TypesGetter.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2009/04/20 06:16:00 $
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
package org.libreoffice.ide.eclipse.core.internal.office;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.unotypebrowser.InternalUnoType;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.reflection.TypeDescriptionSearchDepth;
import com.sun.star.reflection.XTypeDescription;
import com.sun.star.reflection.XTypeDescriptionEnumeration;
import com.sun.star.reflection.XTypeDescriptionEnumerationAccess;
import com.sun.star.registry.XSimpleRegistry;
import com.sun.star.uno.TypeClass;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Class extracting the UNO types from a selected office instance.
*/
public class TypesGetter {

    private static final int TYPES_MAX = 1023;

    private static final Map<Integer, TypeClass> TYPES_MAPPING = new HashMap<>();

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

    private OfficeConnection mConnection;

    private List<String> mLocalRegs = new LinkedList<String>();
    private List<String> mExternalRegs = new LinkedList<String>();
    private String mRoot;
    private int mMask;
    private TypeClass[] mTypeClasses;

    /**
     * Set the office connection to use to get the types.
     *
     * @param pConnection
     *            the office connection to use
     */
    public void setConnection(OfficeConnection pConnection) {
        mConnection = pConnection;
    }

    /**
     * @param pLocalRegs
     *            the local registries to search. The path has to be in written in an OS dependent form. This path is
     *            converted into an OOo valid URL just before getting the types from it.
     */
    public void setLocalRegs(List<String> pLocalRegs) {
        mLocalRegs.clear();
        mLocalRegs.addAll(pLocalRegs);
    }

    /**
     * @param pExternalRegs
     *            the external registries to search. The path has to be in written in an OS dependent form. This path is
     *            converted into an OOo valid URL just before getting the types from it.
     */
    public void setExternalRegs(List<String> pExternalRegs) {
        mExternalRegs.clear();
        mExternalRegs.addAll(pExternalRegs);
    }

    /**
     * Get the UNO types from the defined registries.
     *
     * Only the types under the given root and corresponding to the types defined by the mask will be extracted.
     *
     * @param pRoot
     *            the root registry key where to look for the types. If the value is <code>null</code> the whole
     *            registry will be searched
     * @param pMask
     *            the bit-ORed types to search. The types are defined in the {@link IUnoFactoryConstants} class.
     *
     * @return the UNO types corresponding to the request.
     *
     * @throws Throwable
     *             if anything wrong happens
     */
    public Map<String, List<InternalUnoType>> getTypes(String pRoot, Integer pMask) throws Throwable {
        Map<String, List<InternalUnoType>> types = new HashMap<>();

        mConnection.startOffice();

        initialize(pRoot, pMask);
        types = queryTypes();

        mConnection.stopOffice();

        return types;
    }

    /**
     * Method called to initialize the types getter before querying the types.
     *
     * @param pRoot
     *            the registries root key
     * @param pTypesMask
     *            the types mask for the types to query
     */
    private void initialize(String pRoot, int pTypesMask) {

        // Sets the root to a quite correct value
        if (pRoot == null || pRoot.equals("/")) { //$NON-NLS-1$
            mRoot = ""; //$NON-NLS-1$
        } else {
            mRoot = pRoot;
        }

        // Sets the typesMask
        if (pTypesMask >= TYPES_MAX + 1) {
            pTypesMask = TYPES_MAX;
        }
        if (pTypesMask >= 0 && TYPES_MAX + 1 > pTypesMask) {
            mMask = pTypesMask;
            mTypeClasses = convertToTypeClasses();
        }
    }

    /**
     * Query the types and return them in a map..
     *
     * <p>
     * The types are mapped to an identifier indicating where they have been extracted from. For a LibreOffice instance,
     * the key in the map will be the OOo name. For any external registry, the key in the map will be the OS specific
     * path to the registry.
     * </p>
     *
     * @return the types
     *
     * @throws Exception
     *             if anything wrong happens
     */
    private Map<String, List<InternalUnoType>> queryTypes() throws Exception {

        Map<String, List<InternalUnoType>> results = new HashMap<>();

        for (int i = 0, length = mLocalRegs.size(); i < length; i++) {
            String path = mLocalRegs.get(i);
            String url = mConnection.convertToUrl(path);
            if (url != null) {
                results.put(path, getTypesFromRegistry(url, true));
            }
        }

        for (int i = 0, length = mExternalRegs.size(); i < length; i++) {
            String path = mExternalRegs.get(i);
            String url = mConnection.convertToUrl(path);
            if (url != null) {
                String oooKey = mConnection.getOOo().toString();
                List<InternalUnoType> types = results.get(oooKey);
                if (types == null) {
                    types = new ArrayList<InternalUnoType>();
                    results.put(oooKey, types);
                }
                types.addAll(getTypesFromRegistry(url, false));
            }
        }

        return results;
    }

    /**
     * Get all the types from a registry and return an {@link InternalUnoType} vector.
     *
     * @param pRegistryPath
     *            the path to the types registry from which to extract the types.
     * @param pIsLocal
     *            <code>true</code> if the types registry is local to the project, <code>false</code> otherwise.
     *
     * @return the types from the registry
     *
     * @throws Exception
     *             is thrown if the registry reading fails
     */
    private LinkedList<InternalUnoType> getTypesFromRegistry(String pRegistryPath, boolean pIsLocal) throws Exception {

        LinkedList<InternalUnoType> result = new LinkedList<InternalUnoType>();

        if (null != pRegistryPath && pRegistryPath.startsWith("file:///")) { //$NON-NLS-1$

            // Get the UNO Type enumeration access
            XComponentContext xCtx = mConnection.getContext();
            XMultiComponentFactory xMCF = xCtx.getServiceManager();
            XSimpleRegistry xReg = UnoRuntime.queryInterface(XSimpleRegistry.class,
                xMCF.createInstanceWithContext("com.sun.star.registry.SimpleRegistry", xCtx)); //$NON-NLS-1$

            xReg.open(pRegistryPath, true, false);

            Object[] seqArgs = { xReg };

            Object oTDMgr = xMCF.createInstanceWithArgumentsAndContext(
                "com.sun.star.reflection.TypeDescriptionProvider", //$NON-NLS-1$
                seqArgs, xCtx);

            // Set the local Type Description Manager
            XTypeDescriptionEnumerationAccess localTDMgr = UnoRuntime
                .queryInterface(XTypeDescriptionEnumerationAccess.class, oTDMgr);

            // Query the types from the enumeration access
            XTypeDescriptionEnumeration xLocalTypeEnum = localTDMgr.createTypeDescriptionEnumeration(mRoot,
                mTypeClasses, TypeDescriptionSearchDepth.INFINITE);

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
     * @param pMask
     *            the mask to check
     * @param pType
     *            the type to find in the mask
     *
     * @return <code>true</code> if the mask contains the type, <code>false</code> otherwise.
     */
    private boolean isOfType(int pMask, int pType) {
        return (pMask & pType) != 0;
    }

    /**
     * Convenient method to convert the types mask into an array of UNO TypeClasses.
     *
     * @return the corresponding TypeClass array
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
     * Add the <code>TypeClass</code> corresponding to the given type if it is present in the getter types mask.
     *
     * @param pType
     *            the type to add from {@link IUnoFactoryConstants}
     * @param pTypeClasses
     *            the type classes list.
     */
    private void tryAddingType(int pType, Vector<TypeClass> pTypeClasses) {
        if (isOfType(mMask, pType)) {
            pTypeClasses.add(TYPES_MAPPING.get(pType));
        }
    }

    /**
     * Creates an {@link InternalUnoType} from the UNO TypeDescription and a flag to know whether the type is local or
     * external.
     *
     * <p>
     * Note: this method isn't very useful yet, but it prepares future evolutions currently impossible.
     * </p>
     *
     * @param pType
     *            the type description to convert to an {@link InternalUnoType}
     * @param pIsLocal
     *            <code>true</code> if the file is local to the project, <code>false</code> otherwise.
     *
     * @return the created {@link InternalUnoType}
     */
    private InternalUnoType createInternalType(XTypeDescription pType, boolean pIsLocal) {

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
}
