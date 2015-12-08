/*************************************************************************
 * StringUtils.java
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
 * 
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 *
 * Contributor(s): oliver.boehm@agentes.de
 ************************************************************************/

package org.openoffice.plugin.core.utils;

/**
 * The commons.lang dependency was removed but we need some stuff from the
 * StringUtils class. The missing functionality is recoded here.
 * 
 * @author oliver (oliver.boehm@agentes.de)
 * @since 1.1.1 (08.12.2010)
 */
public class StringUtils {
	
	/** Utility class - no need to instantiate it. */
	private StringUtils() {}

	/**
	 * Checks if the given string is not null and not empty.
	 *
	 * @param str the string
	 * @return true, if string is not empty
	 */
	public static boolean isNotEmpty(final String str) {
		return (str != null) && (str.length() > 0);
	}

}
