package org.openoffice.ide.eclipse.i18n;

import java.io.IOException;
import java.net.URL;
import java.util.PropertyResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.openoffice.ide.eclipse.OOEclipsePlugin;

/**
 * TODOC
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
	private static final String IMAGES_FILE = "/icons/images.conf";
	
	private PropertyResourceBundle imageBundle;
	
	private ImageRegistry registry = new ImageRegistry();
	
	public ImageManager() {
		
		URL fileUrl = OOEclipsePlugin.getDefault().getBundle().getEntry(IMAGES_FILE);
		
		try {
			imageBundle = new PropertyResourceBundle(fileUrl.openStream());
		} catch (IOException e) {
			// Unable to read the image file
			
			OOEclipsePlugin.logError(
					OOEclipsePlugin.getTranslationString(I18nConstants.FILE_UNREADABLE)+IMAGES_FILE,
					e);
			
		} catch (NullPointerException e) {
			// No image file found
			
			OOEclipsePlugin.logError(
					OOEclipsePlugin.getTranslationString(I18nConstants.FILE_NOT_FOUND)+IMAGES_FILE,
					e);
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
