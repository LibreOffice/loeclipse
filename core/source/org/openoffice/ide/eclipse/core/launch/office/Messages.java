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

import org.eclipse.osgi.util.NLS;

/**
 * Messages for the package.
 *
 * @author cdan
 *
 */
public class Messages extends NLS {

    public static String OfficeLaunchDelegate_LaunchError;
    public static String OfficeLaunchDelegate_LaunchErrorTitle;
    public static String OfficeTab_Options;
    public static String OfficeTab_Configurationerror;
    public static String OfficeTab_ProjectNameLabel;
    public static String OfficeTab_Title;
    public static String OfficeTab_UnoProject;
    public static String OfficeTab_ChkUseCleanUserInstallation;
    public static String OfficeTab_ChkUseCleanUserInstallation_ToolTip;
    public static String OfficeTab_ProjectChooserTitle;
    public static String OfficeTab_ProjectChooserMessage;

    private static final String BUNDLE_NAME = "org.openoffice.ide.eclipse.core.launch.office.messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    /**
     * Private constructor.
     */
    private Messages() {
    }
}
