/*************************************************************************
 *
 * $RCSfile: OOEclipsePlugin.java,v $
 *
 * $Revision: 1.6 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/11/27 17:48:25 $
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
package org.openoffice.ide.eclipse;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openoffice.ide.eclipse.editors.Colors;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.i18n.ImageManager;
import org.openoffice.ide.eclipse.i18n.Translator;
import org.openoffice.ide.eclipse.model.ModelUpdater;
import org.openoffice.ide.eclipse.model.UnoidlProject;
import org.openoffice.ide.eclipse.preferences.ooo.OOo;
import org.openoffice.ide.eclipse.preferences.sdk.SDK;
import org.openoffice.ide.eclipse.preferences.sdk.SDKContainer;
import org.osgi.framework.BundleContext;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class OOEclipsePlugin extends AbstractUIPlugin implements IResourceChangeListener {

	/**
	 * Plugin home relative path for the ooo configuration file
	 */
	public final static String OOO_CONFIG = ".ooo_config";
	
	/**
	 * ooeclipseintegration plugin id
	 */
	public static final String OOECLIPSE_PLUGIN_ID = "org.openoffice.ide.eclipse";
	
	/**
	 * uno nature id
	 */
	// HELP The nature id is the natures extension point id appened to the plugin id
	public static final String UNO_NATURE_ID = OOECLIPSE_PLUGIN_ID + ".unonature";
	
	public static final String SDKNAME_PREFERENCE_KEY    = "sdkname";
	public static final String SDKVERSION_PREFERENCE_KEY = "sdkversion";
	public static final String SDKPATH_PREFERENCE_KEY    = "sdkpath";
	public static final String OOOPATH_PREFERENCE_KEY    = "ooopath";

	public static final String UNO_EDITOR_ID = OOECLIPSE_PLUGIN_ID + ".editors.UnoidlEditor";

	// The shared instance.
	private static OOEclipsePlugin plugin;
	
	// An instance of the translator
	private Translator translator;
	
	// An instance of the imageManager
	private ImageManager imageManager;
	
	/**
	 * The constructor.
	 */
	public OOEclipsePlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		setDefaultPreferences();
		
		// Creates the SDK container
		SDKContainer.getSDKContainer();
		
		// TODO Add the project recovery at the beginning of a session
		
		
		// Loads each uno Nature
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i=0, length=projects.length; i<length; i++){
			IProject project = projects[i];
			if (project.hasNature(UNO_NATURE_ID)){
				UnoidlProject unoproject = (UnoidlProject)project.getNature(UNO_NATURE_ID);
				unoproject.configure();
			}
		}
		
		// Add a listener to the resources changes of the workspace
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, 
				IResourceChangeEvent.POST_CHANGE);

	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	/**
	 * Returns the shared instance.
	 */
	public static OOEclipsePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the translator. If the translator is null, this method will
	 * create it before returning it.
	 * 
	 * @return the translator
	 */
	protected Translator getTranslator(){
		
		// HELP Do not access to the translator directly, even if it is
	    //      supposed to be non-null: it could cause strange errors
		//      such as Bundle errors from eclipse...
		if (null == translator){
			translator = new Translator();
		}
		
		return translator;
	}
	
	/**
	 * This method uses internationalization files. They should be placed in the i18n
	 * directory of the plugin. Their name is contitued by <strong>OOEclipsePlugin_
	 * <em>contry</em>.lang</strong> where <em>country</em> corresponds to the two 
	 * letters designing the country or the word default (for the default translation file).
	 * 
	 * The default file UnoPlugin_us.lang should be provided. If no key is found in the 
	 * locale corresponding file, the key is returned instead of the internationalized 
	 * message.
	 * 
	 * @param key Asked entry in the internationalization file.
	 */
	public static String getTranslationString(String key) {
		return getDefault().getTranslator().getString(key);
	}
	
	/**
	 * Returns the image manager. If it is null, this method wil create it
	 * before using it.
	 * 
	 * @return the image manager
	 */
	protected ImageManager getImageManager(){
		if (null == imageManager){
			imageManager = new ImageManager();
		}
		
		return imageManager;
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
	}
	
	/**
	 * This static method is provided to easier log errors in the eclipse error view
	 * 
	 * @param message Message to print in the error log view
	 * @param e Exception raised. Could be null.
	 */
	public static void logError(String message, Exception e){
		getDefault().getLog().log(new Status(
				Status.ERROR, 
				getDefault().getBundle().getSymbolicName(),
				Status.ERROR,
				message,
				e));
	}

	/**
	 * This static method is provided to easier log warnings in the eclipse error view
	 * 
	 * @param message Message to print in the warning log view
	 * @param e Exception raised. Could be null.
	 */
	public static void logWarning(String message, Exception e){
		getDefault().getLog().log(new Status(
				Status.WARNING, 
				getDefault().getBundle().getSymbolicName(),
				Status.WARNING,
				message,
				e));
	}

	public static IWorkbenchPage getActivePage(){
		IWorkbenchPage page = null;
		
		IWorkbenchWindow window = getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (null != window){
			page = window.getActivePage();
		}
		return page;
	}
	
	//--------------- Resources changing listener method
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {

		if (IResourceChangeEvent.POST_CHANGE == event.getType()){
			// Handle the addition of folders in a UNO-IDL capable folder
			
			// Extract all the additions among the changes
			IResourceDelta delta = event.getDelta();
			IResourceDelta[] added = delta.getAffectedChildren();
						
			// In all the added resources, process the projects
			for (int i=0, length=added.length; i<length; i++){
				IResourceDelta addedi = added[i];
				
				// Get the project
				IResource resource = addedi.getResource();
				IProject project = resource.getProject();
				
				try{
					if (project.hasNature(UNO_NATURE_ID)){
						((UnoidlProject)project.getNature(UNO_NATURE_ID)).
														setIdlProperty();
					}
				} catch (CoreException e){
					// Do nothing
				}
			}
			
			try {
				delta.accept(new ModelUpdater());
			} catch (CoreException e) {
				if (null != System.getProperty("DEBUG")) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	//	----------------- Utilities provided to run an sdk tools
	
	
	/**
	 * Create a process for the given shell command. This process will be created with the project
	 * paramters such as it's SDK and location path
	 * 
	 * @param project
	 * @param shellCommand
	 * @param monitor
	 * @return
	 */
	public static Process runTool(IProject project, String shellCommand, IProgressMonitor monitor){
		
		Process process = null;
		
		try {
			UnoidlProject unoProject = (UnoidlProject)project.getNature(OOEclipsePlugin.UNO_NATURE_ID);
			SDK sdk = unoProject.getSdk();
			OOo ooo = unoProject.getOOo();
			
			if (null != sdk && null != ooo){
				
				// Get local references to the SDK used members
				String sdkHome = sdk.getSDKHome();
				String oooHome = ooo.getOOoHome();
				
				String pathSeparator = System.getProperty("path.separator");
				
				String binPath = null;
				String[] vars = null;
				String[] command = new String[3];;
				
				// Fetch the OS family
				String osName = System.getProperty("os.name").toLowerCase();
				
				
				// Create the exec parameters depending on the OS
				if (osName.startsWith("windows")){
					String sdkWinHome = new Path(sdkHome).toOSString();
					
					binPath = sdkWinHome + "\\windows\\bin\\"; 
					
					// Definining path variables
					vars = new String[1];
					vars[0] = "PATH=" + binPath + pathSeparator + oooHome + "\\program";
					
					// Defining the command
					
					if (osName.startsWith("windows 9")){
						command[0] = "command.com";
					} else {
						command[0] = "cmd.exe";
					}
					
					command[1] = "/C";
					command[2] = shellCommand;
					
					
				} else if (osName.equals("linux") || osName.equals("solaris") || osName.equals("sun os")) {
					
					// An UN*X platform
					
					// Determine the platform
					String platform = null;
					
					if (osName.equals("linux")){
						platform = "/linux";
					} else {
						String osArch = System.getProperty("os.arch");
						if (osArch.equals("sparc")) {
							platform = "/solsparc";
							
						} else if (osArch.equals("x86")) {
							platform = "/solintel";
						}
					}
					
					if (null != platform){
						
						// The platform is one supported by a SDK
						binPath = sdkHome + platform + "/bin";
						
						vars = new String[2];
						vars[0] = "PATH=" + sdkHome + platform + "/bin";
						vars[1] = "LD_LIBRARY_PATH=" + oooHome + "/program";
						
						// Set the command
						command[0] = "sh";
						command[1] = "-c";
						command[2] = shellCommand;
					}
					
				} else {
					// Unmanaged OS
					OOEclipsePlugin.logError(
							OOEclipsePlugin.getTranslationString(I18nConstants.SDK_INVALID_OS), 
							null);
				}
				
				// Run only if the OS and ARCH are valid for the SDK
				if (null != vars && null != command){
					
					File projectFile = unoProject.getProject().getLocation().toFile();
					process = Runtime.getRuntime().exec(command, vars, projectFile);
				}
				
			} else {
				// TODO Toggle sdk error marker if it doesn't exist
			}
			
		} catch (CoreException e) {
			// Not a uno nature
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(I18nConstants.NOT_UNO_PROJECT), e);
			
		} catch (IOException e) {
			// Error while launching the process 
			
			MessageDialog dialog = new MessageDialog(
					OOEclipsePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
					OOEclipsePlugin.getTranslationString(I18nConstants.UNO_PLUGIN_ERROR),
					null,
					OOEclipsePlugin.getTranslationString(I18nConstants.PROCESS_ERROR),
					MessageDialog.ERROR,
					new String[]{OOEclipsePlugin.getTranslationString(I18nConstants.OK)},
					0);
			dialog.setBlockOnOpen(true);
			dialog.create();
			dialog.open();
			
		} catch (SecurityException e) {
			// SubProcess creation unauthorized
			
			MessageDialog dialog = new MessageDialog(
					OOEclipsePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
					OOEclipsePlugin.getTranslationString(I18nConstants.UNO_PLUGIN_ERROR),
					null,
					OOEclipsePlugin.getTranslationString(I18nConstants.PROCESS_ERROR),
					MessageDialog.ERROR,
					new String[]{OOEclipsePlugin.getTranslationString(I18nConstants.OK)},
					0);
			dialog.setBlockOnOpen(true);
			dialog.create();
			dialog.open();
		}
		
		return process;
	}
}
