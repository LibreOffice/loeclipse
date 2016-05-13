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
package org.libreoffice.ide.eclipse.core.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.libreoffice.ide.eclipse.core.gui.ConnectionConfigPanel;
import org.libreoffice.ide.eclipse.core.gui.OOoConfigPanel;
import org.libreoffice.ide.eclipse.core.model.OOoContainer;
import org.libreoffice.ide.eclipse.core.model.SDKContainer;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;
import org.libreoffice.ide.eclipse.core.wizards.Messages;

/**
 * Base wizard page for the UNO Client configuration.
 *
 * <p>
 * This class has to be sub-classed by clients to change the pages order and set the patterns for the code to open the
 * UNO connection.
 * </p>
 *
 *
 */
public class BaseUnoConnectionPage extends WizardPage {

    private OOoConfigPanel mOOoConfigPanel;
    private ConnectionConfigPanel mCnxConfigPanel;

    /**
     * Default constructor.
     */
    public BaseUnoConnectionPage() {
        super("unocnxpage"); //$NON-NLS-1$
        setTitle(Messages.getString("BaseUnoConnectionPage.Title")); //$NON-NLS-1$
        setDescription(Messages.getString("BaseUnoConnectionPage.Description")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(Composite pParent) {

        Composite body = new Composite(pParent, SWT.NONE);
        body.setLayout(new GridLayout());
        body.setLayoutData(new GridData(GridData.FILL_BOTH));

        mOOoConfigPanel = new OOoConfigPanel(body);

        mCnxConfigPanel = new ConnectionConfigPanel(body);
        setControl(body);
    }

    /**
     * Defines the patterns of code to instantiate a pipe or socket UNO connection.
     *
     * <p>
     * For a pipe connection, the only parameter is the pipe's name.
     * </p>
     *
     * <p>
     * For a socket connection: the parameters are:
     * </p>
     * <ul>
     * <li><b>{0}</b>: the host name</li>
     * <li><b>{1}</b>: the port name</li>
     * </ul>
     *
     * @param pPipe
     *            the pattern for the pipe connection
     * @param pSocket
     *            the pattern for the socket connection
     */
    public void setConnectionPatterns(String pPipe, String pSocket) {
        mCnxConfigPanel.setPatterns(pPipe, pSocket);
    }

    /**
     * @return the selected OOo instance
     */
    public IOOo getOoo() {
        return OOoContainer.getOOo();
    }

    /**
     * @return the selected SDK instance
     */
    public ISdk getSdk() {
        return SDKContainer.getSDK();
    }

    /**
     * @return the C++ connection code for the sample client
     */
    public String getConnectionCode() {
        return mCnxConfigPanel.getConnectionCode();
    }
}
