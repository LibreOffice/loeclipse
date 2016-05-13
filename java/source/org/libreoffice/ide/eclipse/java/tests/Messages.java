/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Novell, Inc.
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
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2009 by Novell, Inc.
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.java.tests;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Messages for the package.
 *
 *
 */
public class Messages {

    private static final String BUNDLE_NAME = "org.libreoffice.ide.eclipse.java.tests.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
        .getBundle(BUNDLE_NAME);

    /**
     * Default constructor.
     */
    private Messages() {
    }

    /**
     * Get the string from it's key.
     *
     * @param pKey the key of the string
     *
     * @return the internationalized string
     */
    public static String getString(String pKey) {
        String string = '!' + pKey + '!';
        try {
            string = RESOURCE_BUNDLE.getString(pKey);
        } catch (MissingResourceException e) {
        }
        return string;
    }
}
