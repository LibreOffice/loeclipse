/*************************************************************************
 *
 * $RCSfile: ProjectPropertiesPage.java,v $
 *
 * $Revision: 1.8 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:27 $
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
package org.openoffice.ide.eclipse.core.preferences;

import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.OOoTable;
import org.openoffice.ide.eclipse.core.gui.SDKTable;
import org.openoffice.ide.eclipse.core.gui.rows.ChoiceRow;
import org.openoffice.ide.eclipse.core.gui.rows.LabeledRow;
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.SDKContainer;

/**
 * The project preference page. This page can be used to reconfigure the
 * project OOo and SDK.
 * 
 * @author cedricbosdo
 */
public class ProjectPropertiesPage extends PropertyPage 
                                   implements IWorkbenchPropertyPage, IConfigListener {

    private static final String SDK = "__sdk"; //$NON-NLS-1$
    private static final String OOO = "__ooo"; //$NON-NLS-1$
    
    private ChoiceRow mSdkRow;
    private ChoiceRow mOOoRow;
    
    private UnoidlProject mProject;
    
    /**
     * Default constructor setting configuration listeners.
     */
    public ProjectPropertiesPage() {
        super();
        
        noDefaultAndApplyButton();
        SDKContainer.addListener(this);
        OOoContainer.addListener(this);
    }
    
    /**
     * {@inheritDoc}
     */
    public void dispose() {
        
        SDKContainer.removeListener(this);
        OOoContainer.removeListener(this);
        
        super.dispose();
    }

    //------------------------------------------------------- Content managment
    
    /**
     * {@inheritDoc}
     */
    public void setElement(IAdaptable pElement) {
        super.setElement(pElement);
        
        try {
            mProject = (UnoidlProject)((IProject)getElement()).
                                getNature(OOEclipsePlugin.UNO_NATURE_ID);
            
        } catch (CoreException e) {
            PluginLogger.debug(e.getMessage());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected Control createContents(Composite pParent) {
        
        Composite body = new Composite(pParent, SWT.NONE);
        body.setLayout(new GridLayout(LabeledRow.LAYOUT_COLUMNS, false));
        
        // Add the SDK choice field
        mSdkRow = new ChoiceRow(body, SDK,
                        Messages.getString("ProjectPropertiesPage.UsedSdk"), //$NON-NLS-1$
                        Messages.getString("ProjectPropertiesPage.SdksBrowse")); //$NON-NLS-1$
        mSdkRow.setBrowseSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent pEvent) {
                super.widgetSelected(pEvent);
                
                // Open the SDK Configuration page
                TableDialog dialog = new TableDialog(getShell(), true);
                dialog.create();
                dialog.open();
                
            }
        });
        
        fillSDKRow();
        
        
        // Add the OOo choice field
        mOOoRow = new ChoiceRow(body, OOO,
                        Messages.getString("ProjectPropertiesPage.UsedOOo"), //$NON-NLS-1$
                        Messages.getString("ProjectPropertiesPage.OOoBrowse")); //$NON-NLS-1$
        mOOoRow.setBrowseSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent pEvent) {
                super.widgetSelected(pEvent);
                
                // Open the OOo Configuration page
                TableDialog dialog = new TableDialog(getShell(), false);
                dialog.create();
                dialog.open();
            }
        });
        
        fillOOoRow();
        
        return body;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean performOk() {
        saveValues();
        return true;
    }
    
    /**
     * Convenience method to save the SDK and OOo values in their plugin 
     * configuration file.
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
    
    /**
     * Fills the SDK row with the existing values of the SDK container.
     */
    private void fillSDKRow () {
        
        if (null != mSdkRow) {
            // Adding the SDK names to the combo box 
            String[] sdks = new String[SDKContainer.getSDKCount()];
            Vector<String> sdkKeys = SDKContainer.getSDKKeys();
            for (int i = 0, length = SDKContainer.getSDKCount(); i < length; i++) {
                sdks[i] = sdkKeys.get(i);
            }
            sdkKeys.clear();
            
            mSdkRow.removeAll();
            mSdkRow.addAll(sdks);
            
            if (null != mProject.getSdk()) {
                mSdkRow.select(mProject.getSdk().getId());
            } else {
                mSdkRow.select(0);
            }
        }
    }

    /**
     * Fills the OOo row with the existing values of the OOo container.
     */
    private void fillOOoRow() {
        
        if (null != mOOoRow) {
            
            // Adding the OOo names to the combo box 
            String[] ooos = new String[OOoContainer.getOOoCount()];
            Vector<String> oooKeys = OOoContainer.getOOoKeys();
            for (int i = 0, length = OOoContainer.getOOoCount(); i < length; i++) {
                ooos[i] = oooKeys.get(i);
            }
            oooKeys.clear();
            
            mOOoRow.removeAll();
            mOOoRow.addAll(ooos);
            if (null != mProject.getOOo()) {
                mOOoRow.select(mProject.getOOo().getName());
            } else {
                mOOoRow.select(0);
            }
        }
    }
    
    /**
     * The dialog to configure the plugin OOos and SDKs.
     * 
     * @author cedricbosdo
     *
     */
    private class TableDialog extends Dialog {
        
        private boolean mEditSdk = true;
        
        private Object mTable;
        
        /**
         * Constructor.
         * 
         * @param pParentShell the shell used for the dialog creation
         * @param pEditSDK <code>true</code> if the SDK is only edited, <code>false</code> otherwise.
         */
        TableDialog (Shell pParentShell, boolean pEditSDK) {
            super(pParentShell);
            setShellStyle(getShellStyle() | SWT.RESIZE);
            this.mEditSdk = pEditSDK;
            
            // This dialog is a modal one
            setBlockOnOpen(true);
            if (pEditSDK) {
                setTitle(Messages.getString("ProjectPropertiesPage.SdksBrowse")); //$NON-NLS-1$
            } else {
                setTitle(Messages.getString("ProjectPropertiesPage.OOoBrowse")); //$NON-NLS-1$
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

    //-----------------------------------------Implementation of ConfigListener
    
    /**
     * {@inheritDoc}
     */
    public void ConfigAdded(Object pElement) {
        fillSDKRow();
        fillOOoRow();
    }

    /**
     * {@inheritDoc}
     */
    public void ConfigRemoved(Object pElement) {
        fillSDKRow();
        fillOOoRow();
    }

    /**
     * {@inheritDoc}
     */
    public void ConfigUpdated(Object pElement) {
        fillSDKRow();
        fillOOoRow();
    }    

}
