/*************************************************************************
 *
 * $RCSfile: OfficeHelper.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:48 $
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
package org.libreoffice.ide.eclipse.core.office;

import java.lang.reflect.Constructor;
import java.net.URLClassLoader;

import org.libreoffice.ide.eclipse.core.model.config.IOOo;

/**
 * Provides a set of utility methods to use to handle OOo. All the code handling LibreOffice has to be in the
 * {@value #OOO_PACKAGE} package. These classes have to be loaded by the {@link OfficeClassLoader}. All the classes
 * facade classes have to be in the same package than this class.
*/
public class OfficeHelper {

    static final String OOO_PACKAGE = "org.libreoffice.ide.eclipse.core.internal.office"; //$NON-NLS-1$

    static final String CLASS_CONNECTION = OOO_PACKAGE + ".OfficeConnection"; //$NON-NLS-1$

    /**
     * Create an office connection object using a given class loader.
     *
     * @param pClassLoader
     *            the class loader to use
     * @param pOOo
     *            the office to set in the connection
     *
     * @return the office connection object
     *
     * @throws Exception
     *             if the class cannot be found or the constructor cannot be called.
     */
    static Object createConnection(URLClassLoader pClassLoader, IOOo pOOo) throws Exception {
        String className = CLASS_CONNECTION;
        Class<?> clazz = pClassLoader.loadClass(className);

        Constructor<?> constr = clazz.getConstructor(IOOo.class);
        return constr.newInstance(pOOo);
    }
}
