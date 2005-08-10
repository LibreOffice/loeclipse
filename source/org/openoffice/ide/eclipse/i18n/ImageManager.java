/*************************************************************************
 *
 * $RCSfile: ImageManager.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/10 12:07:23 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the following licenses
 *
 *     - GNU Lesser General Public License Version 2.1
 *     - Sun Industry Standards Source License Version 1.1
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
 *
 * Sun Industry Standards Source License Version 1.1
 * =================================================
 * The contents of this file are subject to the Sun Industry Standards
 * Source License Version 1.1 (the "License"); You may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.openoffice.org/license.html.
 *
 * Software provided under this License is provided on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 * MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 * See the License for the specific provisions governing your rights and
 * obligations concerning the Software.
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
	 * @result image corresponding to the key, or <code>null</code> if the key
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
	 * @result image descriptor corresponding to the key, or <code>null</code> 
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
