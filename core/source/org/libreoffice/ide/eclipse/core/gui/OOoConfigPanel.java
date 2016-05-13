/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Novell, Inc.
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
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2009 by Novell, Inc.
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.gui.rows.FileRow;
import org.libreoffice.ide.eclipse.core.model.OOoContainer;
import org.libreoffice.ide.eclipse.core.model.SDKContainer;
import org.libreoffice.ide.eclipse.core.model.config.InvalidConfigException;

/**
 * Class providing the LibreOffice and SDK configuration rows.
 *
 * @author cbosdonnat
 *
 */
public class OOoConfigPanel {

    private static final int GRID_COLUMNS = 3;

    private FileRow mLibreOfficeFileRow;
    private FileRow mSdkFileRow;
    
    private static final String P_SDK_PATH = "__sdk_path";
    private static final String P_LIBREOFFICE_PATH = "__libreoffice_path";

    /**
     * Constructor.
     *
     * @param pParent
     *            the parent composite where to create the fields
     */
    public OOoConfigPanel(Composite pParent) {
        Group group = new Group(pParent, SWT.NONE);
        group.setText(Messages.getString("OOoConfigPanel.GroupTitle")); //$NON-NLS-1$
        group.setLayout(new GridLayout(GRID_COLUMNS, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        
        mLibreOfficeFileRow = new FileRow(group, P_LIBREOFFICE_PATH, Messages.getString("OOoTable.PathTitle"), true);
        mSdkFileRow = new FileRow(group, P_SDK_PATH, Messages.getString("SDKTable.PathTitle"), true);
        
        String libreofficePath = OOoContainer.getOOo().getHome();
        if (!libreofficePath.isEmpty())
            mLibreOfficeFileRow.setValue(libreofficePath);
        String sdkPath = SDKContainer.getSDK().getHome();
        if (!sdkPath.isEmpty())
            mSdkFileRow.setValue(sdkPath);
    }
    
    public boolean saveConfiguration() {
        try {
            OOoContainer.setLibreOfficePath(mLibreOfficeFileRow.getValue());
            SDKContainer.setSdkPath(mSdkFileRow.getValue());
        } catch (InvalidConfigException e) {
            PluginLogger.error("Could not set LibreOffice/SDK.\n" + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * @return SDK name selected
     */
    public String getSDKName() {
        return mSdkFileRow.getValue();
    }

    /**
     * @return OOo name selected
     */
    public String getOOoName() {
        return mLibreOfficeFileRow.getValue();
    }
}
