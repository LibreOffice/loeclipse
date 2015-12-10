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
package org.libreoffice.ide.eclipse.core.editors.utils;

import java.util.Locale;

/**
 * Interface to implement in order to get notified of Locale changes in a control.
 *
 * @author cbosdonnat
 *
 */
public interface ILocaleListener {

    /**
     * The locale selection has changed.
     *
     * @param pLocale
     *            the new locale to use.
     */
    public void selectLocale(Locale pLocale);

    /**
     * A locale has been deleted.
     *
     * @param pLocale
     *            the deleted locale
     */
    public void deleteLocale(Locale pLocale);

    /**
     * A locale has been added.
     *
     * @param pLocale
     *            the added locale
     */
    public void addLocale(Locale pLocale);
}
