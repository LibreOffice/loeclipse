/*************************************************************************
 *
 * $RCSfile: IUnoFactoryConstants.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/08/20 11:55:58 $
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
package org.openoffice.ide.eclipse.core.model;

import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;

/**
 * This interface contains the keys for the data to provide to the UNO
 * factory. This interface shouldn't define any language specific data key.
 * 
 * @author cedricbosdo
 *
 */
public interface IUnoFactoryConstants {
	

	// Project keys 
	/**
	 * The object should be an instance of <code>IProject</code>
	 */
	public final static String PROJECT_HANDLE = "project_handle"; //$NON-NLS-1$
	
	/**
	 * The object should be a dot-separated string 
	 */
	public final static String PROJECT_PREFIX = "project_prefix"; //$NON-NLS-1$
	
	/**
	 * The object should be a single word
	 */
	public final static String PROJECT_COMP	= "project_comp"; //$NON-NLS-1$
	
	/**
	 * The object should be an instance of {@link ILanguage}
	 */
	public final static String PROJECT_LANGUAGE = "project_language"; //$NON-NLS-1$
	
	/**
	 * The object should be an instance of {@link ISdk}
	 */
	public final static String PROJECT_SDK = "project_sdk"; //$NON-NLS-1$
	
	/**
	 * The object should be an instance of {@link IOOo}
	 */
	public final static String PROJECT_OOO = "project_ooo"; //$NON-NLS-1$
	
	// Type keys
	/**
	 * The object is a "::"-separated string
	 */
	public final static String PACKAGE_NAME = "package_name"; //$NON-NLS-1$
	
	/**
	 * The object is a single word
	 */
	public final static String TYPE_NAME = "type_name"; //$NON-NLS-1$
	
	/**
	 * The object is an array of "::"-separated strings
	 */
	public final static String INHERITED_INTERFACES = "inherited_interfaces"; //$NON-NLS-1$
	
	/**
	 * The object is an array of "::"-separated strings
	 */
	public final static String OPT_INHERITED_INTERFACES = "opt_inherited_interfaces"; //$NON-NLS-1$
	
	/**
	 * The object is a <code>Boolean</code>
	 */
	public final static String TYPE_PUBLISHED = "type_published"; //$NON-NLS-1$
	
	/**
	 * The object is an <code>Integer</code>
	 */
	public final static String TYPE = "type"; //$NON-NLS-1$
	
	public final static int MODULE = 1;
	public final static int INTERFACE = 2;
	public final static int SERVICE = 4;
	public final static int STRUCT = 8;
	public final static int ENUM = 16;
	public final static int EXCEPTION = 32;
	public final static int TYPEDEF = 64;
	public final static int CONSTANT = 128;
	public final static int CONSTANTS = 256;
	public final static int SINGLETON = 512;
}
