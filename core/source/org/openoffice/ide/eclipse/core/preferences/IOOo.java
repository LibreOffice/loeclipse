package org.openoffice.ide.eclipse.core.preferences;

/**
 * Interface for an OpenOffice.org configuration instance. This can even
 * be implemented as an URE instance.
 * 
 * @author cbosdonnat
 */
public interface IOOo {

	/**
	 * Set the home directory.
	 * 
	 * @param home the absolute path to the home directory
	 * @throws InvalidConfigException is thrown if the path doesn't match the 
	 * 		implementation requirement for an OOo instance. The error code will
	 * 		be {@link InvalidConfigException#INVALID_OOO_HOME}
	 */
	public void setHome(String home) throws InvalidConfigException;
	
	/**
	 * Returns the path to the OpenOffice.org home directory. This string could 
	 * be passed to the Path constructor to get the folder object. 
	 * 
	 * @return path to the OpenOffice.org home directory.
	 */
	public String getHome();
	
	/**
	 * Returns the OOo name. It should be a unique identifier
	 * 
	 * @return ooo name
	 */
	public String getName();
	
	/**
	 * <p>Returns the path to the OpenOffice.org classes directory. 
	 * This string could be passed to the Path constructor to get the 
	 * folder object.</p> 
	 * 
	 * <p><em>This method should be used for future compatibility with 
	 * URE applications</em></p>
	 * 
	 * @return path to the OpenOffice.org classes directory
	 */
	public String getClassesPath();
	
	/**
	 * <p>Returns the path to the OpenOffice.org shared libraries. This string
	 * could be passed to the Path constructor to get the folder object.</p>
	 * 
	 * @return path to the OpenOffice.org libraries directory
	 */
	public String getLibsPath();
	
	/**
	 * Returns the path to the <code>types.rdb</code> file of the OOo or URE
	 * instance.
	 */
	public String getTypesPath();
	
	/**
	 * Returns the path to the <code>services.rdb</code> file of the OOo or URE
	 * instance.
	 */
	public String getServicesPath();
	
	/**
	 * Returns the path to the uno bootstrap properties file.
	 */
	public String getUnorcPath();
	
	/**
	 * Returns the path to the uno executable file
	 */
	public String getUnoPath();
	
	/**
	 * Returns a command to execute a UNO component
	 * 
	 * @param implementationName the name of the component implementation to run
	 * @param libLocation the name of the library containing the implementation
	 * @param registriesPaths the path to the additionnal registries
	 * @param args the argument for the component launch
	 */
	public String createUnoCommand(String implementationName, 
			String libLocation, String[] registriesPaths, String[] args);
}
