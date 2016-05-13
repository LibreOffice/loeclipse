/*************************************************************************
 *
 * $RCSfile: NewServiceWizardPage.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:47 $
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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.libreoffice.ide.eclipse.core.gui.rows.TypeRow;
import org.libreoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.UnoFactoryData;
import org.libreoffice.ide.eclipse.core.wizards.Messages;

/**
 * Service creation wizard page.
 *
 */
public class NewServiceWizardPage extends NewScopedElementWizardPage {

    private static final String P_IFACE_INHERITANCE = "__iface_inheritance"; //$NON-NLS-1$

    private TypeRow mIfaceInheritanceRow;
    private String mInheritedInterface;

    /**
     * Variable indicating that the inherited interface field value is being changed by the page API.
     */
    private boolean mChanging = false;

    /**
     * Variable indicating that the inherited interface has been changed by the user since the last definition using the
     * page API.
     */
    private boolean mInheritanceChanged = false;

    /**
     * Simple constructor setting the package root and element name to blank values.
     *
     * @param pPageName
     *            the page name
     * @param pProject
     *            the project where to create the service
     */
    public NewServiceWizardPage(String pPageName, IUnoidlProject pProject) {
        super(pPageName, pProject);
    }

    /**
     * Constructor setting allowing to set custom root package and service name.
     *
     * @param pPageName
     *            the page name
     * @param pProject
     *            the project where to create the service
     * @param pRootName
     *            the project root namespace
     * @param pServiceName
     *            the default service name
     */
    public NewServiceWizardPage(String pPageName, IUnoidlProject pProject, String pRootName, String pServiceName) {
        super(pPageName, pProject, pRootName, pServiceName);
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
    public void createSpecificControl(Composite pParent) {

        mIfaceInheritanceRow = new TypeRow(pParent, P_IFACE_INHERITANCE,
            Messages.getString("NewServiceWizardPage.InheritedInterface"), //$NON-NLS-1$
            IUnoFactoryConstants.INTERFACE);
        if (mInheritedInterface != null) {
            mIfaceInheritanceRow.setValue(mInheritedInterface);
        }
        mIfaceInheritanceRow.setFieldChangedListener(this);
        mIfaceInheritanceRow.setTooltip(Messages.getString("NewServiceWizardPage.InheritanceTooltip")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return Messages.getString("NewServiceWizardPage.Title"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return Messages.getString("NewServiceWizardPage.ServiceDescription"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTypeLabel() {
        return Messages.getString("NewServiceWizardPage.Type"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ImageDescriptor getImageDescriptor() {
        return OOEclipsePlugin.getImageDescriptor(ImagesConstants.NEW_SERVICE_IMAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fieldChanged(FieldEvent pEvent) {
        super.fieldChanged(pEvent);

        if (pEvent.getProperty().equals(P_IFACE_INHERITANCE) && !mChanging) {
            mInheritanceChanged = true;
        }
    }

    /**
     * Gets the name of the exported interface.
     *
     * @return the fully qualified name of the exported interface separated with "::"
     */
    public String getInheritanceName() {
        String result = ""; //$NON-NLS-1$
        if (mIfaceInheritanceRow != null) {
            result = mIfaceInheritanceRow.getValue();
        }
        return result;
    }

    /**
     * Sets the name of the exported interface.
     *
     * <p>
     * Use this method to impose the service to implement a particular interface. This is the case for an URE
     * application.
     * </p>
     *
     * @param pValue
     *            the interface fully qualified name
     * @param pForced
     *            disables the field if <code>true</code>
     */
    public void setInheritanceName(String pValue, boolean pForced) {

        if (pValue.matches("([a-zA-Z][a-zA-Z0-9]*)(::[a-zA-Z][a-zA-Z0-9]*)*")) { //$NON-NLS-1$

            if (mIfaceInheritanceRow != null) {
                mChanging = true;

                mIfaceInheritanceRow.setValue(pValue);
                mIfaceInheritanceRow.setEnabled(!pForced);
                mInheritanceChanged = false;

                mChanging = false;
            } else {
                mInheritedInterface = pValue;
            }
        }
    }

    /**
     * Tells whether the user has changed the exported interface since it has last been set using the APIs.
     *
     * @return <code>true</code> is the has changed the exported interface.
     */
    public boolean isInheritanceChanged() {
        return mInheritanceChanged;
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
            pData.setProperty(IUnoFactoryConstants.TYPE_NATURE, Integer.valueOf(IUnoFactoryConstants.SERVICE));
            pData.setProperty(IUnoFactoryConstants.INHERITED_INTERFACES,
                new String[] { getInheritanceName().replace(".", "::") }); //$NON-NLS-1$ //$NON-NLS-2$
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
            typeData.setProperty(IUnoFactoryConstants.TYPE_NATURE, Integer.valueOf(IUnoFactoryConstants.SERVICE));
        }
        return typeData;
    }
}
