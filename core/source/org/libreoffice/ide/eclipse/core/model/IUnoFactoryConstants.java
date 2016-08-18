/*************************************************************************
 *
 * $RCSfile: IUnoFactoryConstants.java,v $
 *
 * $Revision: 1.8 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:50 $
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
package org.libreoffice.ide.eclipse.core.model;

import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;
import org.libreoffice.ide.eclipse.core.model.language.AbstractLanguage;

/**
 * This interface contains the keys for the data to provide to the UNO factory. This interface shouldn't define any
 * language specific data key.
 */
public interface IUnoFactoryConstants {

    // Project keys
    /**
     * The object should be an instance of <code>IProject</code>.
     */
    public static final String PROJECT_HANDLE = "project_handle"; //$NON-NLS-1$

    public static final String PROJECT_PATH = "project_path"; // $NON-NSL-1$ //$NON-NLS-1$

    public static final String PROJECT_NAME = "project_name"; // $NON-NSL-1$ //$NON-NLS-1$

    /**
     * The object should be a dot-separated string.
     */
    public static final String PROJECT_PREFIX = "project_prefix"; //$NON-NLS-1$

    /**
     * The object should be a single word.
     */
    public static final String PROJECT_COMP = "project_comp"; //$NON-NLS-1$

    /**
     * The object should be an instance of {@link AbstractLanguage}.
     */
    public static final String PROJECT_LANGUAGE = "project_language"; //$NON-NLS-1$

    /**
     * The object should be an instance of {@link ISdk}.
     */
    public static final String PROJECT_SDK = "project_sdk"; //$NON-NLS-1$

    /**
     * The object should be an instance of {@link IOOo}.
     */
    public static final String PROJECT_OOO = "project_ooo"; //$NON-NLS-1$

    // Type keys
    /**
     * The object is a "::"-separated string.
     */
    public static final String PACKAGE_NAME = "package_name"; //$NON-NLS-1$

    /**
     * The object is a single word.
     */
    public static final String TYPE_NAME = "type_name"; //$NON-NLS-1$

    /**
     * The object is an array of "::"-separated strings.
     */
    public static final String INHERITED_INTERFACES = "inherited_interfaces"; //$NON-NLS-1$

    /**
     * The object is an array of "::"-separated strings.
     */
    public static final String OPT_INHERITED_INTERFACES = "opt_inherited_interfaces"; //$NON-NLS-1$

    /**
     * The object is a <code>Boolean</code>.
     */
    public static final String TYPE_PUBLISHED = "type_published"; //$NON-NLS-1$

    /**
     * The object defines which UNO type the data is describing: a module, an interface, a service...
     */
    public static final String TYPE_NATURE = "type_nature"; //$NON-NLS-1$

    public static final int MODULE = 1;
    public static final int INTERFACE = 2;
    public static final int SERVICE = 4;
    public static final int STRUCT = 8;
    public static final int ENUM = 16;
    public static final int EXCEPTION = 32;
    public static final int TYPEDEF = 64;
    public static final int CONSTANT = 128;
    public static final int CONSTANTS = 256;
    public static final int SINGLETON = 512;
    public static final int BASICS = 1024;

    /**
     * The object is a String corresponding to the returned type of a method or the type of an attribute.
     */
    public static final String TYPE = "type"; //$NON-NLS-1$

    /**
     * The object is an integer defining whether the data are describing an attribute or a method.
     */
    public static final String MEMBER_TYPE = "member_type"; //$NON-NLS-1$

    public static final int ATTRIBUTE = 1;
    public static final int METHOD = 2;

    /**
     * The object is the data name.
     */
    public static final String NAME = "name"; //$NON-NLS-1$

    /**
     * The object is a string containing the different flags of attributes, properties and methods separated by a space.
     */
    public static final String FLAGS = "flags"; //$NON-NLS-1$

    /**
     * The object is a string among <code>in</code>, <code>out</code> and <code>inout</code> defining the direction of a
     * method parameter.
     */
    public static final String ARGUMENT_INOUT = "argument_inout"; //$NON-NLS-1$

    /**
     * Project property setting the directory where the sources are located.
     */
    public static final String PROJECT_SRC_DIR = "project_src_dir"; //$NON-NLS-1$

    /**
     * Project property setting the directory where the idl files are located.
     */
    public static final String PROJECT_IDL_DIR = "project_idl_dir"; //$NON-NLS-1$
}
