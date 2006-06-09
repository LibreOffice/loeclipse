package org.openoffice.ide.eclipse.core.preferences;

import org.eclipse.core.runtime.Path;

public interface ISdk {

	/**
	 * Set the new SDK Home after having checked for the existence of the idl and settings directory.
	 * Fetches the sdk name and buildid from the dk.mk file
	 * 
	 * @param home path to the new sdk home
	 * 
	 * @exception InvalidConfigException <p>This exception is thrown when the 
	 * 			  following errors are encountered with the 
	 * 			  {@link InvalidConfigException#INVALID_SDK_HOME}error code: </p>
	 *             <ul>
	 *                <li>the sdk path does not point to a valid directory</li>
	 *                <li>the $(SDK_HOME)/idl directory doesnt exist</li>
	 *                <li>the $(SDK_HOME)/settings directory doesnt exist</li>
	 *                <li>the sdk name and buildid cannot be fetched</li>
	 *                <li>an unexpected exception has been raised</li>
	 *             </ul>
	 */
	public void setHome(String home) throws InvalidConfigException;
	
	/**
	 * Returns the SDK home directory. This string could be passed to the
	 * Path constructor to get the folder object. 
	 * 
	 * @return SDK home directory
	 * @see Path
	 */
	public String getHome();
	
	/**
	 * Returns the SDK build id without the parenthesized string. For example, if the
	 * full build id is <code>680m92(Build:8896)</code>, the result will be: <code>680m92</code>.
	 * 
	 * If the builid is <code>null</code>, the return will be 
	 * 
	 * @return the shortened build id
	 */
	public String getId();
	
}
