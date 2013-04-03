/*************************************************************************
 *
 * $RCSfile: ISdk.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/12/07 08:47:16 $
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
package org.openoffice.ide.eclipse.core.model.config;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;

/**
 * Interface defining a LibreOffice SDK.
 * 
 * @author cedricbosdo
 *
 */
public interface ISdk {

    /**
     * Set the new SDK Home after having checked for the existence of the idl and settings directory.
     * Fetches the sdk name and buildid from the dk.mk file
     * 
     * @param pHome path to the new sdk home
     * 
     * @exception InvalidConfigException <p>This exception is thrown when the 
     *               following errors are encountered with the 
     *               {@link InvalidConfigException#INVALID_SDK_HOME}error code: </p>
     *             <ul>
     *                <li>the sdk path does not point to a valid directory</li>
     *                <li>the $(SDK_HOME)/idl directory doesnt exist</li>
     *                <li>the $(SDK_HOME)/settings directory doesnt exist</li>
     *                <li>the sdk name and buildid cannot be fetched</li>
     *                <li>an unexpected exception has been raised</li>
     *             </ul>
     */
    public void setHome(String pHome) throws InvalidConfigException;
    
    /**
     * Returns the SDK home directory. This string could be passed to the
     * Path constructor to get the folder object. 
     * 
     * @return SDK home directory
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
    
    /**
     * @return the path to the directory containing the binaries in the SDK.
     */
    public IPath getBinPath();
    
    /**
     * @return the path to the includes in the SDK.
     */
    public IPath getIncludePath();
    
    /**
     * @return the path to the libraries in the SDK.
     */
    public IPath getLibPath();
    
    /**
     * Create a process for the given shell command. This process will 
     * be created with the project parameters such as it's SDK and 
     * location path
     * 
     * @param pProject the UNO project on which to run the tool
     * @param pShellCommand the shell command to execute the tool
     * @param pMonitor a process monitor to watch the tool launching
     * 
     * @return the process executing the tool
     */
    public Process runTool(IUnoidlProject pProject,   
            String pShellCommand, IProgressMonitor pMonitor);
    
    /**
     * Create a process for the given shell command. This process will 
     * be created with the project parameters such as it's SDK and 
     * location path
     * 
     * @param pProject the folder from which to run the command
     * @param pOOo the ooo instance to run the tool
     * @param pShellCommand the shell command to execute the tool
     * @param pEnv tool environment variable
     * @param pMonitor a process monitor to watch the tool launching
     * 
     * @return the process executing the tool
     */
    public Process runToolWithEnv(IProject pProject, IOOo pOOo, 
            String pShellCommand, String[] pEnv, IProgressMonitor pMonitor);

}
