/*************************************************************************
 *
 * $RCSfile: URE.java,v $
 *
 * $Revision: 1.6 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:26 $
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
package org.openoffice.ide.eclipse.core.internal.model;

import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 * Class representing a URE installation.
 * 
 * @author cedricbosdo
 *
 */
public class URE extends AbstractOOo {

    /**
     * Creating a new URE instance specifying its home directory.
     * 
     * @param pHome the URE home directory
     * @throws InvalidConfigException is thrown if the home directory doesn't
     *         contains the required files and directories
     */
    public URE(String pHome) throws InvalidConfigException {
        super(pHome);
        setName(null);
    }

    /**
     * Creating a new URE instance specifying its home directory and name.
     * 
     * @param pHome the URE home directory
     * @param pName the URE name
     * 
     * @throws InvalidConfigException is thrown if the home directory doesn't
     *         contains the required files and directories
     */
    public URE(String pHome, String pName) throws InvalidConfigException {
        super(pHome, pName);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void setName(String pName) {
        
        String name = pName;
        if (name == null || name.equals("")) { //$NON-NLS-1$
            name = "URE"; //$NON-NLS-1$
        }
        
        super.setName(name);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getClassesPath() {
        String jars = getHome() + FILE_SEP + "share" + FILE_SEP + "java"; //$NON-NLS-1$ //$NON-NLS-2$
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            jars = getHome() + FILE_SEP + "java"; //$NON-NLS-1$
        }
        return jars;
    }

    /**
     * {@inheritDoc}
     */
    public String getLibsPath() {
        String libs = getHome() + FILE_SEP + "lib"; //$NON-NLS-1$
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            libs = getHome() + FILE_SEP + "bin"; //$NON-NLS-1$
        }
        return libs;
    }

    /**
     * {@inheritDoc}
     */
    public String getTypesPath() {
        String types = getHome() + FILE_SEP + "share" + FILE_SEP + "misc" + //$NON-NLS-1$ //$NON-NLS-2$ 
            FILE_SEP + "types.rdb"; //$NON-NLS-1$
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            types = getHome() + FILE_SEP + "misc" + FILE_SEP + "types.rdb"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return types;
    }

    /**
     * {@inheritDoc}
     */
    public String getServicesPath() {
        String services = getHome() + FILE_SEP + "share" + FILE_SEP + "misc" + //$NON-NLS-1$ //$NON-NLS-2$ 
                FILE_SEP + "services.rdb"; //$NON-NLS-1$
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            services = getHome() + FILE_SEP + "misc" + FILE_SEP + "services.rdb"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return services;
    }

    /**
     * {@inheritDoc}
     */
    public String getUnorcPath() {
        String path = getHome() + FILE_SEP + "lib" + FILE_SEP + "unorc"; //$NON-NLS-1$ //$NON-NLS-2$
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            path = getHome() + FILE_SEP + "bin" + FILE_SEP + "uno.ini"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return path;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getUnoPath() {
        String uno = "uno.bin"; //$NON-NLS-1$
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            uno = "uno.exe";  //$NON-NLS-1$
        }
        
        return getHome() + FILE_SEP + "bin" + FILE_SEP + uno; //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "URE " + getName(); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public String createUnoCommand(String pImplementationName, 
            String pLibLocation, String[] pRegistriesPath, String[] pArgs) {
        
        String command = ""; //$NON-NLS-1$
        
        // Put the args into one string
        String sArgs = ""; //$NON-NLS-1$
        for (int i = 0; i < pArgs.length; i++) {
            sArgs += pArgs[i];
            
            if (i < pArgs.length - 1) {
                sArgs += " "; //$NON-NLS-1$
            }
        }
        
        // Transform the registries into a string to give to UNO
        String additionnalRegistries = ""; //$NON-NLS-1$
        for (int i = 0; i < pRegistriesPath.length; i++) {
            additionnalRegistries += "-ro " + pRegistriesPath[i]; //$NON-NLS-1$
            
            if (i < pRegistriesPath.length - 1) {
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
            " -c " + pImplementationName +  //$NON-NLS-1$
            " -l " + pLibLocation +  //$NON-NLS-1$
            " -ro file:///" + sTypesPath + //$NON-NLS-1$
            " -ro file:///" + sServicesPath +  //$NON-NLS-1$
            " " + additionnalRegistries +  //$NON-NLS-1$
            " -- " + sArgs;  //$NON-NLS-1$
        
        return command;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getJavaldxPath() {
        return getHome() + FILE_SEP + "bin" + FILE_SEP + "javaldx"; //$NON-NLS-1$ //$NON-NLS-2$ 
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean canManagePackages() {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public void updatePackage(File pPackageFile) {    
    }
}
