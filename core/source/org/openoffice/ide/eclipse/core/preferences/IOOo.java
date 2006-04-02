package org.openoffice.ide.eclipse.core.preferences;

import org.eclipse.core.runtime.Path;

public interface IOOo {

	/**
	 * 
	 * @param oooHome
	 * @throws InvalidConfigException
	 */
	public void setHome(String home) throws InvalidConfigException;
	
	/**
	 * Returns the path to the OpenOffice.org home directory. This string could 
	 * be passed to the Path constructor to get the folder object. 
	 * 
	 * @return path to the OpenOffice.org home directory.
	 * @see Path
	 */
	public String getHome();
	
	/**
	 * Returns the OOo name as set in the program/bootstraprc file
	 * 
	 * @return ooo name
	 */
	public String getName();
	
	/**
	 * Build a unique id from the ooo name and build id
	 *
	 * @return the ooo unique id
	 */
	public String getId();
	
	/**
	 * <p>Returns the path to the OpenOffice.org classes directory. This string could 
	 * be passed to the Path constructor to get the folder object.</p> 
	 * 
	 * <p><em>This method should be used for future compatibility with URE applications</em></p>
	 * 
	 * @return path to the OpenOffice.org classes directory
	 */
	public String getClassesPath();
}
