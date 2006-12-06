/*************************************************************************
 *
 * $RCSfile: SDK.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/12/06 07:49:24 $
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
package org.openoffice.ide.eclipse.core.internal.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.ITableElement;
import org.openoffice.ide.eclipse.core.internal.helpers.SystemHelper;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 * Class representing a SDK instance used in the UNO-IDL projects
 * 
 * @author cbosdonnat
 *
 */
public class SDK implements ISdk, ITableElement {
	
	public static final String NAME = "__sdk_name"; //$NON-NLS-1$
	
	public static final String PATH = "__sdk_path"; //$NON-NLS-1$
	
	/**
	 * private constant that holds the sdk build id key in the dk.mk file
	 */
	private static final String K_SDK_BUILDID = "BUILDID"; //$NON-NLS-1$
	
	/**
	 * private constant that hold the name of the sdk config file (normaly dk.mk)
	 * This is set to easily change if there are future sdk organization changes
	 */
	private static final String F_DK_CONFIG = "dk.mk"; //$NON-NLS-1$

	
	
	/* SDK Members */
	
	private String buildId;
	private String sdkHome;
	
	/**
	 * Standard and only constructor for the SDK object. The name and buildId will be fetched
	 * from the $(SDK_HOME)/settings/dk.mk properties file.
	 * 
	 * @param sdkHome absolute path of the SDK root
	 */
	public SDK (String sdkHome) throws InvalidConfigException {
		
		// Sets the path to the SDK
		setHome(sdkHome);
	}

