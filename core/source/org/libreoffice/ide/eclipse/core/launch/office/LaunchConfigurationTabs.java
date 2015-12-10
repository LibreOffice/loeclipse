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
package org.libreoffice.ide.eclipse.core.launch.office;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;

/**
 * Creates tabs in the LibreOffice launcher.
 *
 * @author cdan
 *
 */
public class LaunchConfigurationTabs extends AbstractLaunchConfigurationTabGroup {

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTabs(ILaunchConfigurationDialog pDialog, String pMode) {
        ILaunchConfigurationTab[] tabs = null;
        if (ILaunchManager.DEBUG_MODE.equals(pMode)) {
            tabs = new ILaunchConfigurationTab[] { new OfficeTab(), new PackageConfigTab(), new SourceLookupTab(),
                new CommonTab() };
        } else {
            tabs = new ILaunchConfigurationTab[] { new OfficeTab(), new PackageConfigTab(), new CommonTab() };
        }

        setTabs(tabs);
    }

}
