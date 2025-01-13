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
package org.libreoffice.ide.eclipse.core.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.gui.rows.LabeledRow;
import org.libreoffice.ide.eclipse.core.gui.rows.OOoRow;
import org.libreoffice.ide.eclipse.core.gui.rows.SdkRow;
import org.libreoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.libreoffice.ide.eclipse.core.model.OOoContainer;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.SDKContainer;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;

/**
 * The project preference page. This page can be used to reconfigure the project OOo and SDK.
 *
 */
public class ProjectPropertiesPage extends PropertyPage implements IWorkbenchPropertyPage {

    private static final String SDK = "__sdk"; //$NON-NLS-1$
    private static final String OOO = "__ooo"; //$NON-NLS-1$

    private SdkRow mSdkRow;
    private OOoRow mOOoRow;

    private UnoidlProject mProject;

    /**
     * Default constructor setting configuration listeners.
     */
    public ProjectPropertiesPage() {
        super();

        noDefaultAndApplyButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {

        mOOoRow.dispose();
        mSdkRow.dispose();

        super.dispose();
    }

    // ------------------------------------------------------- Content managment

    /**
     * {@inheritDoc}
     */
    @Override
    public void setElement(IAdaptable element) {
        super.setElement(element);

        try {
            IProject prj = element.getAdapter(IProject.class);
            if (prj != null) {
                mProject = (UnoidlProject) ProjectsManager.getProject(prj.getName());
            }
        } catch (Exception e) {
            PluginLogger.debug(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createContents(Composite pParent) {

        Composite body = new Composite(pParent, SWT.NONE);
        body.setLayout(new GridLayout(LabeledRow.LAYOUT_COLUMNS, false));

        // Add the OOo choice field
        mOOoRow = new OOoRow(body, OOO, mProject.getOOo());

        // Add the SDK choice field
        mSdkRow = new SdkRow(body, SDK, mProject.getSdk());

        return body;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performOk() {
        saveValues();
        return true;
    }

    /**
     * Convenience method to save the SDK and OOo values in their plugin configuration file.
     */
    private void saveValues() {
        if (!mSdkRow.getValue().equals("")) { //$NON-NLS-1$
            ISdk sdk = SDKContainer.getSDK(mSdkRow.getValue());
            mProject.setSdk(sdk);
        }

        if (!mOOoRow.getValue().equals("")) { //$NON-NLS-1$
            IOOo ooo = OOoContainer.getOOo(mOOoRow.getValue());
            mProject.setOOo(ooo);
        }
        mProject.saveAllProperties();
    }
}
