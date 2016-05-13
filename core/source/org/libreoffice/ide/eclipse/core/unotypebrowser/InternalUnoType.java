/*************************************************************************
 *
 * $RCSfile: InternalUnoType.java,v $
 *
 * $Revision: 1.5 $
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
package org.libreoffice.ide.eclipse.core.unotypebrowser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;

/**
 * Class describing a UNO-Type.
 *
 * Only used with the {@link UnoTypeProvider}. A UNO type is described by its name, a boolean field defining if it's a
 * local type and a path containing the fully qualified name of the type container.
 *
 *
 */
public class InternalUnoType {

    public static final InternalUnoType STRING = new InternalUnoType("string", IUnoFactoryConstants.BASICS, true); //$NON-NLS-1$

    public static final InternalUnoType VOID = new InternalUnoType("void", IUnoFactoryConstants.BASICS, true); //$NON-NLS-1$

    public static final InternalUnoType BOOLEAN = new InternalUnoType("boolean", IUnoFactoryConstants.BASICS, true); //$NON-NLS-1$

    public static final InternalUnoType BYTE = new InternalUnoType("byte", IUnoFactoryConstants.BASICS, true); //$NON-NLS-1$

    public static final InternalUnoType SHORT = new InternalUnoType("short", IUnoFactoryConstants.BASICS, true); //$NON-NLS-1$

    public static final InternalUnoType LONG = new InternalUnoType("long", IUnoFactoryConstants.BASICS, true); //$NON-NLS-1$

    public static final InternalUnoType HYPER = new InternalUnoType("hyper", IUnoFactoryConstants.BASICS, true); //$NON-NLS-1$

    public static final InternalUnoType FLOAT = new InternalUnoType("float", IUnoFactoryConstants.BASICS, true); //$NON-NLS-1$

    public static final InternalUnoType DOUBLE = new InternalUnoType("double", IUnoFactoryConstants.BASICS, true); //$NON-NLS-1$

    public static final InternalUnoType CHAR = new InternalUnoType("char", IUnoFactoryConstants.BASICS, true); //$NON-NLS-1$

    public static final InternalUnoType TYPE = new InternalUnoType("type", IUnoFactoryConstants.BASICS, true); //$NON-NLS-1$

    public static final InternalUnoType ANY = new InternalUnoType("any", IUnoFactoryConstants.BASICS, true); //$NON-NLS-1$

    public static final InternalUnoType USHORT = new InternalUnoType("unsigned short", IUnoFactoryConstants.BASICS, //$NON-NLS-1$
        true);

    public static final InternalUnoType ULONG = new InternalUnoType("unsigned long", IUnoFactoryConstants.BASICS, true); //$NON-NLS-1$

    public static final InternalUnoType UHYPER = new InternalUnoType("unsigned hyper", IUnoFactoryConstants.BASICS, //$NON-NLS-1$
        true);

    public static final int ALL_TYPES = 2047;

    private static final String LOCAL_TAG = "L"; //$NON-NLS-1$
    private static final String EXTERNAL_TAG = "E"; //$NON-NLS-1$

    private static final int TYPE_REGEX_GROUPS = 3;

    private static final int TYPE_VALUE_GROUP = 3;

    private static final int ALL_TYPES_FILTER = 2048;

    private String mPath;
    private int mType;
    private boolean mLocal = false;

    /**
     * Constructor.
     *
     * @param pTypeString
     *            the string representing the type.
     */
    public InternalUnoType(String pTypeString) {
        if (null != pTypeString) {
            Matcher typeMatcher = Pattern.compile("(" + EXTERNAL_TAG + "|" + LOCAL_TAG + //$NON-NLS-1$ //$NON-NLS-2$
                ") ([^\\s]*) ([0-9]+)").matcher(pTypeString); //$NON-NLS-1$
            if (typeMatcher.matches() && TYPE_REGEX_GROUPS == typeMatcher.groupCount()) {
                setLocal(typeMatcher.group(1));
                setType(Integer.parseInt(typeMatcher.group(TYPE_VALUE_GROUP)));
                mPath = typeMatcher.group(2);
            }
        }
    }

    /**
     * Constructor.
     *
     * @param pCompleteName
     *            the type complete name separated with "."
     * @param pType
     *            the UNO type's type
     * @param pIsLocal
     *            <code>true</code> if the type is defined in the project, <code>false</code> if it's defined in an
     *            external <code>types.rdb</code>.
     */
    public InternalUnoType(String pCompleteName, int pType, boolean pIsLocal) {
        mLocal = pIsLocal;
        setType(pType);
        mPath = pCompleteName;
    }

    /**
     * @return the type name, ie <code>XInterface</code> for <code>com.sun.star.uno.XInterface</code>.
     */
    public String getName() {
        String name = ""; //$NON-NLS-1$

        String[] splittedPath = mPath.split("\\."); //$NON-NLS-1$
        if (splittedPath.length > 0) {
            name = splittedPath[splittedPath.length - 1];
        }
        return name;
    }

    /**
     * @return the type complete name, i.e. <code>com.sun.star.uno.XInterface</code> for
     *         <code>com.sun.star.uno.XInterface</code>.
     */
    public String getFullName() {
        return mPath;
    }

    /**
     * Returns the type of the type, ie {@link IUnoFactoryConstants#INTERFACE} for
     * <code>com.sun.star.uno.XInterface</code>.
     *
     * @return one of the types defined in {@link UnoTypeProvider}
     */
    public int getType() {
        return mType;
    }

    /**
     * @return whether the type is defined in an external project or not.
     */
    public boolean isLocalType() {
        return mLocal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        String sLocal = EXTERNAL_TAG;
        if (isLocalType()) {
            sLocal = LOCAL_TAG;
        }

        return sLocal + " " + getFullName() + " " + getType(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Set the type of the UNO type, e.g. service, interface.
     *
     * @param pType
     *            the type to set described using the types constants
     */
    private void setType(int pType) {
        if (pType >= 0 && pType < ALL_TYPES_FILTER) {
            mType = pType;
        }
    }

    /**
     * Sets whether the type is local to the project or defined in an external <code>types.rdb</code> file.
     *
     * @param pTag
     *            {@link #LOCAL_TAG} or {@link #EXTERNAL_TAG}.
     */
    private void setLocal(String pTag) {
        if (pTag.equals(LOCAL_TAG)) {
            mLocal = true;
        } else {
            mLocal = false;
        }
    }
}
