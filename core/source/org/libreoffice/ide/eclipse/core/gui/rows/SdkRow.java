/*************************************************************************
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
package org.libreoffice.ide.eclipse.core.gui.rows;

import java.util.Vector;

import org.eclipse.swt.widgets.Composite;
import org.libreoffice.ide.eclipse.core.gui.AbstractTable;
import org.libreoffice.ide.eclipse.core.gui.SDKTable;
import org.libreoffice.ide.eclipse.core.model.SDKContainer;
import org.libreoffice.ide.eclipse.core.model.config.IConfigListener;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;

/**
 * Row displaying the selection of an SDK instance.
 */
public class SdkRow extends AbstractConfigRow {

    /**
     * Constructor.
     *
     * @param pParent
     *            the composite where to create the row
     * @param pProperty
     *            the property for the row events
     * @param pToSelect
     *            the configuration element to select first
     */
    public SdkRow(final Composite pParent, String pProperty, ISdk pToSelect) {
        super(pParent, pProperty, Messages.getString("SdkRow.Browse"), pToSelect); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addListener(IConfigListener configListener) {
        SDKContainer.addListener(configListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeListener(IConfigListener configListener) {
        SDKContainer.removeListener(configListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractTable createTable(Composite pParent) {
        SDKTable table = new SDKTable(pParent);
        table.getPreferences();

        return table;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getConfigValues() {
        // Adding the SDK names to the combo box
        String[] sdks = new String[SDKContainer.getSDKCount()];
        Vector<String> sdkKeys = SDKContainer.getSDKKeys();
        for (int i = 0, length = SDKContainer.getSDKCount(); i < length; i++) {
            sdks[i] = sdkKeys.get(i);
        }
        sdkKeys.clear();

        return sdks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRowLabel() {
        return Messages.getString("SdkRow.Label"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getElementName(Object pToSelect) {
        return ((ISdk) pToSelect).getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTableDialogTitle() {
        return Messages.getString("SdkRow.DialogTitle"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void savePreferences() {
        SDKContainer.saveSDKs();
    }

}
