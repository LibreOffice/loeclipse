/*************************************************************************
 *
 * $RCSfile: Flags.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:51 $
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
package org.openoffice.ide.eclipse.core.internal.helpers;

/**
 * Class handling a set of bit-ORed flags.
 *
 * @author cedricbosdo
 *
 */
public class Flags {

    private int mFlags;
    private int mMax;
    private int mAllowed;

    /**
     * Initializes the flags structure with a default value and a maximum
     * number of bits to use for the flags.
     *
     * @param pMax the number value of the flags
     * @param pAllowed the allowed flags
     * @param pDefault the default value
     */
    public Flags(int pMax, int pAllowed, int pDefault) {
        mFlags = pDefault;
        mMax = pMax;
        mAllowed = pAllowed;
    }

    /**
     * Set one or more types. To specify more than one types give the bit or
     * of all the types, e.g. <code>INTERFACE | SERVICE</code>. The non-allowed
     * flags are automatically stripped.
     *
     * @param pValue the bit or of the types
     */
    public void setTypes(int pValue) {

        // Only 10 bits available
        if (pValue >= 0 && pValue <= mMax) {
            if (mFlags != pValue) {
                mFlags = pValue & mAllowed;
            }
        }
    }

    /**
     * @return the flags set as an integer. The flags field is a bit OR of all the
     *          flags set.
     */
    public int getFlags() {
        return mFlags;
    }

    /**
     * Checks if the given flag will be queried.
     *
     * @param pFlag the flag to match
     * @return <code>true</code> if the flag is one of the flags set
     */
    public boolean isFlagSet(int pFlag) {
        return (mFlags & pFlag) == pFlag;
    }
}
