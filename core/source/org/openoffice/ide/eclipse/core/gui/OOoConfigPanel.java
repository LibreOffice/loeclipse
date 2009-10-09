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
package org.openoffice.ide.eclipse.core.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.openoffice.ide.eclipse.core.gui.rows.OOoRow;
import org.openoffice.ide.eclipse.core.gui.rows.SdkRow;

/**
 * Class providing the OOo and SDK configuration rows.
 * 
 * @author cbosdonnat
 *
 */
public class OOoConfigPanel {

    private static final int GRID_COLUMNS = 3;
    
    private SdkRow mSdkRow;
    private OOoRow mOOoRow;
    
    /**
     * Constructor.
     * 
     * @param pParent the parent composite where to create the fields
     */
    public OOoConfigPanel ( Composite pParent ) {
        
        Group group = new Group( pParent, SWT.NONE );
        group.setText( Messages.getString("OOoConfigPanel.GroupTitle") ); //$NON-NLS-1$
        group.setLayout( new GridLayout( GRID_COLUMNS, false ) );
        group.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
        
        mSdkRow = new SdkRow( group, new String(), null );
        mOOoRow = new OOoRow( group, new String(), null );
    }
    
    /**
     * Disposes the object, mainly to unregister the listeners.
     */
    public void dispose( ) {
        mOOoRow.dispose();
        mSdkRow.dispose();
    }
    
    /**
     * @return SDK name selected
     */
    public String getSDKName() {
        String sdkName = new String();
        if (null != mSdkRow) {
            sdkName = mSdkRow.getValue();
        }
        return sdkName;
    }
    
    /** 
     * @return OOo name selected
     */
    public String getOOoName() {
        String oooName = new String();
        if (null != mOOoRow) {
            oooName = mOOoRow.getValue();
        }
        return oooName;
    }
}
