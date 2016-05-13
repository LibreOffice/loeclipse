/*************************************************************************
 *
 * $RCSfile: InterfacesTable.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2009/04/20 06:16:01 $
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
package org.libreoffice.ide.eclipse.core.wizards.pages;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.gui.AbstractTable;
import org.libreoffice.ide.eclipse.core.gui.ITableElement;
import org.libreoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.unotypebrowser.InternalUnoType;
import org.libreoffice.ide.eclipse.core.unotypebrowser.UnoTypeBrowser;
import org.libreoffice.ide.eclipse.core.wizards.Messages;

/**
 * This class corresponds to the table of interface inheritances. The add action launches the UNO Type browser to select
 * one interface. This class shouldn't be subclassed.
 *
 */
public class InterfacesTable extends AbstractTable {

    private static final int OPTIONAL_WIDTH = 25;
    private static final int NAME_WIDTH = 400;

    /**
     * Simplified constructor for this kind of table.
     *
     * @param pParent
     *            the parent composite where to put the table
     */
    public InterfacesTable(Composite pParent) {
        super(pParent, Messages.getString("InterfacesTable.Title"), //$NON-NLS-1$
            new String[] { Messages.getString("InterfacesTable.OptionalTitle"), //$NON-NLS-1$
                Messages.getString("InterfacesTable.NameTitle") //$NON-NLS-1$
        }, new int[] { OPTIONAL_WIDTH, NAME_WIDTH },
            new String[] { InheritanceLine.OPTIONAL, InheritanceLine.NAME });
    }

    /**
     * Add a new interface in the table.
     *
     * @param pIfaceName
     *            the name of the interface to add
     * @param pOptional
     *            <code>true</code> if the interface is optional.
     */
    public void addInterface(String pIfaceName, boolean pOptional) {
        InheritanceLine line = new InheritanceLine();
        line.mInterfaceName = pIfaceName;
        line.mOptional = pOptional;

        addLine(line);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CellEditor[] createCellEditors(Table pTable) {
        CellEditor[] editors = new CellEditor[] { new CheckboxCellEditor(), null };

        return editors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ITableElement addLine() {
        ITableElement line = null;

        // Launching the UNO Type Browser
        UnoTypeBrowser browser = new UnoTypeBrowser(getShell(), IUnoFactoryConstants.INTERFACE);
        if (Window.OK == browser.open()) {

            String value = null;

            InternalUnoType selectedType = browser.getSelectedType();
            if (null != selectedType) {
                value = selectedType.getFullName();
            }

            // Creates the line only if OK has been pressed
            line = new InheritanceLine();
            ((InheritanceLine) line).setInterfaceName(value);
        }

        return line;
    }

    /**
     * The interface names are stored in path-like strings, ie: using "::" as separator. This class describes a line in
     * the table and thus has to implement {@link ITableElement} interface
     *
     *
     */
    public class InheritanceLine implements ITableElement {

        public static final String OPTIONAL = "__optional"; //$NON-NLS-1$
        public static final String NAME = "__name"; //$NON-NLS-1$

        private String mInterfaceName;
        private boolean mOptional = false;

        // ----------------------------------------------------- Member managment

        /**
         * @return the interface name
         */
        public String getInterfaceName() {
            return mInterfaceName;
        }

        /**
         * @return <code>true</code> if the inheritance is optional
         */
        public boolean isOptional() {
            return mOptional;
        }

        /**
         * Set the interface name.
         *
         * @param pInterfaceName
         *            the interface name of the inheritance
         */
        public void setInterfaceName(String pInterfaceName) {
            this.mInterfaceName = pInterfaceName;
        }

        /**
         * Set whether the inheritance is optional or not.
         *
         * @param pOptional
         *            <code>true</code> if the inheritance is optional.
         */
        public void setOptional(boolean pOptional) {
            this.mOptional = pOptional;
        }

        // ----------------------------------------- ITableElement implementation

        /**
         * {@inheritDoc}
         */
        @Override
        public Image getImage(String pProperty) {
            Image image = null;

            if (pProperty.equals(OPTIONAL)) {
                if (isOptional()) {
                    image = OOEclipsePlugin.getImage(ImagesConstants.CHECKED);
                } else {
                    image = OOEclipsePlugin.getImage(ImagesConstants.UNCHECKED);
                }
            }
            return image;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getLabel(String pProperty) {
            String label = null;

            if (pProperty.equals(NAME)) {
                label = getInterfaceName().toString();
            }
            return label;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String[] getProperties() {
            return new String[] { OPTIONAL, NAME };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canModify(String pProperty) {

            return pProperty.equals(OPTIONAL);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValue(String pProperty) {
            Object result = null;

            if (pProperty.equals(OPTIONAL)) {
                result = Boolean.valueOf(isOptional());
            }
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValue(String pProperty, Object pValue) {

            if (pProperty.equals(OPTIONAL) && pValue instanceof Boolean) {

                setOptional(((Boolean) pValue).booleanValue());
            }
        }
    }
}