	//----------------------------------------------------- ISdk Implementation
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.ISdk#setHome(java.lang.String)
	 */
	public void setHome(String home) throws InvalidConfigException {
		try {
		
			// Get the file representing the given sdkHome
			Path homePath = new Path(home);
			File homeFile = homePath.toFile();
			
			// First check the existence of this directory
			if (homeFile.exists() && homeFile.isDirectory()){
				
				/**
				 * <p>If the provided sdk home does not contains <li><code>idl</code></li>
				 * <li><code>settings</code></li> directories, the provided sdk is considered as invalid</p>
				 */
				
				// test for the idl directory
				File idlFile = new File(homeFile, "idl"); //$NON-NLS-1$
				if (! (idlFile.exists() && idlFile.isDirectory()) ) {
					throw new InvalidConfigException(
							Messages.getString("SDK.NoIdlDirError"),  //$NON-NLS-1$
							InvalidConfigException.INVALID_SDK_HOME);
				}
				
				// test for the settings directory
				File settingsFile = new File(homeFile, "settings"); //$NON-NLS-1$
				if (! (settingsFile.exists() && settingsFile.isDirectory()) ) {
					throw new InvalidConfigException(
							Messages.getString("SDK.NoSettingsDirError"), //$NON-NLS-1$
							InvalidConfigException.INVALID_SDK_HOME);
				}
				
				// test for the uno-skeletonmaker tool
				String binName = "uno-skeletonmaker"; //$NON-NLS-1$
				if (Platform.getOS().equals(Platform.OS_WIN32)) {
					binName += ".exe";  //$NON-NLS-1$
				}
				if (!getBinPath(home).append(binName).toFile().exists()) {
					throw new InvalidConfigException(
							Messages.getString("SDK.MinSdkVersionError"), //$NON-NLS-1$
							InvalidConfigException.INVALID_SDK_HOME);
				}
				
				// If the settings and idl directory both exists, then try to fetch the name and buildId from
				// the settings/dk.mk properties file
				readSettings(settingsFile);
				this.sdkHome = home;
				
			} else {
				throw new InvalidConfigException(
						Messages.getString("SDK.NoDirectoryError"), //$NON-NLS-1$
						InvalidConfigException.INVALID_SDK_HOME);
			}
		} catch (Throwable e) {
			
			if (e instanceof InvalidConfigException) {
				
				// Rethrow the InvalidSDKException
				InvalidConfigException exception = (InvalidConfigException)e;
				throw exception;
			} else {
				
				// Unexpected exception thrown
				throw new InvalidConfigException(
						Messages.getString("SDK.UnexpectedError"),  //$NON-NLS-1$
						InvalidConfigException.INVALID_SDK_HOME, e);
			}
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.ISdk#getId()
	 */
	public String getId(){
		String result = null;
		
		String[] splits = buildId.split("\\(.*\\)"); //$NON-NLS-1$
		if (splits.length > 0){
			result = splits[0];
		}
		
		return result;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.ISdk#getHome()
	 */
	public String getHome(){
		return sdkHome;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.ISdk#getBinPath()
	 */
	public IPath getBinPath() {
		return getBinPath(getHome());
	}
	
	private IPath getBinPath(String home) {
		IPath path = null;
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			path = new Path(home).append("/windows/bin/"); //$NON-NLS-1$
		} else if (Platform.getOS().equals(Platform.OS_LINUX)){
			path = new Path(home).append("/linux/bin"); //$NON-NLS-1$
		} else if (Platform.getOS().equals(Platform.OS_SOLARIS)){
			if (Platform.getOSArch().equals(Platform.ARCH_SPARC)) {
				path = new Path(home).append("/solsparc/bin"); //$NON-NLS-1$
			} else if (Platform.getOSArch().equals(Platform.ARCH_X86)) {
				path = new Path(home).append("/solintel/bin"); //$NON-NLS-1$
			}
		}
		return path;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.ISdk#runTool(org.openoffice.ide.eclipse.core.model.IUnoidlProject, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Process runTool(IUnoidlProject project, 
			String shellCommand, IProgressMonitor monitor){
		return runToolWithEnv(project, shellCommand, new String[0], monitor);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.ISdk#runToolWithEnv(org.openoffice.ide.eclipse.core.model.IUnoidlProject, java.lang.String, java.lang.String[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Process runToolWithEnv(IUnoidlProject project, 
			String shellCommand, String[] env, IProgressMonitor monitor){
		
		Process process = null;
		
		try {
			ISdk sdk = project.getSdk();
			IOOo ooo = project.getOOo();
			
			if (null != sdk && null != ooo){
				
				String pathSeparator = System.getProperty("path.separator"); //$NON-NLS-1$
				
				String binPath = null;
				
				// Get the environement variables and copy them. Needs Java 1.5
				
				String[] sysEnv = SystemHelper.getSystemEnvironement();
				
				// problems with PATH merging
				String[] vars = sysEnv;
				for (int i=0; i<env.length; i++) {
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
						vars = SystemHelper.addEnv(sysEnv, name, value, separator);
					}
				}
				
				
				// Fetch the OS family
				String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
				
				// Create the exec parameters depending on the OS
				if (osName.startsWith("windows")){ //$NON-NLS-1$
					binPath = sdk.getBinPath().toOSString();
					
					// Definining path variables
					Path oooLibsPath = new Path(ooo.getLibsPath());
					vars = SystemHelper.addEnv(vars, "PATH", binPath + pathSeparator +  //$NON-NLS-1$
							oooLibsPath.toOSString(), pathSeparator);
					
				} else if (osName.equals("linux") || osName.equals("solaris") ||  //$NON-NLS-1$ //$NON-NLS-2$
						osName.equals("sun os")) { //$NON-NLS-1$
					
					// An UN*X platform
					binPath = sdk.getBinPath().toOSString();
					
					if (null != sdk.getBinPath()){

						String[] tmpVars = SystemHelper.addEnv(vars, "PATH",  //$NON-NLS-1$
								binPath, pathSeparator); //$NON-NLS-1$
						vars = SystemHelper.addEnv(tmpVars, "LD_LIBRARY_PATH", //$NON-NLS-1$
								ooo.getLibsPath(), pathSeparator);
					}
					
				} else {
					// Unmanaged OS
					PluginLogger.error(
							Messages.getString("SDK.InvalidSdkError"), null); //$NON-NLS-1$
					return null;
				}
				
				// Run only if the OS and ARCH are valid for the SDK
				if (null != vars){
					File projectFile = project.getProjectPath().toFile();
					process = SystemHelper.runTool(shellCommand, vars, projectFile);
				}
				
			}
			
		} catch (IOException e) {
			// Error while launching the process 
			
			MessageDialog dialog = new MessageDialog(
					OOEclipsePlugin.getDefault().getWorkbench().
						getActiveWorkbenchWindow().getShell(),
					Messages.getString("SDK.PluginError"), //$NON-NLS-1$
					null,
					Messages.getString("SDK.ProcessError"), //$NON-NLS-1$
					MessageDialog.ERROR,
					new String[]{Messages.getString("SDK.Ok")}, 0); //$NON-NLS-1$
			dialog.setBlockOnOpen(true);
			dialog.create();
			dialog.open();
			
		} catch (SecurityException e) {
			// SubProcess creation unauthorized
			
			MessageDialog dialog = new MessageDialog(
					OOEclipsePlugin.getDefault().getWorkbench().
							getActiveWorkbenchWindow().getShell(),
					Messages.getString("SDK.PluginError"), //$NON-NLS-1$
					null,
					Messages.getString("SDK.ProcessError"), //$NON-NLS-1$
					MessageDialog.ERROR,
					new String[]{Messages.getString("SDK.Ok")},	0); //$NON-NLS-1$
			dialog.setBlockOnOpen(true);
			dialog.create();
			dialog.open();
		}
		
		return process;
	}
	
	//-------------------------------------------- ITableElement Implementation
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.ITableElement#getImage(java.lang.String)
	 */
	public Image getImage(String property) {
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.ITableElement#getLabel(java.lang.String)
	 */
	public String getLabel(String property) {
		String result = ""; //$NON-NLS-1$
		if (property.equals(NAME)) {
			result = getId();
		} else if (property.equals(PATH)) {
			result = getHome();
		}
		return result;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.ITableElement#getProperties()
	 */
	public String[] getProperties() {
		return new String[] {NAME, PATH};
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.ITableElement#canModify(java.lang.String)
	 */
	public boolean canModify(String property) {
		return false;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.ITableElement#getValue(java.lang.String)
	 */
	public Object getValue(String property) {
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.ITableElement#setValue(java.lang.String, java.lang.Object)
	 */
	public void setValue(String property, Object value) {
		// Nothing to do
	}
	
	/**
	 * Reads the dk.mk file to get the sdk name and buildid. They are set in the SDK object if 
	 * they are both fetched. Otherwise an invalide sdk exception is thrown.
	 * 
	 * @param settingsFile
	 * @throws InvalidConfigException Exception thrown when one of the following problems happened
	 *          <ul>
	 *             <li>the given settings file isn't a valid directory</li>
	 *             <li>the settings/dk.mk file doesn't exists or is unreadable</li>
	 *             <li>one or both of the sdk name or buildid key is not set</li>
	 *          </ul>
	 */
	private void readSettings(File settingsFile) throws InvalidConfigException {
		
		if (settingsFile.exists() && settingsFile.isDirectory()) {
		
			// Get the dk.mk file
			File dkFile = new File(settingsFile, F_DK_CONFIG);
			
			Properties dkProperties = new Properties();
			try {
				dkProperties.load(new FileInputStream(dkFile));
				
				// Checks if the name and buildid properties are set
				if (dkProperties.containsKey(K_SDK_BUILDID)){
					
					buildId = dkProperties.getProperty(K_SDK_BUILDID);
				} else if (dkProperties.containsKey(K_SDK_BUILDID)){
					
					buildId = dkProperties.getProperty(K_SDK_BUILDID);
				} else {
					throw new InvalidConfigException(
							Messages.getString("SDK.MissingKeyError") + K_SDK_BUILDID, //$NON-NLS-1$
							InvalidConfigException.INVALID_SDK_HOME);
				}
				
			} catch (FileNotFoundException e) {
				throw new InvalidConfigException(
						Messages.getString("SDK.NoFileError")+ "settings/" + F_DK_CONFIG ,  //$NON-NLS-1$ //$NON-NLS-2$
						InvalidConfigException.INVALID_SDK_HOME);
			} catch (IOException e) {
				throw new InvalidConfigException(
						Messages.getString("SDK.NoReadableFileError") + "settings/" + F_DK_CONFIG,  //$NON-NLS-1$ //$NON-NLS-2$
						InvalidConfigException.INVALID_SDK_HOME);
			}
			
		} else {
			throw new InvalidConfigException(
					Messages.getString("SDK.NoDirectoryError")+ settingsFile.getAbsolutePath(), //$NON-NLS-1$
					InvalidConfigException.INVALID_SDK_HOME);
		}
	}
}
