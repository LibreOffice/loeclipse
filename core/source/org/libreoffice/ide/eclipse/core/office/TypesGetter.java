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
package org.libreoffice.ide.eclipse.core.office;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.unotypebrowser.InternalUnoType;

/**
 * Facade class loading all the methods using a LibreOffice instance.
 */
public class TypesGetter {

    private static final String CLASSNAME = OfficeHelper.OOO_PACKAGE + ".TypesGetter"; //$NON-NLS-1$

    private IOOo mOOo;

    private List<String> mLocalRegs;

    /**
     * Set the LibreOffice instance to use for the different operations.
     *
     * @param ooo
     *            the LibreOffice instance to set.
     */
    public void setOOo(IOOo ooo) {
        mOOo = ooo;
    }

    /**
     * @return the LibreOffice to use for the different operations.
     */
    public IOOo getOOo() {
        return mOOo;
    }

    /**
     * @param localRegs
     *            the local registries to search
     */
    public void setLocalRegs(List<String> localRegs) {
        mLocalRegs = localRegs;
    }

    /**
     * Get the UNO types from an office instance.
     *
     * @param root
     *            the root registry key where to look for the types. If the value is <code>null</code> the whole
     *            registry will be searched
     * @param mask
     *            the bit-ORed types to search. The types are defined in the {@link IUnoFactoryConstants} class.
     *
     * @return the list of types available in the office
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<InternalUnoType>> getTypes(String root, int mask) {
        Map<String, List<InternalUnoType>> types = new HashMap<>();

        try {
            // Load the target class and create the getter instance
            OfficeClassLoader oooClassLoader = OfficeClassLoader.getClassLoader(getOOo(),
                TypesGetter.class.getClassLoader());
            Class<?> clazz = oooClassLoader.loadClass(CLASSNAME);
            Object getter = clazz.getDeclaredConstructor().newInstance();

            // Set the office
            Object oooCnx = OfficeHelper.createConnection(oooClassLoader, getOOo());
            String cnxClassName = OfficeHelper.CLASS_CONNECTION;
            Class<?> cnxClazz = oooClassLoader.loadClass(cnxClassName);

            Method oooSetter = clazz.getMethod("setConnection", cnxClazz); //$NON-NLS-1$
            oooSetter.invoke(getter, oooCnx);

            // Set the project registries if defined
            if (mLocalRegs != null && mLocalRegs.size() > 0) {
                Method localRegsSet = clazz.getMethod("setLocalRegs", List.class); //$NON-NLS-1$
                localRegsSet.invoke(getter, mLocalRegs);
            }

            // Set the Office registries
            String[] paths = mOOo.getTypesPath();
            List<String> extRegs = Arrays.asList(paths);
            Method extRegsSet = clazz.getMethod("setExternalRegs", List.class); //$NON-NLS-1$
            extRegsSet.invoke(getter, extRegs);

            // Get the types
            Method method = clazz.getMethod("getTypes", String.class, Integer.class); //$NON-NLS-1$
            Object result = method.invoke(getter, root, Integer.valueOf(mask));

            types = (Map<String, List<InternalUnoType>>) result;

        } catch (Throwable e) {
            PluginLogger.error(Messages.getString("TypesGetter.ERROR"), e.getCause()); //$NON-NLS-1$
        }

        return types;
    }
}
