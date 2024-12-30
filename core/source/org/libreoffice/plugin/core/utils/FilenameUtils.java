/*************************************************************************
 * FilenameUtils.java
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 *
 * Contributor(s): oliver.boehm@agentes.de
 ************************************************************************/
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
package org.libreoffice.plugin.core.utils;

/**
 * The commons.io dependency was removed but we need some stuff from the FilenameUtils class. The missing functionality
 * is recoded here.
 *
 * @author oliver (oliver.boehm@agentes.de)
 * @since 1.1.1 (21.09.2010)
 */
public final class FilenameUtils {

    /** Utility class - no need to instantiate it. */
    private FilenameUtils() {
    }

    /**
     * Normalize.
     *
     * @param pPathName
     *            the pathname
     * @return the string
     */
    public static String normalize(final String pPathName) {
        return pPathName.replaceAll("//", "/");
    }

    /**
     * Separators to unix.
     *
     * @param pFileName
     *            the filename
     * @return the string
     */
    public static String separatorsToUnix(final String pFileName) {
        // return StringUtils.replaceChars(filename, '\\', '/');
        return pFileName.replace('\\', '/');
    }

}
