/**
 * 
 */
package org.openoffice.ide.eclipse.core.internal.model;

import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 * @author <a href="mailto:cedricbosdo@openofffice.org">Cedric Bosdonnat</a>
 *
 */
public class URE extends AbstractOOo {

	/**
	 * Creating a new URE instance specifying its home directory
	 * 
	 * @param aHome the URE home directory
	 * @throws InvalidConfigException is thrown if the home directory doesn't
	 * 		contains the required files and directories
	 */
	public URE(String aHome) throws InvalidConfigException {
		super(aHome);
		setName(null);
	}

	public URE(String aHome, String aName) throws InvalidConfigException {
		super(aHome, aName);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#setName(java.lang.String)
	 */
	protected void setName(String aName) {
		
		String name = aName;
		if (name == null || name.equals("")) { //$NON-NLS-1$
			name = "URE"; //$NON-NLS-1$
		}
		
		super.setName(name);
	}
	
	/* (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getClassesPath()
	 */
	public String getClassesPath() {
		String jars = getHome() + FILE_SEP + "share" + FILE_SEP + "java"; //$NON-NLS-1$ //$NON-NLS-2$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			jars = getHome() + FILE_SEP + "java"; //$NON-NLS-1$
		}
		return jars;
	}

	/* (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getLibsPath()
	 */
	public String getLibsPath() {
		String libs = getHome() + FILE_SEP + "lib"; //$NON-NLS-1$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			libs = getHome() + FILE_SEP + "bin"; //$NON-NLS-1$
		}
		return libs;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getTypesPath()
	 */
	public String getTypesPath() {
		String types = getHome() + FILE_SEP + "share" + FILE_SEP + "misc" + FILE_SEP + "types.rdb"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			types = getHome() + FILE_SEP + "misc" + FILE_SEP + "types.rdb"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return types;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getServicesPath()
	 */
	public String getServicesPath() {
		String services = getHome() + FILE_SEP + "share" + FILE_SEP + "misc" + FILE_SEP + "services.rdb"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			services = getHome() + FILE_SEP + "misc" + FILE_SEP + "services.rdb"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return services;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getUnorcPath()
	 */
	public String getUnorcPath() {
		String path = getHome() + FILE_SEP + "lib" + FILE_SEP + "unorc"; //$NON-NLS-1$ //$NON-NLS-2$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			path = getHome() + FILE_SEP + "bin" + FILE_SEP + "uno.ini"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return path;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getUnoPath()
	 */
	public String getUnoPath() {
		String uno = "uno.bin"; //$NON-NLS-1$
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			uno = "uno.exe";  //$NON-NLS-1$
		}
		
		return getHome() + FILE_SEP + "bin" + FILE_SEP + uno; //$NON-NLS-1$
	}
	
	public String toString() {
		return "URE " + getName(); //$NON-NLS-1$
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#createUnoCommand(java.lang.String, java.lang.String, java.lang.String[], java.lang.String[])
	 */
	public String createUnoCommand(String implementationName, 
			String libLocation, String[] registriesPath, String[] args) {
		
		String command = ""; //$NON-NLS-1$
		
		// Put the args into one string
		String sArgs = ""; //$NON-NLS-1$
		for (int i=0; i<args.length; i++) {
			sArgs += args[i];
			
			if (i < args.length -1) {
				sArgs += " "; //$NON-NLS-1$
			}
		}
		
		// Transform the registries into a string to give to UNO
		String additionnalRegistries = ""; //$NON-NLS-1$
		for (int i=0; i<registriesPath.length; i++) {
			additionnalRegistries += "-ro " + registriesPath[i]; //$NON-NLS-1$
			
			if (i < registriesPath.length -1) {
				additionnalRegistries += " "; //$NON-NLS-1$
			}
		}
		
		// Get the paths to OOo instance types and services registries
		Path typesPath = new Path(getTypesPath());
		Path servicesPath = new Path(getServicesPath());
		
		String sTypesPath = typesPath.toString().replace(" ", "%20");  //$NON-NLS-1$ //$NON-NLS-2$
		String sServicesPath = servicesPath.toString().replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
		
		String unoPath = getUnoPath();
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			unoPath = "\"" + unoPath + "\"";  // escape spaces in windows names //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		command = unoPath +
			" -c " + implementationName +  //$NON-NLS-1$
			" -l " + libLocation +  //$NON-NLS-1$
			" -ro file:///" + sTypesPath + //$NON-NLS-1$
			" -ro file:///" + sServicesPath +  //$NON-NLS-1$
			" " + additionnalRegistries +  //$NON-NLS-1$
			" -- " + sArgs;  //$NON-NLS-1$
		
		return command;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getJavaldxPath()
	 */
	public String getJavaldxPath() {
		String javaldx = getHome() + FILE_SEP + "bin" + FILE_SEP + "javaldx"; //$NON-NLS-1$ //$NON-NLS-2$
		
		// TODO is there any use of javaldx on windows ?
//		if (Platform.getOS().equals(Platform.OS_LINUX)) {
//			
//		}
		return  javaldx; 
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#canManagePackages()
	 */
	public boolean canManagePackages() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#updatePackage(java.io.File)
	 */
	public void updatePackage(File packageFile) {	
	}
}
