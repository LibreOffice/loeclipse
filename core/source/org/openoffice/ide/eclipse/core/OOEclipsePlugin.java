/*************************************************************************
 *
 * $RCSfile: OOEclipsePlugin.java,v $
 *
 * $Revision: 1.11 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:50 $
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
package org.openoffice.ide.eclipse.core;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.openoffice.ide.eclipse.core.editors.idl.Colors;
import org.openoffice.ide.eclipse.core.i18n.ImageManager;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.SDKContainer;
import org.osgi.framework.BundleContext;

/**
 * Plugin entry point, it is used by Eclipse as a bundle.
 *
 * <p>
 * This class contains the main constants of the plugin, like its ID, the UNO project nature. The internationalization
 * method is provided in this class too.
 * </p>
 *
 * @author cedricbosdo
 */
public class OOEclipsePlugin extends AbstractUIPlugin {

    /**
     * Plugin home relative path for the ooo configuration file.
     */
    public static final String OOO_CONFIG = ".ooo_config"; //$NON-NLS-1$

    /**
     * ooeclipseintegration plugin id.
     */
    public static final String OOECLIPSE_PLUGIN_ID = "org.openoffice.ide.eclipse.core"; //$NON-NLS-1$

    /**
     * uno nature id.
     */
    public static final String UNO_NATURE_ID = OOECLIPSE_PLUGIN_ID + ".unonature"; //$NON-NLS-1$

    /**
     * Uno idl editor ID.
     */
    public static final String UNO_EDITOR_ID = OOECLIPSE_PLUGIN_ID + ".editors.UnoidlEditor"; //$NON-NLS-1$

    /**
     * Log level preference key, used to store the preferences.
     */
    public static final String LOGLEVEL_PREFERENCE_KEY = "loglevel"; //$NON-NLS-1$

    // Light red
    public static final RGB STRING = new RGB(255, 0, 0);

    // White
    public static final RGB BACKGROUND = new RGB(255, 255, 255);

    // Black
    public static final RGB DEFAULT = new RGB(0, 0, 0);

    // Prune
    public static final RGB KEYWORD = new RGB(127, 0, 85);

    // Dark blue
    public static final RGB TYPE = new RGB(0, 0, 128);

    // Grey green
    public static final RGB COMMENT = new RGB(63, 127, 95);

    // Light blue
    public static final RGB DOC_COMMENT = new RGB(64, 128, 255);

    // Light grey
    public static final RGB XML_TAG = new RGB(180, 180, 180);

    // Light green
    public static final RGB MODIFIER = new RGB(54, 221, 28);

    // Dark grey
    public static final RGB PREPROCESSOR_COMMAND = new RGB(128, 128, 128);

    // The shared instance.
    private static OOEclipsePlugin sPlugin;

    // An instance of the imageManager
    private ImageManager mImageManager;

    /**
     * The constructor.
     */
    public OOEclipsePlugin() {
        sPlugin = this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(BundleContext pContext) throws Exception {
        super.start(pContext);
        setDefaultPreferences();

        // Creates the SDK container
        OOoContainer.load();
        SDKContainer.load();

        PluginLogger.info("OOEclipseIntegration started"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext pContext) throws Exception {
        super.stop(pContext);
        sPlugin = null;

        OOoContainer.dispose();
        SDKContainer.dispose();
        ProjectsManager.dispose();
    }

    /**
     * @return the instance of the OOo Eclipse bundle.
     */
    public static OOEclipsePlugin getDefault() {
        return sPlugin;
    }

    /**
     * Returns the image manager. If it is null, this method wil create it before using it.
     *
     * @return the image manager
     */
    public ImageManager getImageManager() {
        if (null == mImageManager) {
            mImageManager = new ImageManager();
        }

        return mImageManager;
    }

    /**
     * Returns the image corresponding to the provided key. If the image file or the key doesn't exists, the method
     * returns <code>null</code>.
     *
     * @param pKey
     *            Key designing the image
     * @return the image associated to the key
     *
     * @see ImageManager#getImage(String)
     */
    public static Image getImage(String pKey) {
        return getDefault().getImageManager().getImage(pKey);
    }

    /**
     * Returns the image descriptor corresponding to the provided key. If the image file or the key doesn't exists, the
     * method returns <code>null</code>.
     *
     * @param pKey
     *            Key designing the image
     * @return the image descriptor associated to the key
     *
     * @see ImageManager#getImageDescriptor(String)
     */
    public static ImageDescriptor getImageDescriptor(String pKey) {
        return getDefault().getImageManager().getImageDescriptor(pKey);
    }

    /**
     * Method that initialize the default preferences of the plugin.
     */
    public static void setDefaultPreferences() {
        IPreferenceStore store = getDefault().getPreferenceStore();
        PreferenceConverter.setDefault(store, Colors.C_KEYWORD, KEYWORD);
        PreferenceConverter.setDefault(store, Colors.C_BACKGROUND, BACKGROUND);
        PreferenceConverter.setDefault(store, Colors.C_TEXT, DEFAULT);
        PreferenceConverter.setDefault(store, Colors.C_STRING, STRING);
        PreferenceConverter.setDefault(store, Colors.C_TYPE, TYPE);
        PreferenceConverter.setDefault(store, Colors.C_COMMENT, COMMENT);
        PreferenceConverter.setDefault(store, Colors.C_AUTODOC_COMMENT, DOC_COMMENT);
        PreferenceConverter.setDefault(store, Colors.C_PREPROCESSOR, PREPROCESSOR_COMMAND);
        PreferenceConverter.setDefault(store, Colors.C_XML_TAG, XML_TAG);
        PreferenceConverter.setDefault(store, Colors.C_MODIFIER, MODIFIER);

        store.setDefault(LOGLEVEL_PREFERENCE_KEY, LogLevels.INFO.toString());
    }
}
