/*************************************************************************
 *
 * $RCSfile: InvalidConfigException.java,v $
 *
 * $Revision: 1.3 $
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
package org.openoffice.ide.eclipse.core.model.config;


/**
 * This exception is thrown when an OOo or SDK isn't valid, ie that it's home
 * doesn't fit to a correct home directory for an OOo or SDK.
 *
 * @see IOOo
 * @see ISdk
 *
 * @author cedricbosdo
 */
public class InvalidConfigException extends Exception {

    /**
     * The error points to an invalid SDK home path.
     */
    public static final int INVALID_SDK_HOME = 0;

    /**
     * The error points to an invalid OOo home path.
     */
    public static final int INVALID_OOO_HOME = 1;

    private static final long serialVersionUID = 2019018152788487567L;

    private int mErrorCode;

    /**
     * Constructor of the invalid SDK exception. It needs a message and a error
     * code among those defined as constants of this class.
     *
     * @param pMessage error message
     * @param pCode error code
     */
    public InvalidConfigException(String pMessage, int pCode) {
        super(pMessage);

        mErrorCode = pCode;
    }

    /**
     * Constructor of the invalid SDK exception. It needs a message and a error
     * code among those defined as constants of this class.
     *
     * @param pMessage error message
     * @param pCode error code
     * @param pException exception in case there is one.
     */
    public InvalidConfigException(String pMessage, int pCode, Throwable pException) {
        super(pMessage, pException);

        mErrorCode = pCode;
    }

    /**
     * Returns the error code of the exception among the constants of the
     * <code>InvalidSDKException</code> class.
     *
     * @return error code.
     */
    public int getErrorCode() {
        return mErrorCode;
    }
}
