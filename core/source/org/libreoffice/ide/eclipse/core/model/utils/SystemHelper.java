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
package org.libreoffice.ide.eclipse.core.model.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;

/**
 * Helper class for system variables handling.
 */
public class SystemHelper {

    public static final String PATH_SEPARATOR = System.getProperty("path.separator"); //$NON-NLS-1$

    private static final int COMMAND_ARGS_LENGTH = 3;

    /**
     * Get a normal Java File from an Eclipse IResource.
     *
     * @param res
     *            the IResource to convert
     *
     * @return the equivalent File
     */
    public static File getFile(IResource res) {
        return res.getLocation().toFile();
    }

    /**
     * Get a normal Java File from an {@link IUnoidlProject}.
     *
     * @param unoPrj
     *            {@link IUnoidlProject} to convert
     *
     * @return the equivalent File
     */
    public static File getFile(IUnoidlProject unoPrj) {
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(unoPrj.getName());
        return getFile(prj);
    }

    /**
     * Add an environment variable to an array of existing variables.
     *
     * @param env
     *            the array of existing environment variables where to add the new variable
     * @param name
     *            the name of the variable to add
     * @param value
     *            the value of the variable to add
     *
     * @return the completed array
     */
    public static String[] addPathEnv(String[] env, String name, String[] value) {

        String values = new String();
        for (int i = 0; i < value.length; i++) {
            String path = value[i];
            String tmpValue = new Path(path).toOSString();
            if (i < value.length - 1) {
                tmpValue += PATH_SEPARATOR;
            }
            values += tmpValue;
        }

        return addEnv(env, name, values, PATH_SEPARATOR);
    }

    /**
     * Add an environment variable to an array of existing variables.
     *
     * @param env
     *            the array of existing environment variables where to add the new variable
     * @param name
     *            the name of the variable to add
     * @param value
     *            the value of the variable to add
     * @param separator
     *            the separator to use if there is already a variable with the same name. If <code>null</code>, the old
     *            variable will be replaced
     *
     * @return the completed array
     */
    public static String[] addEnv(String[] env, String name, String value, String separator) {
        /*
         * TODO cdan should add a test for this method (test that the case is preserved even on windows, but compare
         * with ignoring case on windows)
         */
        String[] result = new String[1];

        if (env != null) {
            int i = 0;
            boolean found = false;

            while (!found && i < env.length) {
                String tmpEnv = env[i];
                String tmpName = name;
                if (Platform.getOS().equals(Platform.OS_WIN32)) {
                    tmpEnv = tmpEnv.toLowerCase();
                    tmpName = name.toLowerCase();
                }
                if (tmpEnv.startsWith(tmpName + "=")) { //$NON-NLS-1$
                    found = true;
                } else {
                    i++;
                }
            }

            if (found) {
                result = new String[env.length];
                System.arraycopy(env, 0, result, 0, env.length);
                if (null != separator) {
                    // First remove the leading NAME=
                    String tmpEnv =  env[i].replaceFirst(name + "=", "");
                    // Put the new env in front of the existing one
                    result[i] = name + "=" + value + separator + tmpEnv;
                } else {
                    result[i] = name + "=" + value; //$NON-NLS-1$
                }

            } else {
                result = new String[env.length + 1];
                System.arraycopy(env, 0, result, 0, env.length);
                result[result.length - 1] = name + "=" + value; //$NON-NLS-1$
            }
        } else {
            result[0] = name + "=" + value; //$NON-NLS-1$
        }

        return result;
    }

    /**
     * @return the system environment variables
     */
    public static String[] getSystemEnvironement() {
        Set<Entry<String, String>> envSet = System.getenv().entrySet();
        String[] sysEnv = new String[envSet.size()];
        Iterator<Entry<String, String>> iter = envSet.iterator();
        int i = 0;
        while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();
            sysEnv[i] = entry.getKey() + "=" + entry.getValue(); //$NON-NLS-1$
            i++;
        }
        return sysEnv;
    }

    /**
     * Run a shell command with the system environment and an optional execution directory.
     *
     * @param shellCommand
     *            the command to run
     * @param execDir
     *            the execution directory or <code>null</code> if none
     * @return the process for the running command
     * @throws IOException
     *             if anything wrong happens during the command launch
     */
    public static Process runToolWithSysEnv(String shellCommand, File execDir) throws IOException {
        return runTool(shellCommand, getSystemEnvironement(), execDir);
    }

    /**
     * Run a shell command with a given environment and an optional execution directory.
     *
     * @param shellCommand
     *            the command to run
     * @param env
     *            the environment variables
     * @param execDir
     *            the execution directory or <code>null</code> if none
     * @return the process for the running command
     * @throws IOException
     *             if anything wrong happens during the command launch
     */
    public static Process runTool(String shellCommand, String[] env, File execDir) throws IOException {
        String[] command = new String[COMMAND_ARGS_LENGTH];

        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
            if (osName.startsWith("windows 9")) { //$NON-NLS-1$
                command[0] = "command.com"; //$NON-NLS-1$
            } else {
                command[0] = "cmd.exe"; //$NON-NLS-1$
            }

            command[1] = "/C"; //$NON-NLS-1$
            command[2] = shellCommand;
        } else {
            command[0] = "sh"; //$NON-NLS-1$
            command[1] = "-c"; //$NON-NLS-1$
            command[2] = shellCommand;
        }

        String execPath = ""; //$NON-NLS-1$
        if (execDir != null) {
            execPath = " from dir: "; //$NON-NLS-1$
            execPath += execDir.getAbsolutePath();
        }
        PluginLogger.debug("Running command: " + shellCommand + //$NON-NLS-1$
            " with env: " + Arrays.toString(env) + //$NON-NLS-1$
            execPath);
        Process process = null;
        if (execDir != null) {
            process = Runtime.getRuntime().exec(command, env, execDir);
        } else {
            process = Runtime.getRuntime().exec(command, env);
        }
        return process;
    }
}
