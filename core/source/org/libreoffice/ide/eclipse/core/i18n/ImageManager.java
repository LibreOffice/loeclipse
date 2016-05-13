/*************************************************************************
 *
 * $RCSfile: ImageManager.java,v $
 *
 * $Revision: 1.7 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:26 $
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
package org.libreoffice.ide.eclipse.core.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;

/**
 * The image manager is an object returning an image or an image descriptor from a key. The keys are described in the
 * ImagesConstants class and the associated properties file is <code>ImageManager.properties</code>.
 *
 *
 */
public class ImageManager {

    /**
     * <p>
     * Constant designing the icon equivalence file. This file defines the association between the image key and the
     * image file. This mechanism helps changing icons without changing the code.
     * </p>
     *
     * <p>
     * For example, this file will define that the ERROR key corresponds to the /icons/errors.gif image.
     * </p>
     */

    private ResourceBundle mImageBundle;

    private ImageRegistry mRegistry = new ImageRegistry();

    /**
     * Default constructor.
     */
    public ImageManager() {

        try {
            mImageBundle = ResourceBundle.getBundle("org.libreoffice.ide.eclipse.core.i18n.ImageManager"); //$NON-NLS-1$

        } catch (NullPointerException e) {
            PluginLogger.debug("Call to getBundle is incorrect: NullPointerException " + //$NON-NLS-1$
                "caught"); //$NON-NLS-1$
        } catch (MissingResourceException e) {

            String message = "Image file not found for locale :" //$NON-NLS-1$
                + Locale.getDefault().toString();
            PluginLogger.error(message);
        }
    }

    /**
     * Method which returns the image corresponding to the provided key.
     *
     * @param pKey
     *            Key corresponding to the image to find
     * @return image corresponding to the key, or <code>null</code> if the key doesn't exists or the bundle is null
     */
    public Image getImage(String pKey) {

        // Tries to load the image from the registry before looking into the bundle
        Image image = mRegistry.get(pKey);

        if (null == image) {

            // The registry do not contain the key, so look into the bundle
            ImageDescriptor descr = getImageDescriptor(pKey);

            if (null != descr) {
                // if the descriptor isn't null, create the image
                image = descr.createImage();
                mRegistry.put(pKey, image);
            }
        }

        return image;
    }

    /**
     * Method which returns the image descriptor corresponding to the provided key.
     *
     * @param pKey
     *            Key corresponding to the image to find
     * @return image descriptor corresponding to the key, or <code>null</code> if the key doesn't exists or the bundle
     *         is null
     */
    public ImageDescriptor getImageDescriptor(String pKey) {
        ImageDescriptor imageDescr = null;

        if (null != mImageBundle) {
            // Fetch the plugin relative path from the bundle
            String path = mImageBundle.getString(pKey);
            imageDescr = AbstractUIPlugin.imageDescriptorFromPlugin(OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, path);
        }

        return imageDescr;
    }

    /**
     * Finds the image descriptor from the bundle relative path of the image.
     *
     * @param pPath
     *            the image path
     * @return the image descriptor
     */
    public ImageDescriptor getImageDescriptorFromPath(String pPath) {

        if (!pPath.startsWith("/")) { //$NON-NLS-1$
            pPath = "/" + pPath; //$NON-NLS-1$
        }

        return AbstractUIPlugin.imageDescriptorFromPlugin(OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, pPath);
    }
}
