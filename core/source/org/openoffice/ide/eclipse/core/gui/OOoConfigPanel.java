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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.openoffice.ide.eclipse.core.gui.rows.ChoiceRow;
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
    private ChoiceRow mSdkRow;
    
    /**
     * OOo used for the project selection row.
     */
    private ChoiceRow mOOoRow;
    private ConfigListener mConfigListener;
    
    /**
     * Constructor.
     * 
     * @param pParent the parent composite where to create the fields
     */
    public OOoConfigPanel ( Composite pParent ) {
        
        OOoContainer.addListener( mConfigListener );
        SDKContainer.addListener( mConfigListener );
        
        // Add the SDK choice field
        mSdkRow = new ChoiceRow(pParent, SDK,
                        Messages.getString("OOoConfigPanel.UsedSdk"), //$NON-NLS-1$
                        Messages.getString("OOoConfigPanel.SdkBrowse")); //$NON-NLS-1$
        mSdkRow.setBrowseSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent pEvent) {
                super.widgetSelected(pEvent);
                
                // Open the SDK Configuration page
                TableDialog dialog = new TableDialog( Display.getDefault().getActiveShell(), true);
                dialog.create();
                dialog.open();
                
            }
        });
        
        fillSDKRow();
        mSdkRow.setTooltip(Messages.getString("OOoConfigPanel.SdkTooltip")); //$NON-NLS-1$
        
        
        // Add the OOo choice field
        mOOoRow = new ChoiceRow(pParent, OOO,
                        Messages.getString("OOoConfigPanel.UsedOOo"), //$NON-NLS-1$
                        Messages.getString("OOoConfigPanel.OOoBrowse")); //$NON-NLS-1$
        mOOoRow.setBrowseSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent pEvent) {
                super.widgetSelected(pEvent);
                
                // Open the OOo Configuration page
                TableDialog dialog = new TableDialog( Display.getDefault().getActiveShell(), false);
                dialog.create();
                dialog.open();
            }
        });
        
        fillOOoRow();
        mOOoRow.setTooltip(Messages.getString("OOoConfigPanel.OOoTooltip")); //$NON-NLS-1$
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
    
    /**
     * Dialog for OOo and SDK configuration.
     * 
     * @author cedribosdo
     */
    private class TableDialog extends Dialog {
        
        private boolean mEditSdk = true;
        
        private Object mTable;
        
        /**
         * Constructor.
         * 
         * @param pParentShell the parent shell of the dialog.
         * @param pEditSDK <code>true</code> for SDK, <code>false</code> for OOo edition.
         */
        TableDialog (Shell pParentShell, boolean pEditSDK) {
            super(pParentShell);
            setShellStyle(getShellStyle() | SWT.RESIZE);
            mEditSdk = pEditSDK;
            
            // This dialog is a modal one
            setBlockOnOpen(true);
            if (pEditSDK) {
                getShell().setText(Messages.getString("OOoConfigPanel.SdkBrowse")); //$NON-NLS-1$
            } else {
                getShell().setText(Messages.getString("OOoConfigPanel.OOoBrowse")); //$NON-NLS-1$
            }
        }
        
        /**
         * {@inheritDoc}
         */
        protected Control createDialogArea(Composite pParent) {
            
            if (mEditSdk) {
                mTable = new SDKTable(pParent);
                ((SDKTable)mTable).getPreferences();
            } else {
                mTable = new OOoTable(pParent);
                ((OOoTable)mTable).getPreferences();
            }
                
            return pParent;
        }
        
        /**
         * {@inheritDoc}
         */
        protected void okPressed() {
            super.okPressed();
            
            if (mEditSdk) {
                ((SDKTable)mTable).savePreferences();
            } else {
                ((OOoTable)mTable).savePreferences();
            }
        }
    }
}
