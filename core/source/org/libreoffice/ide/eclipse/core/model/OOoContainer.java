/*************************************************************************
 *
 * $RCSfile: OOoContainer.java,v $
 *
 * $Revision: 1.9 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/12/26 14:40:25 $
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
package org.libreoffice.ide.eclipse.core.model;

import org.eclipse.jface.preference.IPreferenceStore;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.internal.model.OOo;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.InvalidConfigException;

/**
 * Singleton object containing the LibreOffice configurations.
 */
public class OOoContainer {

    private static OOoContainer sInstance = new OOoContainer();
    private IOOo mLibreOfficeInstance;

    /**
     * The OOo Container should not be created by another object.
     */
    private OOoContainer() {
    }

    public static void setLibreOfficePath(String path) throws InvalidConfigException {
        sInstance.mLibreOfficeInstance = new OOo(path);
    }

    public static IOOo getOOo() {
        return sInstance.mLibreOfficeInstance;
    }

    /**
     * Loads the LibreOffice already configured instances from the preferences.
     */
    public static void load() {
        IPreferenceStore store = OOEclipsePlugin.getDefault().getPreferenceStore();

        String path = store.getString(OOEclipsePlugin.LIBREOFFICE_PATH_PREFERENCE_KEY);
        try {
            setLibreOfficePath(path);
        } catch (InvalidConfigException e) {
            PluginLogger.error(String.format("Couldn't find LibreOffice:\n%s", e.getLocalizedMessage()));
        }
    }

    /**
     * Saves the LibreOffice already configured instances to the preferences.
     */
    public static void save() {
        IPreferenceStore store = OOEclipsePlugin.getDefault().getPreferenceStore();

        store.setValue(OOEclipsePlugin.LIBREOFFICE_PATH_PREFERENCE_KEY, sInstance.mLibreOfficeInstance.getHome());
    }
}
