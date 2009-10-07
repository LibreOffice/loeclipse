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

import java.util.Vector;

import org.eclipse.swt.widgets.Composite;
import org.openoffice.ide.eclipse.core.gui.rows.OOoRow;
import org.openoffice.ide.eclipse.core.gui.rows.SdkRow;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.SDKContainer;
import org.openoffice.ide.eclipse.core.model.config.IConfigListener;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.ISdk;

/**
 * Class providing the OOo and SDK configuration rows.
 * 
 * @author cbosdonnat
 *
 */
public class OOoConfigPanel {
    
    private static final String SDK = "__sdk"; //$NON-NLS-1$
    private static final String OOO = "__ooo"; //$NON-NLS-1$

    /**
     * SDK used for the project selection row.
     */
    private SdkRow mSdkRow;
    
    /**
     * OOo used for the project selection row.
     */
    private OOoRow mOOoRow;
    private ConfigListener mConfigListener;
    
    /**
     * Constructor.
     * 
     * @param pParent the parent composite where to create the fields
     */
    public OOoConfigPanel ( Composite pParent ) {
        
        OOoContainer.addListener( mConfigListener );
        SDKContainer.addListener( mConfigListener );
        
        mSdkRow = new SdkRow( pParent, SDK, null );
        mOOoRow = new OOoRow( pParent, OOO, null );
    }
    
    /**
     * Disposes the object, mainly to unregister the listeners.
     */
    public void dispose( ) {
        OOoContainer.removeListener( mConfigListener );
        SDKContainer.removeListener( mConfigListener );
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
    
    /**
     * Set the SDK names to the SDK list-box.
     */
    private void fillSDKRow () {
        
        if (null != mSdkRow) {
            // Adding the SDK names to the combo box 
            String[] sdks = new String[SDKContainer.getSDKCount()];
            Vector<String> sdkKeys = SDKContainer.getSDKKeys();
            for (int i = 0, length = SDKContainer.getSDKCount(); i < length; i++) {
                sdks[i] = sdkKeys.get(i);
            }
            
            mSdkRow.removeAll();
            mSdkRow.addAll(sdks);
            // The default SDK is randomly the first one
            mSdkRow.select(0);
        }
    }

    /**
     * Set the OOo names to the OOo list-box.
     */
    private void fillOOoRow() {
        
        if (null != mOOoRow) {
            
            // Adding the OOo names to the combo box 
            String[] ooos = new String[OOoContainer.getOOoCount()];
            Vector<String> oooKeys = OOoContainer.getOOoKeys();
            for (int i = 0, length = OOoContainer.getOOoCount(); i < length; i++) {
                ooos[i] = oooKeys.get(i);
            }
            
            mOOoRow.removeAll();
            mOOoRow.addAll(ooos);
            // The default OOo is randomly the first one
            mOOoRow.select(0);
        }
    }
    
    /**
     * Class listening for the OOo and SDK config changes and updating the fields.
     * 
     * @author cbosdonnat
     *
     */
    private class ConfigListener implements IConfigListener {
        /**
         * {@inheritDoc}
         */
        public void ConfigAdded(Object pElement) {
            if (pElement instanceof IOOo) {
                fillOOoRow();
            } else {
                fillSDKRow();
            }
        }

        /**
         * {@inheritDoc}
         */
        public void ConfigRemoved(Object pElement) {
            
            if (null == pElement || pElement instanceof IOOo) {
                fillOOoRow();
            } 
            
            if (null == pElement || pElement instanceof ISdk) {
                fillSDKRow();
            }
        }

        /**
         * {@inheritDoc}
         */
        public void ConfigUpdated(Object pElement) {
            if (pElement instanceof IOOo) {
                fillOOoRow();
            } else {
                fillSDKRow();
            }
        };
    }
}
