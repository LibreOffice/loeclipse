/*************************************************************************
 *
 * $RCSfile: IUnoidlSyntax.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/08/20 11:55:50 $
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
package org.openoffice.ide.eclipse.core.editors.syntax;

/**
 * <p>This class only defines the UNO-IDL main keywords in fonction of their 
 * use in the file. They are devided into
 * 	<ul>
 * 		<li>Reserved words</li>
 * 		<li>IDL modifiers usually written in brackets</li>
 * 		<li>constants like the boolean <code>True</code></li>
 * 		<li>UNO-IDL types</li>
 * 	</ul>
 * </p>
 * 
 * @author cbosdonnat
 *
 */
public interface IUnoidlSyntax {
	
	/**
	 * The UNO-IDL reserved words: they will be rendered as keywords
	 */
	public static final String[] RESERVED_WORDS = {
		"published",      // new with the OpenOffice.org 2.0 SDK  //$NON-NLS-1$
		"get",            // new with the OpenOffice.org 2.0 SDK  //$NON-NLS-1$
		"set",			  // new with the OpenOffice.org 2.0 SDK //$NON-NLS-1$
		"service", //$NON-NLS-1$
		"singleton", //$NON-NLS-1$
		"type", //$NON-NLS-1$
        "module", //$NON-NLS-1$
        "interface", //$NON-NLS-1$
        "struct", //$NON-NLS-1$
        "const", //$NON-NLS-1$
        "constants", //$NON-NLS-1$
        "exception", //$NON-NLS-1$
        "enum", //$NON-NLS-1$
        "raises", //$NON-NLS-1$
        "typedef" //$NON-NLS-1$
	};
	
	/**
	 * UNO-IDL modifiers, usually written in brackets
	 */
	public static final String[] MODIFIERS = {
		"bound", //$NON-NLS-1$
		"constrained", //$NON-NLS-1$
		"maybeambiguous", //$NON-NLS-1$
		"maybedefault", //$NON-NLS-1$
		"maybevoid", //$NON-NLS-1$
        "oneway", //$NON-NLS-1$
		"optional", //$NON-NLS-1$
        "readonly", //$NON-NLS-1$
        "in", "out", "inout", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        "attribute", //$NON-NLS-1$
		"transient", //$NON-NLS-1$
		"removable" //$NON-NLS-1$
	};

	/**
	 * UNO-IDL constants
	 */
	public static final String[] CONSTANTS = {
		"TRUE", //$NON-NLS-1$
		"True", //$NON-NLS-1$
		"FALSE", //$NON-NLS-1$
		"False" //$NON-NLS-1$
	};
	
	/**
	 * UNO-IDL types
	 */
	public static final String[] TYPES = {
        "string", //$NON-NLS-1$
        "short", //$NON-NLS-1$
        "long", //$NON-NLS-1$
        "byte", //$NON-NLS-1$
        "hyper", //$NON-NLS-1$
        "float", //$NON-NLS-1$
        "boolean", //$NON-NLS-1$
        "any", //$NON-NLS-1$
        "char", //$NON-NLS-1$
        "double", //$NON-NLS-1$
        "long", //$NON-NLS-1$
        "void", //$NON-NLS-1$
        "sequence", //$NON-NLS-1$
        "unsigned", //$NON-NLS-1$
        "...", //$NON-NLS-1$
        "any..."       // in the uno idl grammar, there could be a space, or not //$NON-NLS-1$
	};
}
