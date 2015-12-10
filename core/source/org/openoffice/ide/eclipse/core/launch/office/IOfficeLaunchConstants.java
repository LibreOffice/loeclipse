/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2010 by Dan Corneanu
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
 * The Initial Developer of the Original Code is: Dan Corneanu.
 *
 * Copyright: 2010 by Dan Corneanu
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.openoffice.ide.eclipse.core.launch.office;

import org.openoffice.ide.eclipse.core.OOEclipsePlugin;

/**
 * Just a bunch of constants.
 *
 * @author cdan
 *
 */
public interface IOfficeLaunchConstants {

    String PROJECT_NAME = OOEclipsePlugin.OOECLIPSE_PLUGIN_ID + ".PROJECT_ATTR";
    String CLEAN_USER_INSTALLATION = OOEclipsePlugin.OOECLIPSE_PLUGIN_ID + ".USE_CLEAN_USER_INSTALLATION_ATTR";
    String CONTENT_PATHS = OOEclipsePlugin.OOECLIPSE_PLUGIN_ID + ".PACKAGE_CONTENT_PATHS";
    String PATHS_SEPARATOR = ":";

}
