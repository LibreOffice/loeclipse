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
package org.libreoffice.ide.eclipse.core.wizards.pages;

import java.util.Vector;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.gui.ITableElement;
import org.libreoffice.ide.eclipse.core.gui.rows.LabeledRow;
import org.libreoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.UnoFactoryData;
import org.libreoffice.ide.eclipse.core.wizards.Messages;

/**
 * Interface creation page.
 */
public class NewInterfaceWizardPage extends NewScopedElementWizardPage implements ISelectionChangedListener {

    private static final int MAX_HEIGHT = 600;
    private static final int MIN_WIDTH = 600;
    private InterfacesTable mInterfaceInheritances;
    private InterfaceMembersTable mMembers;

    /**
     * Constructor.
     *
     * @param pPageName
     *            the page name
     * @param pUnoProject
     *            the project for which to create the interface.
     */
    public NewInterfaceWizardPage(String pPageName, IUnoidlProject pUnoProject) {
        super(pPageName, pUnoProject);
    }

    /**
     * Constructor.
     *
     * @param pPageName
     *            the page name
     * @param pProject
     *            the project for which to create the interface.
     * @param pRootName
     *            scoped name of the module containing the type
     * @param pElementName
     *            name of the type, without any '.' or '::'
     */
    public NewInterfaceWizardPage(String pPageName, IUnoidlProject pProject, String pRootName, String pElementName) {
        super(pPageName, pProject, pRootName, pElementName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {

        mInterfaceInheritances.removeSelectionChangedListener(this);

        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getProvidedTypes() {
        return IUnoFactoryConstants.INTERFACE;
    }

    // --------------------------------------------------- Page content managment

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createSpecificControl(Composite pParent) {

        // To get correct table sizes
        Point point = getShell().getSize();
        point.y = Math.max(point.y, MAX_HEIGHT);
        point.x = Math.min(point.x, MIN_WIDTH);
        getShell().setSize(point);

        Composite tableParent = new Composite(pParent, SWT.NORMAL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = LabeledRow.LAYOUT_COLUMNS;
        tableParent.setLayoutData(gd);
        tableParent.setLayout(new GridLayout(1, false));

        mInterfaceInheritances = new InterfacesTable(tableParent);
        mInterfaceInheritances.setToolTipText(Messages.getString("NewInterfaceWizardPage.InheritancesTableTooltip")); //$NON-NLS-1$
        mInterfaceInheritances.addSelectionChangedListener(this);

        mMembers = new InterfaceMembersTable(tableParent);
        mMembers.setToolTipText(Messages.getString("NewInterfaceWizardPage.MembersTableTooltip")); //$NON-NLS-1$
        mMembers.addSelectionChangedListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return Messages.getString("NewInterfaceWizardPage.Title"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return Messages.getString("NewInterfaceWizardPage.InterfaceDescription"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTypeLabel() {
        return Messages.getString("NewInterfaceWizardPage.Label"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ImageDescriptor getImageDescriptor() {
        return OOEclipsePlugin.getImageDescriptor(ImagesConstants.NEW_INTERFACE_IMAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectionChanged(SelectionChangedEvent pEvent) {
        setPageComplete(isPageComplete());
    }

    /**
     * @param pData
     *            the data to fill
     *
     * @return the given data with the completed properties, <code>null</code> if the provided data is <code>null</code>
     */
    @Override
    public UnoFactoryData fillData(UnoFactoryData pData) {
        pData = super.fillData(pData);
        if (pData != null) {
            pData.setProperty(IUnoFactoryConstants.TYPE_NATURE, Integer.valueOf(IUnoFactoryConstants.INTERFACE));

            // Vector containing the interface inheritance paths "::" separated
            Vector<String> optionalIntf = new Vector<String>();
            Vector<String> mandatoryIntf = new Vector<String>();

            // Separate the optional and mandatory interface inheritances
            if (mInterfaceInheritances != null) {
                Vector<ITableElement> lines = mInterfaceInheritances.getLines();
                for (ITableElement linei : lines) {
                    InterfacesTable.InheritanceLine line = (InterfacesTable.InheritanceLine) linei;

                    if (line.isOptional()) {
                        optionalIntf.add(line.getInterfaceName().replace(".", "::")); //$NON-NLS-1$ //$NON-NLS-2$
                    } else {
                        mandatoryIntf.add(line.getInterfaceName().replace(".", "::")); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                lines.clear();
            }

            // Get the mandatory inheritances
            String[] interfaces = new String[mandatoryIntf.size()];
            interfaces = mandatoryIntf.toArray(interfaces);
            pData.setProperty(IUnoFactoryConstants.INHERITED_INTERFACES, interfaces);

            // Get the optional inheritances
            String[] opt_interfaces = new String[optionalIntf.size()];
            opt_interfaces = optionalIntf.toArray(opt_interfaces);
            pData.setProperty(IUnoFactoryConstants.OPT_INHERITED_INTERFACES, opt_interfaces);

            optionalIntf.clear();
            mandatoryIntf.clear();

            // Get the interface members data
            if (mMembers != null) {
                UnoFactoryData[] membersData = mMembers.getUnoFactoryData();
                for (UnoFactoryData member : membersData) {
                    pData.addInnerData(member);
                }
            }
        }
        return pData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnoFactoryData getEmptyTypeData() {
        UnoFactoryData typeData = new UnoFactoryData();

        if (typeData != null) {
            typeData.setProperty(IUnoFactoryConstants.TYPE_NATURE, Integer.valueOf(IUnoFactoryConstants.INTERFACE));
        }
        return typeData;
    }
}
