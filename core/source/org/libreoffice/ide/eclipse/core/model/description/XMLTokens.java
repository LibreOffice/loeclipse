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
package org.libreoffice.ide.eclipse.core.model.description;

/**
 * Class defining constants for the XML tokens contained in description.xml file.
 */
public class XMLTokens {

    public static final String ATTR_XMLNS = "xmlns"; //$NON-NLS-1$

    public static final String URI_DESCRIPTION = "http://openoffice.org/extensions/description/2006"; //$NON-NLS-1$
    public static final String PREFIX_DESCRIPTION = "d"; //$NON-NLS-1$

    public static final String ELEMENT_DESCRIPTION = "description"; //$NON-NLS-1$
    public static final String ELEMENT_VERSION = "version"; //$NON-NLS-1$
    public static final String ELEMENT_IDENTIFIER = "identifier"; //$NON-NLS-1$
    public static final String ELEMENT_PLATFORM = "platform"; //$NON-NLS-1$
    public static final String ELEMENT_DEPENDENCIES = "dependencies"; //$NON-NLS-1$
    public static final String ELEMENT_UPDATE_INFORMATION = "update-information"; //$NON-NLS-1$
    public static final String ELEMENT_REGISTRATION = "registration"; //$NON-NLS-1$
    public static final String ELEMENT_PUBLISHER = "publisher"; //$NON-NLS-1$
    public static final String ELEMENT_RELEASE_NOTES = "release-notes"; //$NON-NLS-1$
    public static final String ELEMENT_DISPLAY_NAME = "display-name"; //$NON-NLS-1$
    public static final String ELEMENT_ICON = "icon"; //$NON-NLS-1$
    public static final String ELEMENT_EXTENSION_DESCRIPTION = "extension-description"; //$NON-NLS-1$

    public static final String ELEMENT_OOO_MIN = "OpenOffice.org-minimal-version"; //$NON-NLS-1$
    public static final String ELEMENT_OOO_MAX = "OpenOffice.org-maximal-version"; //$NON-NLS-1$
    public static final String ELEMENT_NAME = "name"; //$NON-NLS-1$
    public static final String ELEMENT_SRC = "src"; //$NON-NLS-1$
    public static final String ELEMENT_SIMPLE_LICENSE = "simple-license"; //$NON-NLS-1$
    public static final String ELEMENT_LICENSE_TEXT = "license-text"; //$NON-NLS-1$
    public static final String ELEMENT_DEFAULT = "default"; //$NON-NLS-1$
    public static final String ELEMENT_HIGH_CONTRAST = "high-contrast"; //$NON-NLS-1$

    public static final String ATTR_LANG = "lang"; //$NON-NLS-1$
    public static final String ATTR_VALUE = "value"; //$NON-NLS-1$
    public static final String ATTR_ACCEPT_BY = "accept-by"; //$NON-NLS-1$
    public static final String ATTR_SUPPRESS_ON_UPDATE = "suppress-on-update"; //$NON-NLS-1$

    public static final String VALUE_USER = "user"; //$NON-NLS-1$
    public static final String VALUE_ADMIN = "admin"; //$NON-NLS-1$

    public static final String URI_XLINK = "http://www.w3.org/1999/xlink"; //$NON-NLS-1$
    public static final String PREFIX_XLINK = "xlink"; //$NON-NLS-1$
    public static final String ATTR_HREF = "href"; //$NON-NLS-1$

    /**
     * Returns the XML qname corresponding to the given prefix and local name.
     *
     * @param pPrefix
     *            the prefix (can be <code>null</code>)
     * @param pLocalName
     *            the element local name
     *
     * @return the qname
     */
    public static String createQName(String pPrefix, String pLocalName) {
        String qname = pLocalName;
        if (pPrefix != null) {
            qname = pPrefix + ":" + qname; //$NON-NLS-1$
        }
        return qname;
    }

}
