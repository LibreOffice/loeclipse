/*************************************************************************
 *
 * $RCSfile: SystemHelper.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/26 21:33:42 $
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

import org.eclipse.core.runtime.Platform;

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
}
