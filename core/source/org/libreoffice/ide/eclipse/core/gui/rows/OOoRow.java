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
import org.libreoffice.ide.eclipse.core.gui.OOoTable;
import org.libreoffice.ide.eclipse.core.model.OOoContainer;
import org.libreoffice.ide.eclipse.core.model.config.IConfigListener;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;

/**
 * Row displaying the selection of an OOo instance.
 */
public class OOoRow extends AbstractConfigRow {

    /**
     * Constructor.
     *
     * @param parent
     *            the composite where to create the row
     * @param property
     *            the property for the row events
     * @param toSelect
     *            the configuration element to select first
     */
    public OOoRow(final Composite parent, String property, IOOo toSelect) {
        super(parent, property, Messages.getString("OOoRow.Browse"), toSelect); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addListener(IConfigListener configListener) {
        OOoContainer.addListener(configListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeListener(IConfigListener configListener) {
        OOoContainer.removeListener(configListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRowLabel() {
        return Messages.getString("OOoRow.Label"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getConfigValues() {
        // Adding the OOo names to the combo box
        String[] ooos = new String[OOoContainer.getOOoCount()];
        Vector<String> oooKeys = OOoContainer.getOOoKeys();
        for (int i = 0, length = OOoContainer.getOOoCount(); i < length; i++) {
            ooos[i] = oooKeys.get(i);
        }
        oooKeys.clear();

        return ooos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getElementName(Object toSelect) {
        return ((IOOo) toSelect).getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTableDialogTitle() {
        return Messages.getString("OOoRow.DialogTitle"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractTable createTable(Composite parent) {
        OOoTable table = new OOoTable(parent);
        table.getPreferences();

        return table;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void savePreferences() {
        OOoContainer.saveOOos();
    }
}
