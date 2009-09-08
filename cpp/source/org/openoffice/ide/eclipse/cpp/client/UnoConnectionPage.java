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
package org.openoffice.ide.eclipse.cpp.client;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openoffice.ide.eclipse.core.gui.OOoConfigPanel;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.SDKContainer;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.ISdk;

/**
 * Wizard page for the UNO Client configuration.
 * 
 * @author cbosdonnat
 *
 */
public class UnoConnectionPage extends WizardPage {

    private static final int LAYOUT_COLUMNS = 3;
    private UnoClientWizardPage mMainPage;
    private OOoConfigPanel mOOoConfigPanel;

    public UnoConnectionPage( ) {
        super( "unocnxpage" ); //$NON-NLS-1$
        setTitle( "UNO configuration" );
        setDescription( "Set some important informations for the UNO development" );
    }
    
    @Override
    public void createControl(Composite pParent) {

        Composite body = new Composite( pParent, SWT.NONE );
        body.setLayout( new GridLayout( LAYOUT_COLUMNS, false ) );
        body.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        
        // Add a title label here
        Label confLbl = new Label( body, SWT.NONE );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = LAYOUT_COLUMNS;
        confLbl.setLayoutData( gd );
        confLbl.setText( "OpenOffice.org and SDK for building" );
        
        // TODO Add a section for the sample connection config
        
        mOOoConfigPanel = new OOoConfigPanel( body );
        
        setControl( body );
    }
    
    /**
     * @return the selected OOo instance
     */
    public IOOo getOoo( ) {
        return OOoContainer.getOOo( mOOoConfigPanel.getOOoName() );
    }
    
    /**
     * @return the selected SDK instance
     */
    public ISdk getSdk( ) {
        return SDKContainer.getSDK( mOOoConfigPanel.getSDKName() );
    }
    
    /**
     * @return the normal next page of the CDT main page.
     */
    @Override
    public IWizardPage getNextPage() {
        return mMainPage.getNextCdtPage();
    }

    public void setMainPage(UnoClientWizardPage pMainPage) {
        mMainPage = pMainPage;
    }
}
