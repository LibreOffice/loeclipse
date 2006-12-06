/*************************************************************************
 *
 * $RCSfile: SystemHelper.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/12/06 07:49:23 $
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
package org.openoffice.ide.eclipse.core.internal.helpers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.openoffice.ide.eclipse.core.PluginLogger;

/**
 * Helper class for system variables handling.
 * 
 * @author cedricbosdo
 *
 */
public class SystemHelper {
	
	/**
	 * Add an environment variable to an array of existing variables.
	 * 
	 * @param env the array of existing environment variables where to add the
	 * 		new variable
	 * @param name the name of the variable to add
	 * @param value	the value of the variable to add
	 * @param separator the separator to use if there is already a variable with
	 * 		the same name. If <code>null</code>, the old variable will be replaced
	 * 
	 * @return the completed array
	 */
	public static String[] addEnv(String[] env, String name, String value,
			String separator) {
		
		String[] result = new String[1];  
		
		if (env != null) { 
			int i = 0;
			boolean found = false;
			
			while (!found && i < env.length) {
				String tmpEnv = env[i]; 
				if (Platform.getOS().equals(Platform.OS_WIN32)) {
					tmpEnv = tmpEnv.toLowerCase();
					name = name.toLowerCase();
				}
				if (tmpEnv.startsWith(name+"=")) { //$NON-NLS-1$
					found = true;
				} else {
					i++;
				}
			}
			
			if (found) {
				result = new String[env.length];
				System.arraycopy(env, 0, result, 0, env.length);
				if (null != separator) {
					result[i] = env[i] + separator + value;
				} else {
					result[i] = name + "=" + value; //$NON-NLS-1$
				}
				
			} else {
				result = new String[env.length + 1];
				System.arraycopy(env, 0, result, 0, env.length);
				result[result.length-1] = name + "=" + value; //$NON-NLS-1$
			}
		} else {
			result [0] = name + "=" + value; //$NON-NLS-1$
		}
		
		return result;
	}
	
	/**
	 * @return the system environement variables
	 */
	public static String[] getSystemEnvironement() {
		Set envSet = System.getenv().entrySet();
		String[] sysEnv = new String[envSet.size()];
		Iterator iter = envSet.iterator();
		int i = 0;
		while (iter.hasNext())  {
			Map.Entry entry = (Map.Entry)iter.next();
			sysEnv[i] = (String)entry.getKey() + "=" + (String)entry.getValue(); //$NON-NLS-1$
			i++;
		}
		return sysEnv;
	}
	
	/**
	 * Run a shell command with the system environment and an optional execution 
	 * directory.
	 * 
	 * @param shellCommand the command to run
	 * @param execDir the execution directory or <code>null</code> if none
	 * @return the process for the running command
	 * @throws IOException if anything wrong happens during the command launch
	 */
	public static Process runToolWithSysEnv(String shellCommand, File execDir) throws IOException {
		return runTool(shellCommand, getSystemEnvironement(), execDir);
	}
	
	/**
	 * Run a shell command with a given environment and an optional execution 
	 * directory.
	 * 
	 * @param shellCommand the command to run
	 * @param env the environment variables
	 * @param execDir the execution directory or <code>null</code> if none
	 * @return the process for the running command
	 * @throws IOException if anything wrong happens during the command launch
	 */
	public static Process runTool(String shellCommand, String[] env, File execDir) throws IOException {
		String[] command = new String[3];
		
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
			if (osName.startsWith("windows 9")){ //$NON-NLS-1$
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
		PluginLogger.debug("Running command: " + shellCommand +  //$NON-NLS-1$
				" with env: " + Arrays.toString(env) +  //$NON-NLS-1$
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