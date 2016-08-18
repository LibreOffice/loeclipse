/*************************************************************************
 *
 * $RCSfile: UnoSDKConfigPage.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:27 $
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
package org.libreoffice.ide.eclipse.core.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.libreoffice.ide.eclipse.core.gui.OOoTable;
import org.libreoffice.ide.eclipse.core.gui.SDKTable;

/**
 * Preference page to configure the plugin available OOo and SDK instances.
 */
public class UnoSDKConfigPage extends PreferencePage implements IWorkbenchPreferencePage {

    private SDKTable mSdkTable;
    private OOoTable mOOoTable;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createContents(Composite pParent) {
        noDefaultAndApplyButton();

        mOOoTable = new OOoTable(pParent);
        mOOoTable.getPreferences();
        
        mSdkTable = new SDKTable(pParent);
        mSdkTable.getPreferences();

        return pParent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performOk() {
        mSdkTable.savePreferences();
        mOOoTable.savePreferences();

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        mSdkTable.dispose();
        mOOoTable.dispose();
        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IWorkbench pWorkbench) {
    }
}
