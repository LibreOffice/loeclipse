/*************************************************************************
 *
 * $RCSfile: ImageManager.java,v $
 *
 * $Revision: 1.6 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/02/19 11:32:41 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
package org.openoffice.ide.eclipse.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.openoffice.ide.eclipse.OOEclipsePlugin;

/**
 * The image manager is a singleton object that returns an image or an image descriptor
 * from a key. The keys are described in the ImagesConstants class and the associated 
 * properties file is /icons/images.conf. The goal of this object is to better parametrize
 * the images and minimize the images creations.
 * 
 * @author cbosdonnat
 *
 */
public class ImageManager {
	
	/**
	 * <p>Constant designing the icon equivalence file. This file defines the 
	 * association between the image key and the image file. This mechanism
	 * helps changing icons without changing the code.</p>
	 * 
	 * <p>For example, this file will define that the ERROR key corresponds
	 * to the /icons/errors.gif image.</p>
	 */
	
	private ResourceBundle imageBundle;
	
	private ImageRegistry registry = new ImageRegistry();
	
	public ImageManager() {
		
		try {
			imageBundle = ResourceBundle.getBundle("org.openoffice.ide.eclipse.i18n.ImageManager");
			
		} catch (NullPointerException e) {
			
			if (null != System.getProperty("DEBUG")) {
				System.out.println("Call to getBundle is incorrect: NullPointerException catched");
			}
		} catch(MissingResourceException e) {
			
			String message = "Image file not found for locale :" + Locale.getDefault().toString();
			OOEclipsePlugin.logError(message, null);
		}
	}
	
	/**
	 * Method which returns the image corresponding to the provided key.
	 * 
	 * @param key Key corresponding to the image to find
	 * @return image corresponding to the key, or <code>null</code> if the key
	 *         doesn't exists or the bundle is null
	 */
	public Image getImage(String key){
		
		// Tries to load the image from the registry before looking into the bundle
		Image image = registry.get(key);
		
		if (null == image){
		
			// The registry do not contain the key, so look into the bundle
			ImageDescriptor descr = getImageDescriptor(key);
			
			if (null != descr){
				// if the descriptor isn't null, create the image
				image = descr.createImage();
				registry.put(key, image);
			}
		}
		
		return image;
	}

	/**
	 * Method which returns the image descriptor corresponding to the provided key.
	 * 
	 * @param key Key corresponding to the image to find
	 * @return image descriptor corresponding to the key, or <code>null</code> 
	 * 		   if the key doesn't exists or the bundle is null
	 */
	public ImageDescriptor getImageDescriptor(String key){
		ImageDescriptor imageDescr = null;
		
		if (null != imageBundle){
			// Fetch the plugin relative path from the bundle
			String path = imageBundle.getString(key);
			imageDescr = AbstractUIPlugin.imageDescriptorFromPlugin(
									OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, 
									path);
		}
		
		return imageDescr;
	}
	
}
