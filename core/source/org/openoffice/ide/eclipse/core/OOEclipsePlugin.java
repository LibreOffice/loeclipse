/*************************************************************************
 *
 * $RCSfile: OOEclipsePlugin.java,v $
 *
 * $Revision: 1.8 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/02/04 18:17:07 $
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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openoffice.ide.eclipse.core.editors.Colors;
import org.openoffice.ide.eclipse.core.i18n.ImageManager;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.SDKContainer;
import org.osgi.framework.BundleContext;

/**
 * Plugin entry point, it is used by Eclipse as a bundle. 
 * 
 * <p>This class contains the main constants of the plugin, like its
 * ID, the UNO project nature. The internationalization method is provided
 * in this class too.</p> 
 * 
 * @author cbosdonnat
 */
public class OOEclipsePlugin extends AbstractUIPlugin {

	/**
	 * Plugin home relative path for the ooo configuration file
	 */
	public final static String OOO_CONFIG = ".ooo_config"; //$NON-NLS-1$
	
	/**
	 * ooeclipseintegration plugin id
	 */
	public static final String OOECLIPSE_PLUGIN_ID = "org.openoffice.ide.eclipse.core"; //$NON-NLS-1$
	
	/**
	 * uno nature id
	 */
	// HELP The nature id is the natures extension point id appened to the plugin id
	public static final String UNO_NATURE_ID = OOECLIPSE_PLUGIN_ID + ".unonature"; //$NON-NLS-1$
	
	/**
	 * Uno idl editor ID
	 */
	public static final String UNO_EDITOR_ID = OOECLIPSE_PLUGIN_ID + ".editors.UnoidlEditor"; //$NON-NLS-1$
	
	/**
	 * Log level preference key. Used to store the preferences
	 */
	public static final String LOGLEVEL_PREFERENCE_KEY 	 = "loglevel"; //$NON-NLS-1$

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
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		setDefaultPreferences();

		// Creates the SDK container
		OOoContainer.load();
		SDKContainer.load();
		
		PluginLogger.info("OOEclipseIntegration started"); //$NON-NLS-1$
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		sPlugin = null;
		
		OOoContainer.dispose();
		SDKContainer.dispose();
		ProjectsManager.dispose();
	}

	/**
	 * Returns the shared instance.
	 */
	public static OOEclipsePlugin getDefault() {
		return sPlugin;
	}
	
	/**
	 * Returns the image manager. If it is null, this method wil create it
	 * before using it.
	 * 
	 * @return the image manager
	 */
	public ImageManager getImageManager(){
		if (null == mImageManager){
			mImageManager = new ImageManager();
		}
		
		return mImageManager;
	}
	
	/**
	 * Returns the image corresponding to the provided key. If the image file
	 * or the key doesn't exists, the method returns <code>null</code>.
	 * 
	 * @param key Key designing the image 
	 * @return the image associated to the key
	 * 
	 * @see ImageManager#getImage(String)
	 */
	public static Image getImage(String key){
		return getDefault().getImageManager().getImage(key);
	}
	
	/**
	 * Returns the image descriptor corresponding to the provided key. 
	 * If the image file or the key doesn't exists, the method returns 
	 * <code>null</code>.
	 * 
	 * @param key Key designing the image 
	 * @return the image descriptor associated to the key
	 * 
	 * @see ImageManager#getImageDescriptor(String)
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getDefault().getImageManager().getImageDescriptor(key);
	}
	
	/**
	 * Method that initialize the default preferences of the plugin
	 */
	public static void setDefaultPreferences() {
		final RGB
			STRING = new RGB(255,0,0),	                     // Ligth red 
			BACKGROUND = new RGB(255,255,255),               // White
			DEFAULT = new RGB(0,0,0),                        // Black
			KEYWORD = new RGB(127,0,85),                     // Prune
			TYPE = new RGB(0,0,128),                         // Dark blue
			COMMENT = new RGB(63,127,95),                    // Grey green
			DOC_COMMENT = new RGB(64,128,255),               // Light blue
			XML_TAG = new RGB(180, 180, 180),                // Light grey
			MODIFIER = new RGB(54, 221, 28),                 // Light green
			PREPROCESSOR_COMMAND = new RGB(128, 128, 128);   // Dark grey 
		
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
		
		store.setDefault(LOGLEVEL_PREFERENCE_KEY, PluginLogger.INFO);
	}

	/**
	 * Convenience method returning the active workbench page.
	 */
	public static IWorkbenchPage getActivePage(){
		IWorkbenchPage page = null;
		
		IWorkbenchWindow window = getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (null != window){
			page = window.getActivePage();
		}
		return page;
	}
}
