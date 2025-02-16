/*************************************************************************
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
package org.libreoffice.ide.eclipse.core.internal.model;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.libreoffice.ide.eclipse.core.model.config.InvalidConfigException;

/**
 * Class representing a URE installation.
 */
public class URE extends AbstractOOo {

    /**
     * Creating a new URE instance specifying its home directory.
     *
     * @param home
     *            the URE home directory
     * @throws InvalidConfigException
     *             is thrown if the home directory doesn't contains the required files and directories
     */
    public URE(String home) throws InvalidConfigException {
        super(home);
        setName(null);
    }

    /**
     * Creating a new URE instance specifying its home directory and name.
     *
     * @param home
     *            the URE home directory
     * @param name
     *            the URE name
     *
     * @throws InvalidConfigException
     *             is thrown if the home directory doesn't contains the required files and directories
     */
    public URE(String home, String name) throws InvalidConfigException {
        super(home, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String name) {

        String newName = name;
        if (newName == null || newName.equals("")) { //$NON-NLS-1$
            newName = "URE"; //$NON-NLS-1$
        }

        super.setName(newName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getClassesPath() {
        String jars;
        if (getPlatform().equals(Platform.OS_MACOSX)) {
            jars = getHome() + FILE_SEP + "Resources" + FILE_SEP + "java";
        } else {
            jars = getHome() + FILE_SEP + "program" + FILE_SEP + "classes"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return new String[] { jars };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getLibsPath() {
        String libs = getHome() + FILE_SEP + "lib"; //$NON-NLS-1$
        if (getPlatform().equals(Platform.OS_WIN32)) {
            libs = getHome() + FILE_SEP + "bin"; //$NON-NLS-1$
        }
        return new String[] { libs };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getBinPath() {
        String libs = getHome() + FILE_SEP + "bin"; //$NON-NLS-1$
        return new String[] { libs };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getTypesPath() {
        String types;
        if (getPlatform().equals(Platform.OS_MACOSX)) {
            types = getHome() + FILE_SEP + "Resources" + FILE_SEP + "ure" + FILE_SEP + "share" + //$NON-NLS-1$
                FILE_SEP + "misc" + FILE_SEP + "types.rdb";
        } else {
            types = getHome() + FILE_SEP + "program" + FILE_SEP + "types.rdb"; //$NON-NLS-1$
        }
        return new String[] { types };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getServicesPath() {
        String services;
        if (getPlatform().equals(Platform.OS_MACOSX)) {
            services = getHome() + FILE_SEP + "Resources" + FILE_SEP + "ure" + FILE_SEP + "share" + //$NON-NLS-1$
                FILE_SEP + "misc" + FILE_SEP + "services.rdb";
        } else {
            services = getHome() + FILE_SEP + "program" + FILE_SEP + "services.rdb"; //$NON-NLS-1$
        }
        return new String[] { services };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUnorcPath() {
        String basis = getHome() + FILE_SEP;
        if (getPlatform().equals(Platform.OS_MACOSX)) {
            basis += "Resources" + FILE_SEP + "URE" + FILE_SEP + "etc";
        } else {
            basis += "program";
        }
        String filename = "unorc";
        if (getPlatform().equals(Platform.OS_WIN32)) {
            filename = "uno.ini";
        }
        return basis + FILE_SEP + filename;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUnoPath() {
        String home = null;
        if (getPlatform().equals(Platform.OS_MACOSX)) {
            home = getHome() + FILE_SEP + "MacOS" + FILE_SEP + getUnoExecutable(); //$NON-NLS-1$
        } else {
            home = getHome() + FILE_SEP + "program" + FILE_SEP + getUnoExecutable(); //$NON-NLS-1$
        }
        return home;
    }

    public static String getUnoExecutable() {
        String executable = null;
        if (getPlatformOS().equals(Platform.OS_WIN32)) {
            executable = "uno.exe"; //$NON-NLS-1$
        } else if (getPlatformOS().equals(Platform.OS_MACOSX)) {
            executable = "uno"; //$NON-NLS-1$
        } else {
            executable = "uno.bin"; //$NON-NLS-1$
        }
        return executable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "URE " + getName(); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createUnoCommand(String implementationName, String libLocation, String[] registriesPath,
        String[] args) {

        String command = ""; //$NON-NLS-1$

        // Put the args into one string
        String sArgs = ""; //$NON-NLS-1$
        for (int i = 0; i < args.length; i++) {
            sArgs += args[i];

            if (i < args.length - 1) {
                sArgs += " "; //$NON-NLS-1$
            }
        }

        // Transform the registries into a string to give to UNO
        String additionnalRegistries = ""; //$NON-NLS-1$
        for (int i = 0; i < registriesPath.length; i++) {
            additionnalRegistries += "-ro " + registriesPath[i]; //$NON-NLS-1$

            if (i < registriesPath.length - 1) {
                additionnalRegistries += " "; //$NON-NLS-1$
            }
        }

        // Get the paths to OOo instance types and services registries
        String typesArg = ""; //$NON-NLS-1$
        String[] paths = getTypesPath();
        for (String path : paths) {
            Path typesPath = new Path(path);
            String sTypesPath = typesPath.toString().replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
            typesArg += " -ro file:///" + sTypesPath; //$NON-NLS-1$
        }

        String serviceArgs = ""; //$NON-NLS-1$
        String[] servicePaths = getServicesPath();
        for (String path : servicePaths) {
            Path servicesPath = new Path(path);
            String sServicesPath = servicesPath.toString().replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
            serviceArgs += " -ro file:///" + sServicesPath; //$NON-NLS-1$
        }

        String unoPath = getUnoPath();
        if (Platform.OS_WIN32.equals(getPlatform())) {
            unoPath = "\"" + unoPath + "\""; // escape spaces in windows names //$NON-NLS-1$ //$NON-NLS-2$
        }

        command = unoPath + " -c " + implementationName + //$NON-NLS-1$
            " -l " + libLocation + //$NON-NLS-1$
            typesArg + " -ro file:///" + serviceArgs + //$NON-NLS-1$
            " " + additionnalRegistries + //$NON-NLS-1$
            " -- " + sArgs; //$NON-NLS-1$

        return command;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJavaldxPath() {
        return getHome() + FILE_SEP + "bin" + FILE_SEP + "javaldx"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canManagePackages() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePackage(File packageFile, IPath userInstallation) {
    }
}
