/*************************************************************************
 *
 * $RCSfile: OOEclipsePlugin.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/11 18:39:51 $
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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openoffice.ide.eclipse.core.editors.Colors;
import org.openoffice.ide.eclipse.core.i18n.ImageManager;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.SDKContainer;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;
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
public class OOEclipsePlugin extends AbstractUIPlugin implements IResourceChangeListener {

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
		SDKContainer.getInstance();
		
		// Load the projects manager
		ProjectsManager.getInstance();
		
		// Add a listener to the resources changes of the workspace
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, 
				IResourceChangeEvent.POST_CHANGE);

	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		sPlugin = null;
		
		OOoContainer.getInstance().dispose();
		SDKContainer.getInstance().dispose();
		ProjectsManager.getInstance().dispose();
		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
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
	protected ImageManager getImageManager(){
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
				
				IUnoidlProject unoproject = ProjectsManager.getInstance().
						getProject(project.getName());
				
				if (unoproject != null){
					UnoidlProjectHelper.setIdlProperty(unoproject);
				}
			}
		}
	}
	
	//	----------------- Utilities provided to run an sdk tool
	
	/**
	 * Create a process for the given shell command. This process will 
	 * be created with the project parameters such as it's SDK and 
	 * location path
	 * 
	 * @param project the UNO-IDL project on which to run the tool
	 * @param shellCommand the shell command to execute the tool
	 * @param monitor a process monitor to watch the tool launching
	 * 
	 * @return the process executing the tool
	 */
	public static Process runTool(IUnoidlProject project, 
			String shellCommand, IProgressMonitor monitor){
		return runToolWithEnv(project, shellCommand, new String[0], monitor);
	}
	
	
	/**
	 * Create a process for the given shell command. This process will 
	 * be created with the project parameters such as it's SDK and 
	 * location path
	 * 
	 * @param project the UNO-IDL project on which to run the tool
	 * @param shellCommand the shell command to execute the tool
	 * @param env tool environement variable
	 * @param monitor a process monitor to watch the tool launching
	 * 
	 * @return the process executing the tool
	 */
	public static Process runToolWithEnv(IUnoidlProject project, 
			String shellCommand, String[] env, IProgressMonitor monitor){
		
		Process process = null;
		
		try {
			ISdk sdk = project.getSdk();
			IOOo ooo = project.getOOo();
			
			if (null != sdk && null != ooo){
				
				// Get local references to the SDK used members
				String sdkHome = sdk.getHome();
				
				String pathSeparator = System.getProperty("path.separator"); //$NON-NLS-1$
				
				String binPath = null;
				String[] command = new String[3];
				
				// Get the environement variables and copy them. Needs Java 1.5
				Set envSet = System.getenv().entrySet();
				String[] sysEnv = new String[envSet.size()];
				Iterator iter = envSet.iterator();
				int i = 0;
				while (iter.hasNext())  {
					Map.Entry entry = (Map.Entry)iter.next();
					sysEnv[i] = (String)entry.getKey() + "=" + (String)entry.getValue(); //$NON-NLS-1$
					i++;
				}
				
				// FIXME problems with PATH merging
				String[] vars = sysEnv;
				for (i=0; i<env.length; i++) {
					String envi = env[i];
					Matcher m = Pattern.compile("([^=]+)=(.*)").matcher(envi); //$NON-NLS-1$
					if (m.matches()) {
						String name = m.group(1);
						String value = m.group(2);
						String separator = null;
						if (name.toLowerCase().equals("path") ||  //$NON-NLS-1$
								name.toLowerCase().equals("ld_library_path")) { //$NON-NLS-1$
							separator = pathSeparator;
						}
						vars = addEnv(sysEnv, name, value, separator);
					}
				}
				
				
				// Fetch the OS family
				String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
				
				// Create the exec parameters depending on the OS
				if (osName.startsWith("windows")){ //$NON-NLS-1$
					String sdkWinHome = new Path(sdkHome).toOSString();
					
					binPath = sdkWinHome + "\\windows\\bin\\";  //$NON-NLS-1$
					
					// Definining path variables
					Path oooLibsPath = new Path(ooo.getLibsPath());
					vars = addEnv(vars, "PATH", binPath + pathSeparator +  //$NON-NLS-1$
							oooLibsPath.toOSString(), pathSeparator);
					
					// Defining the command
					
					if (osName.startsWith("windows 9")){ //$NON-NLS-1$
						command[0] = "command.com"; //$NON-NLS-1$
					} else {
						command[0] = "cmd.exe"; //$NON-NLS-1$
					}
					
					command[1] = "/C"; //$NON-NLS-1$
					command[2] = shellCommand;
					
					
				} else if (osName.equals("linux") || osName.equals("solaris") ||  //$NON-NLS-1$ //$NON-NLS-2$
						osName.equals("sun os")) { //$NON-NLS-1$
					
					// An UN*X platform
					
					// Determine the platform
					String platform = null;
					
					if (osName.equals("linux")){ //$NON-NLS-1$
						platform = "/linux"; //$NON-NLS-1$
					} else {
						String osArch = System.getProperty("os.arch"); //$NON-NLS-1$
						if (osArch.equals("sparc")) { //$NON-NLS-1$
							platform = "/solsparc"; //$NON-NLS-1$
							
						} else if (osArch.equals("x86")) { //$NON-NLS-1$
							platform = "/solintel"; //$NON-NLS-1$
						}
					}
					
					if (null != platform){
						
						// The platform is one supported by a SDK
						binPath = sdkHome + platform + "/bin"; //$NON-NLS-1$

						String[] tmpVars = addEnv(vars, "PATH",  //$NON-NLS-1$
								sdkHome + platform + "/bin", pathSeparator); //$NON-NLS-1$
						vars = addEnv(tmpVars, "LD_LIBRARY_PATH", //$NON-NLS-1$
								ooo.getLibsPath(), pathSeparator);
						
						// Set the command
						command[0] = "sh"; //$NON-NLS-1$
						command[1] = "-c"; //$NON-NLS-1$
						command[2] = shellCommand;
					}
					
				} else {
					// Unmanaged OS
					PluginLogger.error(
							Messages.getString("OOEclipsePlugin.InvalidSdkError"), null); //$NON-NLS-1$
				}
				
				// Run only if the OS and ARCH are valid for the SDK
				if (null != vars && null != command){
					File projectFile = project.getProjectPath().toFile();
					PluginLogger.debug("Running command: " + shellCommand +  //$NON-NLS-1$
							" with env: " + Arrays.toString(vars) +  //$NON-NLS-1$
							" from dir: " + projectFile.getAbsolutePath()); //$NON-NLS-1$
					process = Runtime.getRuntime().exec(command, vars, projectFile);
				}
				
			}
			
		} catch (IOException e) {
			// Error while launching the process 
			
			MessageDialog dialog = new MessageDialog(
					OOEclipsePlugin.getDefault().getWorkbench().
						getActiveWorkbenchWindow().getShell(),
					Messages.getString("OOEclipsePlugin.PluginError"), //$NON-NLS-1$
					null,
					Messages.getString("OOEclipsePlugin.ProcessError"), //$NON-NLS-1$
					MessageDialog.ERROR,
					new String[]{Messages.getString("OOEclipsePlugin.Ok")}, 0); //$NON-NLS-1$
			dialog.setBlockOnOpen(true);
			dialog.create();
			dialog.open();
			
		} catch (SecurityException e) {
			// SubProcess creation unauthorized
			
			MessageDialog dialog = new MessageDialog(
					OOEclipsePlugin.getDefault().getWorkbench().
							getActiveWorkbenchWindow().getShell(),
					Messages.getString("OOEclipsePlugin.PluginError"), //$NON-NLS-1$
					null,
					Messages.getString("OOEclipsePlugin.ProcessError"), //$NON-NLS-1$
					MessageDialog.ERROR,
					new String[]{Messages.getString("OOEclipsePlugin.Ok")},	0); //$NON-NLS-1$
			dialog.setBlockOnOpen(true);
			dialog.create();
			dialog.open();
		}
		
		return process;
	}
	
	public static String[] addEnv(String[] env, String name, String value,
			String separator) {
		
		String[] result = new String[1];  
		
		if (env != null) { 
			int i = 0;
			boolean found = false;
			
			while (!found && i < env.length) {
				String tmpEnv = env[i]; 
				if (Platform.getOS().equals(Platform.OS_WIN32)) {
					tmpEnv = tmpEnv.toLowerCase();
					name = name.toLowerCase();
				}
				if (tmpEnv.startsWith(name+"=")) { //$NON-NLS-1$
					found = true;
				} else {
					i++;
				}
			}
			
			if (found) {
				result = new String[env.length];
				System.arraycopy(env, 0, result, 0, env.length);
				if (null != separator) {
					result[i] = env[i] + separator + value;
				} else {
					result[i] = name + "=" + value; //$NON-NLS-1$
				}
				
			} else {
				result = new String[env.length + 1];
				System.arraycopy(env, 0, result, 0, env.length);
				result[result.length-1] = name + "=" + value; //$NON-NLS-1$
			}
		} else {
			result [0] = name + "=" + value; //$NON-NLS-1$
		}
		
		return result;
	}
}
