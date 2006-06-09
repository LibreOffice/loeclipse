/**
 * 
 */
package org.openoffice.ide.eclipse.core.internal.model;

import org.eclipse.core.runtime.Platform;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 * @author cedric
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
		if (name == null || name.equals("")) {
			name = "URE";
		}
		
		super.setName(name);
	}
	
	/* (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getClassesPath()
	 */
	public String getClassesPath() {
		return getHome() + "/share/java";
	}

	/* (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getLibsPath()
	 */
	public String getLibsPath() {
		return getHome() + "/lib";
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getTypesPath()
	 */
	public String getTypesPath() {
		return getHome() + "/share/misc/types.rdb";
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getServicesPath()
	 */
	public String getServicesPath() {
		return getHome() + "/share/misc/services.rdb";
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getUnorcPath()
	 */
	public String getUnorcPath() {
		String path = getHome() + "/lib/uno";
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			path += ".ini";
		} else {
			path += "rc";
		}
		return path;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.preferences.IOOo#getUnoPath()
	 */
	public String getUnoPath() {
		return getHome() + "/bin/uno";
	}
}
