/*************************************************************************
 *
 * $RCSfile: InterfaceMembersTable.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:29 $
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

import java.util.Vector;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.gui.AbstractTable;
import org.libreoffice.ide.eclipse.core.gui.ITableElement;
import org.libreoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.model.UnoFactoryData;
import org.libreoffice.ide.eclipse.core.wizards.Messages;

/**
 * Table representing the interface members in the interface wizard page.
 *
 *
 */
public class InterfaceMembersTable extends AbstractTable {

    private static final String TYPE = "__type"; //$NON-NLS-1$
    private static final String NAME = "__name"; //$NON-NLS-1$
    private static final String OPTIONS = "__options"; //$NON-NLS-1$
    private static final int NAME_WIDTH = 100;
    private static final int TYPE_WIDTH = 50;
    private static final int OPTIONS_WIDTH = 300;

    /**
     * Creates a table to add/edit/remove the attributes and methods of an interface.
     *
     * @param pParent
     *            the parent composite where to create the table. Its layout should be a Grid Layout with one column
     */
    public InterfaceMembersTable(Composite pParent) {
        super(pParent, Messages.getString("InterfaceMembersTable.Title"), //$NON-NLS-1$
            new String[] { Messages.getString("InterfaceMembersTable.NameColumnTitle"), //$NON-NLS-1$
                Messages.getString("InterfaceMembersTable.TypeColumnTitle"), //$NON-NLS-1$
                Messages.getString("InterfaceMembersTable.FlagsColumnTitle") //$NON-NLS-1$
        }, new int[] { NAME_WIDTH, TYPE_WIDTH, OPTIONS_WIDTH }, new String[] { NAME, TYPE, OPTIONS });
    }

    /**
     * Returns an array of the defined {@link UnoFactoryData}.
     *
     * @return the created factory data
     */
    public UnoFactoryData[] getUnoFactoryData() {
        Vector<ITableElement> lines = getLines();
        int size = lines.size();
        UnoFactoryData[] data = new UnoFactoryData[size];

        for (int i = 0; i < size; i++) {
            data[i] = ((MemberLine) lines.get(i)).mData;
        }
        return data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ITableElement addLine() {
        MemberLine result = null;
        UnoFactoryData data = openDialog(null);
        if (data != null) {
            result = new MemberLine(data);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleDoubleClick(DoubleClickEvent pEvent) {

        // Open the Member dialog but freeze the member type
        super.handleDoubleClick(pEvent);

        if (getSelection() instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) getSelection();
            Object o = selection.getFirstElement();
            if (o instanceof MemberLine) {
                MemberLine line = (MemberLine) o;
                UnoFactoryData data = openDialog(line.mData);
                line.mData = data;
                mTableViewer.refresh(line);
            }
        }
    }

    /**
     * Open the member dialog for edition or creation.
     *
     * @param pContent
     *            if <code>null</code>, the dialog is opened to create a new member, otherwise it reuses the given data
     *            to modify them.
     *
     * @return the created or edited data
     */
    protected UnoFactoryData openDialog(UnoFactoryData pContent) {
        InterfaceMemberDialog dlg;
        UnoFactoryData result = pContent;

        if (pContent == null) {
            dlg = new InterfaceMemberDialog();
        } else {
            dlg = new InterfaceMemberDialog(pContent);
        }

        if (Window.OK == dlg.open()) {
            result = dlg.getData();
        } else {
            if (pContent == null) {
                dlg.disposeData();
            }
        }
        return result;
    }

    /**
     * This class defines the model of the member lines.
     *
     * @see AbstractTable
     */
    class MemberLine implements ITableElement {

        private UnoFactoryData mData;

        /**
         * This constructor instanciates an UnoFactoryData, keep in mind that these should be disposed.
         */
        public MemberLine() {
            mData = new UnoFactoryData();
        }

        /**
         * This constructor only makes a reference copy of the data, don't dispose them too early.
         *
         * @param pData
         *            the data for the line
         */
        public MemberLine(UnoFactoryData pData) {
            mData = pData;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canModify(String pProperty) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Image getImage(String pProperty) {
            Image image = null;
            if (pProperty.equals(NAME)) {
                int memberType = ((Integer) mData.getProperty(IUnoFactoryConstants.MEMBER_TYPE)).intValue();
                if (memberType == IUnoFactoryConstants.ATTRIBUTE) {
                    image = OOEclipsePlugin.getImage(ImagesConstants.ATTRIBUTE);
                } else if (memberType == IUnoFactoryConstants.METHOD) {
                    image = OOEclipsePlugin.getImage(ImagesConstants.METHOD);
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

            if (pProperty.equals(TYPE)) {
                String type = (String) mData.getProperty(IUnoFactoryConstants.TYPE);
                label = type;
            } else if (pProperty.equals(NAME)) {
                String name = (String) mData.getProperty(IUnoFactoryConstants.NAME);
                label = name;
            } else if (pProperty.equals(OPTIONS)) {
                int memberType = ((Integer) mData.getProperty(IUnoFactoryConstants.MEMBER_TYPE)).intValue();
                if (memberType == IUnoFactoryConstants.ATTRIBUTE) {
                    label = (String) mData.getProperty(IUnoFactoryConstants.FLAGS);
                } else if (memberType == IUnoFactoryConstants.METHOD) {
                    UnoFactoryData[] args = mData.getInnerData();
                    label = ""; //$NON-NLS-1$
                    for (int i = 0; i < args.length; i++) {
                        String name = (String) args[i].getProperty(IUnoFactoryConstants.NAME);
                        if (name != null) {
                            label += name + " "; //$NON-NLS-1$
                        }
                    }
                }
            }

            if (label == null) {
                label = ""; //$NON-NLS-1$
            }

            return label;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String[] getProperties() {
            return new String[] { TYPE, NAME, OPTIONS };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValue(String pProperty) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValue(String pProperty, Object pValue) {
        }
    }
}
